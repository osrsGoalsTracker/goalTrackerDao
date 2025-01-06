package com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;
import com.osrsGoalTracker.dao.goalTracker.exception.DuplicateUserException;
import com.osrsGoalTracker.dao.goalTracker.exception.ResourceNotFoundException;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.util.SortKeyUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB implementation for user-related operations.
 * Handles creating and retrieving user entities.
 */
public class DynamoUserDao {
    private static final Logger LOGGER = LogManager.getLogger(DynamoUserDao.class);

    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String USER_PREFIX = "USER#";

    private static final String ID = "id";
    private static final String EMAIL = "email";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    /**
     * Constructor for DynamoUserDao.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     * @param tableName      The name of the DynamoDB table
     */
    public DynamoUserDao(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    private void validateUserEntity(UserEntity user) {
        if (user == null) {
            LOGGER.warn("Attempted to create null user");
            throw new IllegalArgumentException("User entity cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            LOGGER.warn("Attempted to create user with null or empty email");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    private Map<String, AttributeValue> checkIfUserExists(String email) {
        Map<String, AttributeValue> expressionAttributeValues = Map.of(
                ":email", AttributeValue.builder().s(email).build(),
                ":sk", AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("email = :email AND sk = :sk")
                .expressionAttributeValues(expressionAttributeValues)
                .indexName("email-sk-index")
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        if (!queryResponse.items().isEmpty()) {
            return queryResponse.items().get(0);
        }
        return null;
    }

    private String generateNewUserId() {
        return UUID.randomUUID().toString();
    }

    private Map<String, AttributeValue> createNewUserItem(String userId, String email, String timestamp) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        item.put(SK, AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());
        item.put(ID, AttributeValue.builder().s(userId).build());
        item.put(EMAIL, AttributeValue.builder().s(email).build());
        item.put(CREATED_AT, AttributeValue.builder().s(timestamp).build());
        item.put(UPDATED_AT, AttributeValue.builder().s(timestamp).build());
        return item;
    }

    /**
     * Creates a new user in the database.
     *
     * @param user The user entity to create
     * @return The created user entity with generated ID and timestamps
     * @throws IllegalArgumentException If user is null or email is null/empty
     * @throws DuplicateUserException   If a user with the same email already exists
     */
    public UserEntity createUser(UserEntity user) {
        LOGGER.debug("Attempting to create user: {}", user);

        validateUserEntity(user);

        LOGGER.debug("Creating new user with email: {}", user.getEmail());

        Map<String, AttributeValue> existingItem = checkIfUserExists(user.getEmail());
        if (existingItem != null) {
            LOGGER.warn("Attempted to create user with existing email: {}", user.getEmail());
            throw new DuplicateUserException("User already exists with email: " + user.getEmail());
        }

        String newUserId = generateNewUserId();
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = createNewUserItem(newUserId, user.getEmail(), timestamp);

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .conditionExpression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                    .expressionAttributeNames(Map.of(
                            "#pk", PK,
                            "#sk", SK))
                    .build();

            LOGGER.debug("Putting new user item in DynamoDB with ID: {}", newUserId);
            dynamoDbClient.putItem(putItemRequest);
            LOGGER.info("Successfully created new user with ID: {} and email: {}", newUserId, user.getEmail());
        } catch (ConditionalCheckFailedException e) {
            LOGGER.warn("Concurrent attempt to create user with email: {}", user.getEmail());
            throw new DuplicateUserException("User already exists with email: " + user.getEmail());
        }

        return UserEntity.builder()
                .userId(newUserId)
                .email(user.getEmail())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Retrieves a user from the database.
     *
     * @param userId The ID of the user to retrieve
     * @return The user entity
     * @throws IllegalArgumentException  If userId is null or empty
     * @throws ResourceNotFoundException If user is not found
     */
    public UserEntity getUser(String userId) {
        LOGGER.debug("Getting user with ID: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warn("Attempted to get user with null or empty ID");
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        String pk = USER_PREFIX + userId;
        String sk = SortKeyUtil.getUserMetadataSortKey();

        Map<String, AttributeValue> key = new LinkedHashMap<>();
        key.put(PK, AttributeValue.builder().s(pk).build());
        key.put(SK, AttributeValue.builder().s(sk).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        if (!response.hasItem()) {
            LOGGER.warn("User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        Map<String, AttributeValue> item = response.item();
        UserEntity user = UserEntity.builder()
                .userId(item.get(ID).s())
                .email(item.get(EMAIL).s())
                .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
                .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
                .build();

        LOGGER.debug("Successfully retrieved user: {}", user);
        return user;
    }
}
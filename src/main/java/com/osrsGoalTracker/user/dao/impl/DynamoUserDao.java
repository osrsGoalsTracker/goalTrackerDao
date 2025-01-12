package com.osrsGoalTracker.user.dao.impl;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.osrsGoalTracker.shared.dao.exception.ResourceNotFoundException;
import com.osrsGoalTracker.shared.dao.util.SortKeyUtil;
import com.osrsGoalTracker.user.dao.UserDao;
import com.osrsGoalTracker.user.dao.entity.UserEntity;
import com.osrsGoalTracker.user.dao.exception.DuplicateUserException;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DynamoUserDao implements UserDao {
    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String USER_PREFIX = "USER#";

    private static final String ID = "id";
    private static final String EMAIL = "email";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

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
            log.warn("Attempted to create null user");
            throw new IllegalArgumentException("User entity cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.warn("Attempted to create user with null or empty email");
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

    private Map<String, AttributeValue> createNewUserItem(String userId, String email, Instant timestamp) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        item.put(SK, AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());
        item.put(ID, AttributeValue.builder().s(userId).build());
        item.put(EMAIL, AttributeValue.builder().s(email).build());
        item.put(CREATED_AT, AttributeValue.builder().s(timestamp.toString()).build());
        item.put(UPDATED_AT, AttributeValue.builder().s(timestamp.toString()).build());
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
        log.debug("Attempting to create user: {}", user);

        validateUserEntity(user);

        log.debug("Creating new user with email: {}", user.getEmail());

        Map<String, AttributeValue> existingItem = checkIfUserExists(user.getEmail());
        if (existingItem != null) {
            log.warn("Attempted to create user with existing email: {}", user.getEmail());
            throw new DuplicateUserException("User already exists with email: " + user.getEmail());
        }

        String newUserId = generateNewUserId();
        Instant now = Instant.now();

        Map<String, AttributeValue> item = createNewUserItem(newUserId, user.getEmail(), now);

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .conditionExpression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                    .expressionAttributeNames(Map.of(
                            "#pk", PK,
                            "#sk", SK))
                    .build();

            log.debug("Putting new user item in DynamoDB with ID: {}", newUserId);
            dynamoDbClient.putItem(putItemRequest);
            log.info("Successfully created new user with ID: {} and email: {}", newUserId, user.getEmail());
        } catch (ConditionalCheckFailedException e) {
            log.warn("Concurrent attempt to create user with email: {}", user.getEmail());
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
        log.debug("Getting user with ID: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to get user with null or empty ID");
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        Map<String, AttributeValue> key = new LinkedHashMap<>();
        key.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        key.put(SK, AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        log.debug("Getting user item from DynamoDB with ID: {}", userId);
        GetItemResponse response = dynamoDbClient.getItem(getItemRequest);

        if (!response.hasItem()) {
            log.warn("User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        Map<String, AttributeValue> item = response.item();
        return UserEntity.builder()
                .userId(item.get(ID).s())
                .email(item.get(EMAIL).s())
                .createdAt(Instant.parse(item.get(CREATED_AT).s()))
                .updatedAt(Instant.parse(item.get(UPDATED_AT).s()))
                .build();
    }
}
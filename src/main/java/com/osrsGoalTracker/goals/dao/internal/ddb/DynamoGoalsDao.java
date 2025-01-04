package com.osrsGoalTracker.goals.dao.internal.ddb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.osrsGoalTracker.goals.dao.GoalsDao;
import com.osrsGoalTracker.goals.dao.entity.RsnEntity;
import com.osrsGoalTracker.goals.dao.entity.UserEntity;
import com.osrsGoalTracker.goals.dao.exception.DuplicateUserException;
import com.osrsGoalTracker.goals.dao.exception.ResourceNotFoundException;
import com.osrsGoalTracker.goals.dao.internal.ddb.util.SortKeyUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB implementation of the GoalsDao interface.
 * Handles all DynamoDB operations for goals tracking data.
 * 
 * This implementation uses a single-table design with composite keys:
 * - Partition Key (PK): USER#userId
 * - Sort Key (SK): Various formats depending on the data type
 * - User metadata: METADATA
 * - RSN metadata: RSN#METADATA#rsn
 */
public class DynamoGoalsDao implements GoalsDao {
    private static final Logger LOGGER = LogManager.getLogger(DynamoGoalsDao.class);

    private static final String TABLE_NAME = "Goals";
    private static final String PK = "PK";
    private static final String SK = "SK";
    private static final String USER_PREFIX = "USER#";

    private static final String ID = "id";
    private static final String EMAIL = "email";
    private static final String RSN = "rsn";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final DynamoDbClient dynamoDbClient;

    /**
    * Constructor.
    * @param dynamoDbClient the client
    */
    @Inject
    public DynamoGoalsDao(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        LOGGER.debug("Creating new user: {}", user);

        if (user == null) {
            throw new IllegalArgumentException("User entity cannot be null");
        }
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK, AttributeValue.builder().s(USER_PREFIX + user.getUserId()).build());
        item.put(SK, AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());
        item.put(ID, AttributeValue.builder().s(user.getUserId()).build());
        item.put(EMAIL, AttributeValue.builder().s(user.getEmail()).build());
        item.put(CREATED_AT, AttributeValue.builder().s(timestamp).build());
        item.put(UPDATED_AT, AttributeValue.builder().s(timestamp).build());

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .conditionExpression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                .expressionAttributeNames(Map.of(
                    "#pk", PK,
                    "#sk", SK))
                .build();

            dynamoDbClient.putItem(putItemRequest);
        } catch (software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException e) {
            throw new DuplicateUserException("User already exists with ID: " + user.getUserId(), e);
        }

        return UserEntity.builder()
            .userId(user.getUserId())
            .email(user.getEmail())
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    @Override
    public UserEntity getUser(String userId) {
        LOGGER.debug("Getting user with ID: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        Map<String, AttributeValue> key = new HashMap<>();
        key.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        key.put(SK, AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());

        GetItemRequest request = GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(key)
            .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        if (!response.hasItem()) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        Map<String, AttributeValue> item = response.item();
        return UserEntity.builder()
            .userId(item.get(ID).s())
            .email(item.get(EMAIL).s())
            .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
            .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
            .build();
    }

    @Override
    public List<RsnEntity> getRsnsForUser(String userId) {
        LOGGER.debug("Getting RSNs for user with ID: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        QueryRequest request = QueryRequest.builder()
            .tableName(TABLE_NAME)
            .keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk_prefix)")
            .expressionAttributeNames(Map.of(
                "#pk", PK,
                "#sk", SK))
            .expressionAttributeValues(Map.of(
                ":pk", AttributeValue.builder().s(USER_PREFIX + userId).build(),
                ":sk_prefix", AttributeValue.builder().s(SortKeyUtil.getRsnMetadataPrefix()).build()))
            .build();

        QueryResponse response = dynamoDbClient.query(request);
        return response.items().stream()
            .map(item -> RsnEntity.builder()
                .userId(item.get(ID).s())
                .rsn(item.get(RSN).s())
                .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
                .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
                .build())
            .collect(Collectors.toList());
    }
} 

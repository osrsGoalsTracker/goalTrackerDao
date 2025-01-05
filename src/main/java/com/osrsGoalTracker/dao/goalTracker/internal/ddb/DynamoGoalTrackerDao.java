package com.osrsGoalTracker.dao.goalTracker.internal.ddb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
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

/**
 * DynamoDB implementation of the GoalsTrackerDao interface.
 * Handles all DynamoDB operations for goals tracking data.
 * 
 * This implementation uses a single-table design with composite keys:
 * - Partition Key (PK): USER#userId
 * - Sort Key (SK): Various formats depending on the data type
 * - User metadata: METADATA
 */
public class DynamoGoalTrackerDao implements GoalTrackerDao {
    private static final Logger LOGGER = LogManager.getLogger(DynamoGoalTrackerDao.class);

    private static final String TABLE_NAME = getTableName();
    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String USER_PREFIX = "USER#";

    private static final String ID = "id";
    private static final String EMAIL = "email";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final DynamoDbClient dynamoDbClient;

    private static String getTableName() {
        String tableName = System.getenv("GOALS_TABLE_NAME");
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = System.getProperty("GOALS_TABLE_NAME");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException(
                    "GOALS_TABLE_NAME must be set in environment variables or system properties");
        }
        return tableName;
    }

    /**
     * Constructor for DynamoGoalsTrackerDao.
     * Uses Guice for dependency injection of the DynamoDB client.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     */
    @Inject
    public DynamoGoalTrackerDao(DynamoDbClient dynamoDbClient) {
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

        Map<String, AttributeValue> item = new LinkedHashMap<>();
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
        } catch (ConditionalCheckFailedException e) {
            throw new DuplicateUserException("User already exists with ID: " + user.getUserId());
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

        String pk = USER_PREFIX + userId;
        String sk = SortKeyUtil.getUserMetadataSortKey();

        Map<String, AttributeValue> key = new LinkedHashMap<>();
        key.put(PK, AttributeValue.builder().s(pk).build());
        key.put(SK, AttributeValue.builder().s(sk).build());

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
}
package com.osrs.goal.dao;

import com.google.inject.Inject;
import com.osrs.goal.dao.entity.RsnEntity;
import com.osrs.goal.dao.entity.UserEntity;
import com.osrs.goal.dao.exception.ResourceNotFoundException;
import com.osrs.goal.dao.util.SortKeyUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamoDB implementation of the GoalDao interface.
 * Handles all DynamoDB operations for goal tracking data.
 * 
 * This implementation uses a single-table design with composite keys:
 * - Partition Key (PK): USER#userId
 * - Sort Key (SK): Various formats depending on the data type
 * - User metadata: METADATA
 * - RSN metadata: RSN#METADATA#rsn
 */
public class DynamoGoalDao implements GoalDao {
    // Logger instance for this class
    private static final Logger LOGGER = LogManager.getLogger(DynamoGoalDao.class);

    // DynamoDB table and attribute names
    private static final String TABLE_NAME = "Goals";
    private static final String PK = "PK"; // Partition key attribute
    private static final String SK = "SK"; // Sort key attribute
    private static final String USER_PREFIX = "USER#"; // Prefix for user partition keys

    // Entity attribute names in DynamoDB
    private static final String EMAIL = "email";
    private static final String RSN = "rsn";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    // Date formatter for timestamp attributes
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    // DynamoDB client injected by Guice
    private final DynamoDbClient dynamoDbClient;

    /**
     * Constructor for DynamoGoalDao.
     * Uses Guice for dependency injection of the DynamoDB client.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     */
    @Inject
    public DynamoGoalDao(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public UserEntity getUser(String userId) {
        LOGGER.debug("Getting user with ID: {}", userId);

        // Construct the composite key for user metadata
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        key.put(SK, AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build());

        // Build and execute the GetItem request
        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        if (!response.hasItem()) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Convert DynamoDB item to UserEntity
        Map<String, AttributeValue> item = response.item();
        return UserEntity.builder()
                .userId(userId)
                .email(item.get(EMAIL).s())
                .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
                .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
                .build();
    }

    @Override
    public List<RsnEntity> getRsnsForUser(String userId) {
        LOGGER.debug("Getting RSNs for user with ID: {}", userId);

        // Build query to find all RSNs for the user using a begins_with condition on
        // the sort key
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk_prefix)")
                // Use expression attribute names to avoid reserved word conflicts
                .expressionAttributeNames(Map.of(
                        "#pk", PK,
                        "#sk", SK))
                // Define the query parameters
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s(USER_PREFIX + userId).build(),
                        ":sk_prefix", AttributeValue.builder().s(SortKeyUtil.getRsnMetadataPrefix()).build()))
                .build();

        // Execute query and convert results to RsnEntity objects
        QueryResponse response = dynamoDbClient.query(request);
        return response.items().stream()
                .map(item -> RsnEntity.builder()
                        .userId(userId)
                        .rsn(item.get(RSN).s())
                        .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
                        .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
                        .build())
                .collect(Collectors.toList());
    }
}

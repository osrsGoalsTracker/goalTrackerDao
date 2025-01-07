package com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.osrsGoalTracker.dao.goalTracker.entity.PlayerEntity;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.util.SortKeyUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB implementation for player-related operations.
 * Handles adding players to user accounts.
 */
public class DynamoPlayerDao {
    private static final Logger LOGGER = LogManager.getLogger(DynamoPlayerDao.class);

    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String USER_PREFIX = "USER#";

    private static final String NAME = "name";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    /**
     * Constructor for DynamoPlayerDao.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     * @param tableName      The name of the DynamoDB table
     */
    public DynamoPlayerDao(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    private void validateAddPlayerToUserInput(String userId, String playerName) {
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warn("Attempted to add player with null or empty user ID");
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (playerName == null || playerName.trim().isEmpty()) {
            LOGGER.warn("Attempted to add player with null or empty name");
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
    }

    private Map<String, AttributeValue> createNewPlayerItem(String userId, String playerName, String timestamp) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        item.put(SK, AttributeValue.builder().s(SortKeyUtil.getPlayerMetadataSortKey() + "#" + playerName).build());
        item.put(NAME, AttributeValue.builder().s(playerName).build());
        item.put(CREATED_AT, AttributeValue.builder().s(timestamp).build());
        item.put(UPDATED_AT, AttributeValue.builder().s(timestamp).build());
        return item;
    }

    /**
     * Adds a RuneScape player to a user's account.
     *
     * @param userId     The ID of the user to add the player to
     * @param playerName The name of the RuneScape player to add
     * @return The created player entity
     * @throws IllegalArgumentException If userId or playerName is null or empty
     */
    public PlayerEntity addPlayerToUser(String userId, String playerName) {
        LOGGER.debug("Attempting to add player {} to user {}", playerName, userId);

        validateAddPlayerToUserInput(userId, playerName);

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = createNewPlayerItem(userId, playerName, timestamp);

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        LOGGER.debug("Putting new player item in DynamoDB for user {} with name {}", userId, playerName);
        dynamoDbClient.putItem(putItemRequest);
        LOGGER.info("Successfully added player {} to user {}", playerName, userId);

        return PlayerEntity.builder()
                .name(playerName)
                .userId(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Retrieves all players associated with a user.
     *
     * @param userId The ID of the user to get players for
     * @return List of player entities associated with the user
     * @throws IllegalArgumentException If userId is null or empty
     */
    public List<PlayerEntity> getPlayersForUser(String userId) {
        LOGGER.debug("Getting players for user {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warn("Attempted to get players with null or empty user ID");
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":pk", AttributeValue.builder().s(USER_PREFIX + userId).build());
        expressionAttributeValues.put(":sk_prefix",
                AttributeValue.builder().s(SortKeyUtil.getPlayerMetadataSortKey()).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("pk = :pk AND begins_with(sk, :sk_prefix)")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        LOGGER.debug("Querying DynamoDB for players with user ID: {}", userId);
        QueryResponse response = dynamoDbClient.query(queryRequest);

        List<PlayerEntity> players = response.items().stream()
                .map(item -> PlayerEntity.builder()
                        .userId(userId)
                        .name(item.get(NAME).s())
                        .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
                        .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
                        .build())
                .collect(Collectors.toList());

        LOGGER.info("Found {} players for user {}", players.size(), userId);
        return players;
    }
}
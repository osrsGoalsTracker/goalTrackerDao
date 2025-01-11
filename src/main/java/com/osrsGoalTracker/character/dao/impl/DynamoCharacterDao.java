package com.osrsGoalTracker.character.dao.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.osrsGoalTracker.character.dao.CharacterDao;
import com.osrsGoalTracker.character.dao.entity.CharacterEntity;
import com.osrsGoalTracker.shared.dao.util.SortKeyUtil;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB implementation for character-related operations.
 * Handles adding characters to user accounts.
 */
@Slf4j
public class DynamoCharacterDao implements CharacterDao {
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
     * Constructor for DynamoCharacterDao.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     * @param tableName      The name of the DynamoDB table
     */
    public DynamoCharacterDao(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    private void validateAddCharacterToUserInput(String userId, String characterName) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to add character with null or empty user ID");
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (characterName == null || characterName.trim().isEmpty()) {
            log.warn("Attempted to add character with null or empty name");
            throw new IllegalArgumentException("Character name cannot be null or empty");
        }
    }

    private Map<String, AttributeValue> createNewCharacterItem(String userId, String characterName, String timestamp) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(PK, AttributeValue.builder().s(USER_PREFIX + userId).build());
        item.put(SK, AttributeValue.builder().s(SortKeyUtil.getCharacterMetadataSortKey(characterName)).build());
        item.put(NAME, AttributeValue.builder().s(characterName).build());
        item.put(CREATED_AT, AttributeValue.builder().s(timestamp).build());
        item.put(UPDATED_AT, AttributeValue.builder().s(timestamp).build());
        return item;
    }

    /**
     * Adds a RuneScape character to a user's account.
     *
     * @param userId        The ID of the user to add the character to
     * @param characterName The name of the RuneScape character to add
     * @return The created character entity
     * @throws IllegalArgumentException If userId or characterName is null or empty
     */
    public CharacterEntity addCharacterToUser(String userId, String characterName) {
        log.debug("Attempting to add character {} to user {}", characterName, userId);

        validateAddCharacterToUserInput(userId, characterName);

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = createNewCharacterItem(userId, characterName, timestamp);

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        log.debug("Putting new character item in DynamoDB for user {} with name {}", userId, characterName);
        dynamoDbClient.putItem(putItemRequest);
        log.info("Successfully added character {} to user {}", characterName, userId);

        return CharacterEntity.builder()
                .name(characterName)
                .userId(userId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Retrieves all characters associated with a user.
     *
     * @param userId The ID of the user to get characters for
     * @return List of character entities associated with the user
     * @throws IllegalArgumentException If userId is null or empty
     */
    public List<CharacterEntity> getCharactersForUser(String userId) {
        log.debug("Getting characters for user {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to get characters with null or empty user ID");
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":pk", AttributeValue.builder().s(USER_PREFIX + userId).build());
        expressionAttributeValues.put(":sk_prefix",
                AttributeValue.builder().s(SortKeyUtil.CHARACTER_METADATA_PREFIX).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("pk = :pk AND begins_with(sk, :sk_prefix)")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        log.debug("Querying DynamoDB for characters with user ID: {}", userId);
        QueryResponse response = dynamoDbClient.query(queryRequest);

        List<CharacterEntity> characters = response.items().stream()
                .map(item -> CharacterEntity.builder()
                        .userId(userId)
                        .name(item.get(NAME).s())
                        .createdAt(LocalDateTime.parse(item.get(CREATED_AT).s(), DATE_TIME_FORMATTER))
                        .updatedAt(LocalDateTime.parse(item.get(UPDATED_AT).s(), DATE_TIME_FORMATTER))
                        .build())
                .collect(Collectors.toList());

        log.info("Found {} characters for user {}", characters.size(), userId);
        return characters;
    }
}
package com.osrsGoalTracker.goal.dao.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import com.google.inject.Inject;
import com.osrsGoalTracker.goal.dao.GoalDao;
import com.osrsGoalTracker.goal.dao.entity.GoalEntity;
import com.osrsGoalTracker.goal.dao.impl.DynamoItem.DynamoGoalMetadataItem;
import com.osrsGoalTracker.goal.dao.impl.DynamoItem.DynamoGoalProgressItem;
import com.osrsGoalTracker.shared.dao.util.SortKeyUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

/**
 * Implementation of the GoalDao interface using DynamoDB.
 */
@Slf4j
@RequiredArgsConstructor
public class DynamoGoalDao implements GoalDao {
    @Inject
    private final DynamoDbClient dynamoDbClient;
    @Inject
    private final DynamoDbTable<DynamoGoalMetadataItem> metadataTable;
    @Inject
    private final DynamoDbTable<DynamoGoalProgressItem> progressTable;

    private void validateStringNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    private void validateGoalEntity(GoalEntity goalEntity) {
        validateNotNull(goalEntity, "goalEntity");
        validateStringNotEmpty(goalEntity.getUserId(), "userId");
        validateStringNotEmpty(goalEntity.getCharacterName(), "characterName");
        validateStringNotEmpty(goalEntity.getTargetAttribute(), "targetAttribute");
        validateStringNotEmpty(goalEntity.getTargetType(), "targetType");
        validateNotNull(goalEntity.getTargetValue(), "targetValue");
    }

    private DynamoGoalMetadataItem createMetadataItem(String userId, String characterName, String goalId,
            GoalEntity goalEntity, Instant timestamp) {
        return DynamoGoalMetadataItem.builder()
                .pk("USER#" + userId)
                .sk(SortKeyUtil.buildGoalMetadataSortKey(characterName, goalId))
                .userId(userId)
                .characterName(characterName)
                .goalId(goalId)
                .targetAttribute(goalEntity.getTargetAttribute())
                .targetType(goalEntity.getTargetType())
                .targetValue(goalEntity.getTargetValue())
                .targetDate(goalEntity.getTargetDate())
                .notificationChannelType(goalEntity.getNotificationChannelType())
                .frequency(goalEntity.getFrequency())
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }

    private DynamoGoalProgressItem createProgressItem(String userId, String characterName, String goalId,
            Instant timestamp, String sortKey, long currentValue) {
        return DynamoGoalProgressItem.builder()
                .pk("USER#" + userId)
                .sk(sortKey)
                .userId(userId)
                .characterName(characterName)
                .goalId(goalId)
                .progressValue(currentValue)
                .createdAt(timestamp)
                .build();
    }

    private TransactWriteItemsRequest createTransactionRequest(String userId, String characterName, String goalId,
            DynamoGoalMetadataItem metadataItem, Instant timestamp, long currentValue) {
        // Create progress items
        DynamoGoalProgressItem progressItem = createProgressItem(userId, characterName, goalId,
                timestamp, SortKeyUtil.buildGoalProgressSortKey(characterName, goalId, timestamp), currentValue);
        DynamoGoalProgressItem latestItem = createProgressItem(userId, characterName, goalId,
                timestamp, SortKeyUtil.buildGoalLatestSortKey(characterName, goalId), currentValue);
        DynamoGoalProgressItem earliestItem = createProgressItem(userId, characterName, goalId,
                timestamp, SortKeyUtil.buildGoalEarliestSortKey(characterName, goalId), currentValue);

        return TransactWriteItemsRequest.builder()
                .transactItems(Arrays.asList(
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(metadataTable.tableName())
                                        .item(metadataTable.tableSchema().itemToMap(metadataItem, true))
                                        .build())
                                .build(),
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(progressTable.tableName())
                                        .item(progressTable.tableSchema().itemToMap(progressItem, true))
                                        .build())
                                .build(),
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(progressTable.tableName())
                                        .item(progressTable.tableSchema().itemToMap(latestItem, true))
                                        .build())
                                .build(),
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(progressTable.tableName())
                                        .item(progressTable.tableSchema().itemToMap(earliestItem, true))
                                        .build())
                                .build()))
                .build();
    }

    private GoalEntity createReturnEntity(String goalId, GoalEntity goalEntity, Instant timestamp) {
        return GoalEntity.builder()
                .userId(goalEntity.getUserId())
                .goalId(goalId)
                .characterName(goalEntity.getCharacterName())
                .targetAttribute(goalEntity.getTargetAttribute())
                .targetType(goalEntity.getTargetType())
                .targetValue(goalEntity.getTargetValue())
                .targetDate(goalEntity.getTargetDate())
                .notificationChannelType(goalEntity.getNotificationChannelType())
                .frequency(goalEntity.getFrequency())
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }

    @Override
    public GoalEntity createGoal(GoalEntity goalEntity, long currentValue) {
        validateGoalEntity(goalEntity);

        log.info("Creating new goal for user: {}, character: {}, targetAttribute: {}",
                goalEntity.getUserId(), goalEntity.getCharacterName(), goalEntity.getTargetAttribute());

        String goalId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // Create the goal metadata item
        DynamoGoalMetadataItem metadataItem = createMetadataItem(goalEntity.getUserId(), goalEntity.getCharacterName(),
                goalId, goalEntity, now);
        log.debug("Created metadata item with goalId: {}, pk: {}, sk: {}",
                goalId, metadataItem.getPk(), metadataItem.getSk());

        // Create transaction request with all items
        TransactWriteItemsRequest transactionRequest = createTransactionRequest(goalEntity.getUserId(),
                goalEntity.getCharacterName(), goalId, metadataItem, now, currentValue);
        log.debug("Initiating transaction to create goal and progress records");

        try {
            dynamoDbClient.transactWriteItems(transactionRequest);
            log.info("Successfully created goal with id: {} for user: {}, character: {}",
                    goalId, goalEntity.getUserId(), goalEntity.getCharacterName());
        } catch (Exception e) {
            log.error("Failed to create goal for user: {}, character: {}, error: {}",
                    goalEntity.getUserId(), goalEntity.getCharacterName(), e.getMessage());
            throw e;
        }

        return createReturnEntity(goalId, goalEntity, now);
    }
}
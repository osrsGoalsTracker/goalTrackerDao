package com.osrsGoalTracker.goal.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import com.osrsGoalTracker.goal.dao.entity.GoalEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

@ExtendWith(MockitoExtension.class)
class DynamoGoalDaoTest {
    private static final String USER_ID = "testUser";
    private static final String CHARACTER_NAME = "testCharacter";
    private static final String TARGET_ATTRIBUTE = "Woodcutting";
    private static final String TARGET_TYPE = "xp";
    private static final Long TARGET_VALUE = 13034431L;
    private static final Instant TARGET_DATE = Instant.ofEpochMilli(1715731200000L);
    private static final String NOTIFICATION_CHANNEL_TYPE = "SMS";
    private static final String FREQUENCY = "daily";
    private static final int EXPECTED_TRANSACTION_ITEMS = 4; // 1 metadata + 3 progress records
    private static final long CURRENT_VALUE = 1000L;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private DynamoDbTable<DynamoGoalMetadataItem> metadataTable;

    @Mock
    private DynamoDbTable<DynamoGoalProgressItem> progressTable;

    @Captor
    private ArgumentCaptor<TransactWriteItemsRequest> transactRequestCaptor;

    private DynamoGoalDao goalDao;

    @BeforeEach
    void setUp() {
        goalDao = new DynamoGoalDao(dynamoDbClient, metadataTable, progressTable);
    }

    @Test
    void testCreateGoalWithValidInputCreatesSuccessfully() {
        // Given
        when(metadataTable.tableName()).thenReturn("Goals");
        when(metadataTable.tableSchema()).thenReturn(TableSchema.fromBean(DynamoGoalMetadataItem.class));
        when(progressTable.tableName()).thenReturn("Goals");
        when(progressTable.tableSchema()).thenReturn(TableSchema.fromBean(DynamoGoalProgressItem.class));

        GoalEntity goalToCreate = GoalEntity.builder()
                .userId(USER_ID)
                .characterName(CHARACTER_NAME)
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetType(TARGET_TYPE)
                .targetValue(TARGET_VALUE)
                .targetDate(TARGET_DATE)
                .notificationChannelType(NOTIFICATION_CHANNEL_TYPE)
                .frequency(FREQUENCY)
                .build();

        // When
        GoalEntity createdGoal = goalDao.createGoal(goalToCreate, CURRENT_VALUE);

        // Then
        verify(dynamoDbClient).transactWriteItems(transactRequestCaptor.capture());
        TransactWriteItemsRequest transactRequest = transactRequestCaptor.getValue();

        // Verify we have the expected number of items in the transaction
        assertThat(transactRequest.transactItems()).hasSize(EXPECTED_TRANSACTION_ITEMS);

        // Verify the created goal entity
        assertThat(createdGoal.getUserId()).isEqualTo(USER_ID);
        assertThat(createdGoal.getCharacterName()).isEqualTo(CHARACTER_NAME);
        assertThat(createdGoal.getTargetAttribute()).isEqualTo(TARGET_ATTRIBUTE);
        assertThat(createdGoal.getTargetType()).isEqualTo(TARGET_TYPE);
        assertThat(createdGoal.getTargetValue()).isEqualTo(TARGET_VALUE);
        assertThat(createdGoal.getTargetDate()).isEqualTo(TARGET_DATE);
        assertThat(createdGoal.getNotificationChannelType()).isEqualTo(NOTIFICATION_CHANNEL_TYPE);
        assertThat(createdGoal.getFrequency()).isEqualTo(FREQUENCY);
        assertThat(createdGoal.getCreatedAt()).isNotNull();
        assertThat(createdGoal.getUpdatedAt()).isNotNull();
        assertThat(createdGoal.getGoalId()).isNotNull();
    }

    @Test
    void testCreateGoalWithNullGoalEntityThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> goalDao.createGoal(null, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("goalEntity cannot be null");
    }

    @Test
    void testCreateGoalWithNullUserIdThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .characterName(CHARACTER_NAME)
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetType(TARGET_TYPE)
                .targetValue(TARGET_VALUE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId cannot be null or empty");
    }

    @Test
    void testCreateGoalWithEmptyUserIdThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .userId("  ")
                .characterName(CHARACTER_NAME)
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetType(TARGET_TYPE)
                .targetValue(TARGET_VALUE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId cannot be null or empty");
    }

    @Test
    void testCreateGoalWithNullCharacterNameThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .userId(USER_ID)
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetType(TARGET_TYPE)
                .targetValue(TARGET_VALUE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("characterName cannot be null or empty");
    }

    @Test
    void testCreateGoalWithEmptyCharacterNameThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .userId(USER_ID)
                .characterName("  ")
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetType(TARGET_TYPE)
                .targetValue(TARGET_VALUE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("characterName cannot be null or empty");
    }

    @Test
    void testCreateGoalWithNullTargetAttributeThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .userId(USER_ID)
                .characterName(CHARACTER_NAME)
                .targetType(TARGET_TYPE)
                .targetValue(TARGET_VALUE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetAttribute cannot be null or empty");
    }

    @Test
    void testCreateGoalWithNullTargetTypeThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .userId(USER_ID)
                .characterName(CHARACTER_NAME)
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetValue(TARGET_VALUE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetType cannot be null or empty");
    }

    @Test
    void testCreateGoalWithNullTargetValueThrowsIllegalArgumentException() {
        GoalEntity goalToCreate = GoalEntity.builder()
                .userId(USER_ID)
                .characterName(CHARACTER_NAME)
                .targetAttribute(TARGET_ATTRIBUTE)
                .targetType(TARGET_TYPE)
                .build();

        assertThatThrownBy(() -> goalDao.createGoal(goalToCreate, CURRENT_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("targetValue cannot be null");
    }
}
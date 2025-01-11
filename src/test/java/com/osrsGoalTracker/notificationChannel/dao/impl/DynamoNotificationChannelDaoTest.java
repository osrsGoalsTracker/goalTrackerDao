package com.osrsGoalTracker.notificationChannel.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

import com.osrsGoalTracker.notificationChannel.dao.entity.NotificationChannelEntity;
import com.osrsGoalTracker.shared.dao.util.SortKeyUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@ExtendWith(MockitoExtension.class)
class DynamoNotificationChannelDaoTest {
    private static final String TABLE_NAME = "test-table";
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CHANNEL_TYPE = "DISCORD";
    private static final String TEST_IDENTIFIER = "test-identifier";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    @Captor
    private ArgumentCaptor<QueryRequest> queryRequestCaptor;

    private DynamoNotificationChannelDao dynamoNotificationChannelDao;

    @BeforeEach
    void setUp() {
        dynamoNotificationChannelDao = new DynamoNotificationChannelDao(dynamoDbClient, TABLE_NAME);
    }

    @Test
    void testCreateNotificationChannelWithValidChannelCreatesSuccessfully() {
        // Given
        NotificationChannelEntity channelToCreate = NotificationChannelEntity.builder()
                .channelType(TEST_CHANNEL_TYPE)
                .identifier(TEST_IDENTIFIER)
                .isActive(true)
                .build();

        // When
        NotificationChannelEntity createdChannel = dynamoNotificationChannelDao.createNotificationChannel(
                TEST_USER_ID, channelToCreate);

        // Then
        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        PutItemRequest putRequest = putItemRequestCaptor.getValue();
        assertThat(putRequest.tableName()).isEqualTo(TABLE_NAME);

        Map<String, AttributeValue> item = putRequest.item();
        assertThat(item.get("channelType").s()).isEqualTo(TEST_CHANNEL_TYPE);
        assertThat(item.get("identifier").s()).isEqualTo(TEST_IDENTIFIER);
        assertThat(item.get("isActive").bool()).isTrue();
        assertThat(item.get("sk").s()).isEqualTo(SortKeyUtil.getNotificationChannelSortKey(TEST_CHANNEL_TYPE));

        assertThat(createdChannel.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(createdChannel.getChannelType()).isEqualTo(TEST_CHANNEL_TYPE);
        assertThat(createdChannel.getIdentifier()).isEqualTo(TEST_IDENTIFIER);
        assertThat(createdChannel.isActive()).isTrue();
        assertThat(createdChannel.getCreatedAt()).isNotNull();
        assertThat(createdChannel.getUpdatedAt()).isNotNull();
    }

    @Test
    void testCreateNotificationChannelWithNullUserIdThrowsIllegalArgumentException() {
        NotificationChannelEntity channel = NotificationChannelEntity.builder()
                .channelType(TEST_CHANNEL_TYPE)
                .identifier(TEST_IDENTIFIER)
                .isActive(true)
                .build();

        assertThatThrownBy(() -> dynamoNotificationChannelDao.createNotificationChannel(null, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }

    @Test
    void testCreateNotificationChannelWithNullChannelThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoNotificationChannelDao.createNotificationChannel(TEST_USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification channel entity cannot be null");
    }

    @Test
    void testCreateNotificationChannelWithNullChannelTypeThrowsIllegalArgumentException() {
        NotificationChannelEntity channel = NotificationChannelEntity.builder()
                .identifier(TEST_IDENTIFIER)
                .isActive(true)
                .build();

        assertThatThrownBy(() -> dynamoNotificationChannelDao.createNotificationChannel(TEST_USER_ID, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Channel type cannot be null or empty");
    }

    @Test
    void testCreateNotificationChannelWithNullIdentifierThrowsIllegalArgumentException() {
        NotificationChannelEntity channel = NotificationChannelEntity.builder()
                .channelType(TEST_CHANNEL_TYPE)
                .isActive(true)
                .build();

        assertThatThrownBy(() -> dynamoNotificationChannelDao.createNotificationChannel(TEST_USER_ID, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Identifier cannot be null or empty");
    }

    @Test
    void testGetNotificationChannelsWithValidUserIdReturnsChannels() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> channel1 = Map.of(
                "channelType", AttributeValue.builder().s(TEST_CHANNEL_TYPE).build(),
                "identifier", AttributeValue.builder().s(TEST_IDENTIFIER).build(),
                "isActive", AttributeValue.builder().bool(true).build(),
                "createdAt", AttributeValue.builder().s(timestamp).build(),
                "updatedAt", AttributeValue.builder().s(timestamp).build());

        Map<String, AttributeValue> channel2 = Map.of(
                "channelType", AttributeValue.builder().s("SMS").build(),
                "identifier", AttributeValue.builder().s("1234567890").build(),
                "isActive", AttributeValue.builder().bool(false).build(),
                "createdAt", AttributeValue.builder().s(timestamp).build(),
                "updatedAt", AttributeValue.builder().s(timestamp).build());

        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(Arrays.asList(channel1, channel2)).build());

        // When
        var channels = dynamoNotificationChannelDao.getNotificationChannels(TEST_USER_ID);

        // Then
        verify(dynamoDbClient).query(queryRequestCaptor.capture());
        QueryRequest queryRequest = queryRequestCaptor.getValue();
        assertThat(queryRequest.tableName()).isEqualTo(TABLE_NAME);
        assertThat(queryRequest.keyConditionExpression()).contains("begins_with(sk, :sk_prefix)");

        assertThat(channels).hasSize(2);

        NotificationChannelEntity firstChannel = channels.get(0);
        assertThat(firstChannel.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(firstChannel.getChannelType()).isEqualTo(TEST_CHANNEL_TYPE);
        assertThat(firstChannel.getIdentifier()).isEqualTo(TEST_IDENTIFIER);
        assertThat(firstChannel.isActive()).isTrue();
        assertThat(firstChannel.getCreatedAt()).isEqualTo(now);
        assertThat(firstChannel.getUpdatedAt()).isEqualTo(now);

        NotificationChannelEntity secondChannel = channels.get(1);
        assertThat(secondChannel.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(secondChannel.getChannelType()).isEqualTo("SMS");
        assertThat(secondChannel.getIdentifier()).isEqualTo("1234567890");
        assertThat(secondChannel.isActive()).isFalse();
        assertThat(secondChannel.getCreatedAt()).isEqualTo(now);
        assertThat(secondChannel.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testGetNotificationChannelsWithNullUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoNotificationChannelDao.getNotificationChannels(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }

    @Test
    void testGetNotificationChannelsWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoNotificationChannelDao.getNotificationChannels("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }
}
package com.osrsGoalTracker.notificationChannel.dao.internal.ddb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.osrsGoalTracker.notificationChannel.dao.entity.NotificationChannelEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * Tests for {@link DynamoNotificationChannelDao}.
 */
public class DynamoNotificationChannelDaoTest {
    private static final String TEST_TABLE_NAME = "test-table";
    private static final String TEST_USER_ID = "testUser123";
    private static final String TEST_CHANNEL_TYPE = "DISCORD";
    private static final String TEST_IDENTIFIER = "discord-id-123";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient mockDynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    @Captor
    private ArgumentCaptor<QueryRequest> queryRequestCaptor;

    private DynamoNotificationChannelDao dynamoNotificationChannelDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dynamoNotificationChannelDao = new DynamoNotificationChannelDao(mockDynamoDbClient, TEST_TABLE_NAME);
    }

    @Test
    void testCreateNotificationChannelSuccess() {
        // Given
        NotificationChannelEntity inputChannel = NotificationChannelEntity.builder()
                .channelType(TEST_CHANNEL_TYPE)
                .identifier(TEST_IDENTIFIER)
                .isActive(true)
                .build();

        // When
        NotificationChannelEntity result = 
                dynamoNotificationChannelDao.createNotificationChannel(TEST_USER_ID, inputChannel);

        // Then
        verify(mockDynamoDbClient).putItem(putItemRequestCaptor.capture());
        PutItemRequest capturedRequest = putItemRequestCaptor.getValue();

        assertEquals(TEST_TABLE_NAME, capturedRequest.tableName());
        Map<String, AttributeValue> item = capturedRequest.item();
        assertEquals("USER#" + TEST_USER_ID, item.get("pk").s());
        assertTrue(item.get("sk").s().startsWith("NOTIFICATION#"));
        assertEquals(TEST_CHANNEL_TYPE, item.get("channelType").s());
        assertEquals(TEST_IDENTIFIER, item.get("identifier").s());
        assertTrue(item.get("isActive").bool());
        assertNotNull(item.get("createdAt").s());
        assertNotNull(item.get("updatedAt").s());

        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_CHANNEL_TYPE, result.getChannelType());
        assertEquals(TEST_IDENTIFIER, result.getIdentifier());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void testCreateNotificationChannelWithNullUserId() {
        NotificationChannelEntity channel = NotificationChannelEntity.builder()
                .channelType(TEST_CHANNEL_TYPE)
                .identifier(TEST_IDENTIFIER)
                .isActive(true)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> dynamoNotificationChannelDao.createNotificationChannel(null, channel));
    }

    @Test
    void testCreateNotificationChannelWithNullChannel() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoNotificationChannelDao.createNotificationChannel(TEST_USER_ID, null));
    }

    @Test
    void testGetNotificationChannelsSuccess() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("pk", AttributeValue.builder().s("USER#" + TEST_USER_ID).build());
        item.put("sk", AttributeValue.builder().s("NOTIFICATION#" + TEST_CHANNEL_TYPE).build());
        item.put("channelType", AttributeValue.builder().s(TEST_CHANNEL_TYPE).build());
        item.put("identifier", AttributeValue.builder().s(TEST_IDENTIFIER).build());
        item.put("isActive", AttributeValue.builder().bool(true).build());
        item.put("createdAt", AttributeValue.builder().s(now.format(DATE_TIME_FORMATTER)).build());
        item.put("updatedAt", AttributeValue.builder().s(now.format(DATE_TIME_FORMATTER)).build());

        QueryResponse mockResponse = QueryResponse.builder()
                .items(List.of(item))
                .build();

        when(mockDynamoDbClient.query(any(QueryRequest.class))).thenReturn(mockResponse);

        // When
        List<NotificationChannelEntity> results = dynamoNotificationChannelDao.getNotificationChannels(TEST_USER_ID);

        // Then
        verify(mockDynamoDbClient).query(queryRequestCaptor.capture());
        QueryRequest capturedRequest = queryRequestCaptor.getValue();

        assertEquals(TEST_TABLE_NAME, capturedRequest.tableName());
        assertEquals("pk = :pk AND begins_with(sk, :sk_prefix)", capturedRequest.keyConditionExpression());

        assertFalse(results.isEmpty());
        NotificationChannelEntity result = results.get(0);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_CHANNEL_TYPE, result.getChannelType());
        assertEquals(TEST_IDENTIFIER, result.getIdentifier());
        assertTrue(result.isActive());
        assertEquals(now.format(DATE_TIME_FORMATTER), result.getCreatedAt().format(DATE_TIME_FORMATTER));
        assertEquals(now.format(DATE_TIME_FORMATTER), result.getUpdatedAt().format(DATE_TIME_FORMATTER));
    }

    @Test
    void testGetNotificationChannelsWithNullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoNotificationChannelDao.getNotificationChannels(null));
    }
}
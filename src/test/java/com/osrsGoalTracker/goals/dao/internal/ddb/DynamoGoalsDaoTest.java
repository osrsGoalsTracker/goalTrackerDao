package com.osrsGoalTracker.goals.dao.internal.ddb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.osrsGoalTracker.goals.dao.entity.UserEntity;
import com.osrsGoalTracker.goals.dao.exception.ResourceNotFoundException;
import com.osrsGoalTracker.goals.dao.internal.ddb.util.SortKeyUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Unit tests for DynamoGoalsDao.
 * Tests the DAO's interaction with DynamoDB for user operations.
 */
class DynamoGoalsDaoTest {
    private static final String TABLE_NAME = "Goals";
    private static final String USER_PREFIX = "USER#";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private DynamoDbClient dynamoDbClient;
    private DynamoGoalsDao goalsDao;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        goalsDao = new DynamoGoalsDao(dynamoDbClient);
    }

    @Test
    void testCreateUserWithValidEmail() {
        // Given
        String email = "test@example.com";

        // When
        UserEntity result = goalsDao.createUser(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNotNull(result.getUserId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        // Verify DynamoDB interaction
        ArgumentCaptor<PutItemRequest> requestCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(requestCaptor.capture());

        PutItemRequest request = requestCaptor.getValue();
        assertEquals(TABLE_NAME, request.tableName());

        Map<String, AttributeValue> item = request.item();
        assertNotNull(item.get("PK"));
        assertEquals(SortKeyUtil.getUserMetadataSortKey(), item.get("SK").s());
        assertEquals(email, item.get("email").s());
        assertNotNull(item.get("createdAt"));
        assertNotNull(item.get("updatedAt"));
    }

    @Test
    void testCreateUserWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> goalsDao.createUser(null));
    }

    @Test
    void testCreateUserWithEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> goalsDao.createUser(""));
    }

    @Test
    void testGetExistingUser() {
        // Given
        String userId = "testUserId";
        String email = "test@example.com";
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = Map.of(
                "PK", AttributeValue.builder().s(USER_PREFIX + userId).build(),
                "SK", AttributeValue.builder().s(SortKeyUtil.getUserMetadataSortKey()).build(),
                "userId", AttributeValue.builder().s(userId).build(),
                "email", AttributeValue.builder().s(email).build(),
                "createdAt", AttributeValue.builder().s(timestamp).build(),
                "updatedAt", AttributeValue.builder().s(timestamp).build());

        GetItemResponse response = GetItemResponse.builder()
                .item(item)
                .build();

        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        // When
        UserEntity result = goalsDao.getUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        assertEquals(now.format(DATE_TIME_FORMATTER),
                result.getCreatedAt().format(DATE_TIME_FORMATTER));
        assertEquals(now.format(DATE_TIME_FORMATTER),
                result.getUpdatedAt().format(DATE_TIME_FORMATTER));

        // Verify DynamoDB interaction
        ArgumentCaptor<GetItemRequest> requestCaptor = ArgumentCaptor.forClass(GetItemRequest.class);
        verify(dynamoDbClient).getItem(requestCaptor.capture());

        GetItemRequest request = requestCaptor.getValue();
        assertEquals(TABLE_NAME, request.tableName());
        assertEquals(USER_PREFIX + userId, request.key().get("PK").s());
        assertEquals(SortKeyUtil.getUserMetadataSortKey(), request.key().get("SK").s());
    }

    @Test
    void testGetNonExistentUser() {
        // Given
        String userId = "nonExistentUser";
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        // Then
        assertThrows(ResourceNotFoundException.class, () -> goalsDao.getUser(userId));
    }

    @Test
    void testGetUserWithNullId() {
        assertThrows(IllegalArgumentException.class, () -> goalsDao.getUser(null));
    }

    @Test
    void testGetUserWithEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> goalsDao.getUser(""));
    }
}

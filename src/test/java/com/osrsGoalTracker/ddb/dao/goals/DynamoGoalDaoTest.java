package com.osrsGoalTracker.ddb.dao.goals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.osrsGoalTracker.ddb.dao.goals.entity.UserEntity;
import com.osrsGoalTracker.ddb.dao.goals.exception.ResourceNotFoundException;
import com.osrsGoalTracker.ddb.dao.goals.util.SortKeyUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoGoalDaoTest {
    private static final String TABLE_NAME = "Goals";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoGoalDao goalDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        goalDao = new DynamoGoalDao(dynamoDbClient);
    }

    @Test
    void testCreateUserWithValidEmail() {
        // Given
        String email = "test@example.com";

        // When
        UserEntity result = goalDao.createUser(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNotNull(result.getUserId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        ArgumentCaptor<PutItemRequest> requestCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(requestCaptor.capture());

        PutItemRequest request = requestCaptor.getValue();
        assertEquals(TABLE_NAME, request.tableName());
        Map<String, AttributeValue> item = request.item();
        assertEquals("USER#" + result.getUserId(), item.get("PK").s());
        assertEquals(SortKeyUtil.getUserMetadataSortKey(), item.get("SK").s());
        assertEquals(email, item.get("email").s());
    }

    @Test
    void testCreateUserWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> goalDao.createUser(null));
    }

    @Test
    void testCreateUserWithEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> goalDao.createUser(""));
    }

    @Test
    void testGetExistingUser() {
        // Given
        String userId = "test-user-id";
        String email = "test@example.com";
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("userId", AttributeValue.builder().s(userId).build());
        item.put("email", AttributeValue.builder().s(email).build());
        item.put("createdAt", AttributeValue.builder().s(timestamp).build());
        item.put("updatedAt", AttributeValue.builder().s(timestamp).build());

        GetItemResponse mockResponse = GetItemResponse.builder()
                .item(item)
                .build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockResponse);

        // When
        UserEntity result = goalDao.getUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        assertEquals(LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER), result.getCreatedAt());
        assertEquals(LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER), result.getUpdatedAt());

        ArgumentCaptor<GetItemRequest> requestCaptor = ArgumentCaptor.forClass(GetItemRequest.class);
        verify(dynamoDbClient).getItem(requestCaptor.capture());

        GetItemRequest request = requestCaptor.getValue();
        assertEquals(TABLE_NAME, request.tableName());
        assertEquals("USER#" + userId, request.key().get("PK").s());
        assertEquals(SortKeyUtil.getUserMetadataSortKey(), request.key().get("SK").s());
    }

    @Test
    void testGetNonExistingUser() {
        // Given
        String userId = "non-existing-user";
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> goalDao.getUser(userId));
    }

    @Test
    void testGetUserWithNullId() {
        assertThrows(IllegalArgumentException.class, () -> goalDao.getUser(null));
    }

    @Test
    void testGetUserWithEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> goalDao.getUser(""));
    }
}

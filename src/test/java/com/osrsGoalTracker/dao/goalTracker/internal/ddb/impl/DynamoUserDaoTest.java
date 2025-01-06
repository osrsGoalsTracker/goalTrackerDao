package com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;
import com.osrsGoalTracker.dao.goalTracker.exception.DuplicateUserException;
import com.osrsGoalTracker.dao.goalTracker.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

class DynamoUserDaoTest {
    private static final String TEST_TABLE_NAME = "test-table";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient mockDynamoDbClient;

    private DynamoUserDao dynamoUserDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dynamoUserDao = new DynamoUserDao(mockDynamoDbClient, TEST_TABLE_NAME);
    }

    @Test
    void testCreateUserSuccess() {
        // Given
        String email = "test@example.com";
        UserEntity userToCreate = UserEntity.builder()
                .email(email)
                .build();

        when(mockDynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(Collections.emptyList())
                        .build());

        when(mockDynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(null); // DynamoDB PutItem returns null on success

        // When
        UserEntity createdUser = dynamoUserDao.createUser(userToCreate);

        // Then
        assertNotNull(createdUser);
        assertNotNull(createdUser.getUserId());
        assertEquals(email, createdUser.getEmail());
        assertNotNull(createdUser.getCreatedAt());
        assertNotNull(createdUser.getUpdatedAt());
    }

    @Test
    void testCreateUserWithDuplicateEmailThrowsDuplicateUserException() {
        // Given
        String email = "test@example.com";
        UserEntity userToCreate = UserEntity.builder()
                .email(email)
                .build();

        Map<String, AttributeValue> existingItem = new HashMap<>();
        existingItem.put("email", AttributeValue.builder().s(email).build());

        when(mockDynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(Collections.singletonList(existingItem))
                        .build());

        // When/Then
        assertThrows(DuplicateUserException.class, () -> dynamoUserDao.createUser(userToCreate));
    }

    @Test
    void testCreateUserWithConcurrentCreationThrowsDuplicateUserException() {
        // Given
        String email = "test@example.com";
        UserEntity userToCreate = UserEntity.builder()
                .email(email)
                .build();

        when(mockDynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(Collections.emptyList())
                        .build());

        when(mockDynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.class);

        // When/Then
        assertThrows(DuplicateUserException.class, () -> dynamoUserDao.createUser(userToCreate));
    }

    @Test
    void testCreateUserWithNullUserThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dynamoUserDao.createUser(null));
    }

    @Test
    void testCreateUserWithEmptyEmailThrowsIllegalArgumentException() {
        UserEntity userWithEmptyEmail = UserEntity.builder()
                .email("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> dynamoUserDao.createUser(userWithEmptyEmail));
    }

    @Test
    void testGetUserSuccess() {
        // Given
        String userId = "test-user-id";
        String email = "test@example.com";
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(userId).build());
        item.put("email", AttributeValue.builder().s(email).build());
        item.put("createdAt", AttributeValue.builder().s(timestamp).build());
        item.put("updatedAt", AttributeValue.builder().s(timestamp).build());

        when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder()
                        .item(item)
                        .build());

        // When
        UserEntity retrievedUser = dynamoUserDao.getUser(userId);

        // Then
        assertNotNull(retrievedUser);
        assertEquals(userId, retrievedUser.getUserId());
        assertEquals(email, retrievedUser.getEmail());
        assertEquals(now.format(DATE_TIME_FORMATTER),
                retrievedUser.getCreatedAt().format(DATE_TIME_FORMATTER));
        assertEquals(now.format(DATE_TIME_FORMATTER),
                retrievedUser.getUpdatedAt().format(DATE_TIME_FORMATTER));
    }

    @Test
    void testGetUserWhenUserNotFoundThrowsResourceNotFoundException() {
        // Given
        String userId = "non-existent-user";

        when(mockDynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> dynamoUserDao.getUser(userId));
    }

    @Test
    void testGetUserWithNullUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dynamoUserDao.getUser(null));
    }

    @Test
    void testGetUserWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dynamoUserDao.getUser(""));
    }
}
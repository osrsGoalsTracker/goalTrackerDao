package com.osrsGoalTracker.user.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import com.osrsGoalTracker.shared.dao.exception.ResourceNotFoundException;
import com.osrsGoalTracker.shared.dao.util.SortKeyUtil;
import com.osrsGoalTracker.user.dao.entity.UserEntity;
import com.osrsGoalTracker.user.dao.exception.DuplicateUserException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@ExtendWith(MockitoExtension.class)
class DynamoUserDaoTest {
    private static final String TABLE_NAME = "test-table";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USER_ID = "test-user-id";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    @Captor
    private ArgumentCaptor<QueryRequest> queryRequestCaptor;

    @Captor
    private ArgumentCaptor<GetItemRequest> getItemRequestCaptor;

    private DynamoUserDao dynamoUserDao;

    @BeforeEach
    void setUp() {
        dynamoUserDao = new DynamoUserDao(dynamoDbClient, TABLE_NAME);
    }

    @Test
    void testCreateUserWithValidUserCreatesSuccessfully() {
        // Given
        UserEntity userToCreate = UserEntity.builder()
                .email(TEST_EMAIL)
                .build();

        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(Collections.emptyList()).build());

        // When
        UserEntity createdUser = dynamoUserDao.createUser(userToCreate);

        // Then
        verify(dynamoDbClient).query(queryRequestCaptor.capture());
        QueryRequest queryRequest = queryRequestCaptor.getValue();
        assertThat(queryRequest.tableName()).isEqualTo(TABLE_NAME);
        assertThat(queryRequest.indexName()).isEqualTo("email-sk-index");

        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        PutItemRequest putRequest = putItemRequestCaptor.getValue();
        assertThat(putRequest.tableName()).isEqualTo(TABLE_NAME);

        Map<String, AttributeValue> item = putRequest.item();
        assertThat(item.get("email").s()).isEqualTo(TEST_EMAIL);
        assertThat(item.get("sk").s()).isEqualTo(SortKeyUtil.getUserMetadataSortKey());

        assertThat(createdUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(createdUser.getUserId()).isNotNull();
        assertThat(createdUser.getCreatedAt()).isNotNull();
        assertThat(createdUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void testCreateUserWithExistingEmailThrowsDuplicateUserException() {
        // Given
        UserEntity userToCreate = UserEntity.builder()
                .email(TEST_EMAIL)
                .build();

        Map<String, AttributeValue> existingItem = Map.of(
                "email", AttributeValue.builder().s(TEST_EMAIL).build());

        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(Collections.singletonList(existingItem)).build());

        // When/Then
        assertThatThrownBy(() -> dynamoUserDao.createUser(userToCreate))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining(TEST_EMAIL);
    }

    @Test
    void testCreateUserWithNullUserThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoUserDao.createUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User entity cannot be null");
    }

    @Test
    void testCreateUserWithNullEmailThrowsIllegalArgumentException() {
        UserEntity userToCreate = UserEntity.builder().build();

        assertThatThrownBy(() -> dynamoUserDao.createUser(userToCreate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null or empty");
    }

    @Test
    void testCreateUserWithConcurrentCreationThrowsDuplicateUserException() {
        // Given
        UserEntity userToCreate = UserEntity.builder()
                .email(TEST_EMAIL)
                .build();

        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(Collections.emptyList()).build());
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.class);

        // When/Then
        assertThatThrownBy(() -> dynamoUserDao.createUser(userToCreate))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining(TEST_EMAIL);
    }

    @Test
    void testGetUserWithExistingUserReturnsUser() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.builder().s(TEST_USER_ID).build(),
                "email", AttributeValue.builder().s(TEST_EMAIL).build(),
                "createdAt", AttributeValue.builder().s(timestamp).build(),
                "updatedAt", AttributeValue.builder().s(timestamp).build());

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        // When
        UserEntity user = dynamoUserDao.getUser(TEST_USER_ID);

        // Then
        verify(dynamoDbClient).getItem(getItemRequestCaptor.capture());
        GetItemRequest getRequest = getItemRequestCaptor.getValue();
        assertThat(getRequest.tableName()).isEqualTo(TABLE_NAME);

        assertThat(user.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testGetUserWithNonExistentUserThrowsResourceNotFoundException() {
        // Given
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        // When/Then
        assertThatThrownBy(() -> dynamoUserDao.getUser(TEST_USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(TEST_USER_ID);
    }

    @Test
    void testGetUserWithNullUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoUserDao.getUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }

    @Test
    void testGetUserWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoUserDao.getUser("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }
}
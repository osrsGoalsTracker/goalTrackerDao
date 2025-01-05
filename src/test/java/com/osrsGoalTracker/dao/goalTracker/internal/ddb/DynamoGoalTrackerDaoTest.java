package com.osrsGoalTracker.dao.goalTracker.internal.ddb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;
import com.osrsGoalTracker.dao.goalTracker.exception.DuplicateUserException;
import com.osrsGoalTracker.dao.goalTracker.exception.ResourceNotFoundException;

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

@ExtendWith(MockitoExtension.class)
class DynamoGoalTrackerDaoTest {

    private static final String TEST_USER_ID = "testUser123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TEST_TABLE_NAME = "GoalsTable-dev";

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    @Captor
    private ArgumentCaptor<GetItemRequest> getItemRequestCaptor;

    private DynamoGoalTrackerDao goalsDao;

    @BeforeEach
    void setUp() {
        System.setProperty("GOALS_TABLE_NAME", TEST_TABLE_NAME);
        goalsDao = new DynamoGoalTrackerDao(dynamoDbClient);
    }

    @Test
    void testCreateUserSuccess() {
        // Given
        UserEntity userToCreate = UserEntity.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .build();

        // When
        UserEntity createdUser = goalsDao.createUser(userToCreate);

        // Then
        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        PutItemRequest capturedRequest = putItemRequestCaptor.getValue();

        assertThat(capturedRequest.tableName()).isEqualTo(TEST_TABLE_NAME);

        Map<String, AttributeValue> item = capturedRequest.item();
        assertThat(item.get("pk").s()).isEqualTo("USER#" + TEST_USER_ID);
        assertThat(item.get("sk").s()).isEqualTo("METADATA");
        assertThat(item.get("id").s()).isEqualTo(TEST_USER_ID);
        assertThat(item.get("email").s()).isEqualTo(TEST_EMAIL);
        assertThat(item.get("createdAt").s()).isNotEmpty();
        assertThat(item.get("updatedAt").s()).isNotEmpty();

        assertThat(createdUser.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(createdUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(createdUser.getCreatedAt()).isNotNull();
        assertThat(createdUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void testCreateUserDuplicateUser() {
        // Given
        UserEntity userToCreate = UserEntity.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .build();

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.class);

        // Then
        assertThatThrownBy(() -> goalsDao.createUser(userToCreate))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining(TEST_USER_ID);
    }

    @Test
    void testCreateUserNull() {
        assertThatThrownBy(() -> goalsDao.createUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void testGetUserSuccess() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.builder().s(TEST_USER_ID).build(),
                "email", AttributeValue.builder().s(TEST_EMAIL).build(),
                "createdAt", AttributeValue.builder().s(now.format(DATE_TIME_FORMATTER)).build(),
                "updatedAt", AttributeValue.builder().s(now.format(DATE_TIME_FORMATTER)).build());

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        // When
        UserEntity user = goalsDao.getUser(TEST_USER_ID);

        // Then
        verify(dynamoDbClient).getItem(getItemRequestCaptor.capture());
        GetItemRequest capturedRequest = getItemRequestCaptor.getValue();

        assertThat(capturedRequest.key().get("pk").s()).isEqualTo("USER#" + TEST_USER_ID);
        assertThat(capturedRequest.key().get("sk").s()).isEqualTo("METADATA");

        assertThat(user.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testGetUserNotFound() {
        // Given
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        // Then
        assertThatThrownBy(() -> goalsDao.getUser(TEST_USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(TEST_USER_ID);
    }

    @Test
    void testGetUserNull() {
        assertThatThrownBy(() -> goalsDao.getUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }
}

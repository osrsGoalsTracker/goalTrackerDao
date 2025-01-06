package com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import com.osrsGoalTracker.dao.goalTracker.entity.PlayerEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoPlayerDaoTest {
    private static final String TEST_TABLE_NAME = "test-table";

    @Mock
    private DynamoDbClient mockDynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    private DynamoPlayerDao dynamoPlayerDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dynamoPlayerDao = new DynamoPlayerDao(mockDynamoDbClient, TEST_TABLE_NAME);
    }

    @Test
    void testAddPlayerToUserSuccess() {
        // Given
        String userId = "test-user-id";
        String playerName = "TestPlayer123";

        when(mockDynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(null); // DynamoDB PutItem returns null on success

        // When
        PlayerEntity addedPlayer = dynamoPlayerDao.addPlayerToUser(userId, playerName);

        // Then
        assertNotNull(addedPlayer);
        assertEquals(playerName, addedPlayer.getName());
        assertEquals(userId, addedPlayer.getUserId());
        assertNotNull(addedPlayer.getCreatedAt());
        assertNotNull(addedPlayer.getUpdatedAt());

        // Verify DynamoDB interaction
        verify(mockDynamoDbClient).putItem(putItemRequestCaptor.capture());
        PutItemRequest capturedRequest = putItemRequestCaptor.getValue();

        assertEquals(TEST_TABLE_NAME, capturedRequest.tableName());

        // Verify item attributes
        assertEquals("USER#" + userId,
                capturedRequest.item().get("pk").s());
        assertNotNull(capturedRequest.item().get("sk").s());
        assertEquals(playerName,
                capturedRequest.item().get("name").s());
        assertNotNull(capturedRequest.item().get("createdAt").s());
        assertNotNull(capturedRequest.item().get("updatedAt").s());
    }

    @Test
    void testAddPlayerToUserWithNullUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoPlayerDao.addPlayerToUser(null, "TestPlayer"));
    }

    @Test
    void testAddPlayerToUserWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoPlayerDao.addPlayerToUser("", "TestPlayer"));
    }

    @Test
    void testAddPlayerToUserWithNullPlayerNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoPlayerDao.addPlayerToUser("test-user-id", null));
    }

    @Test
    void testAddPlayerToUserWithEmptyPlayerNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoPlayerDao.addPlayerToUser("test-user-id", ""));
    }

    @Test
    void testAddPlayerToUserVerifyTimestamps() {
        // Given
        String userId = "test-user-id";
        String playerName = "TestPlayer123";
        LocalDateTime beforeTest = LocalDateTime.now();

        when(mockDynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(null);

        // When
        PlayerEntity addedPlayer = dynamoPlayerDao.addPlayerToUser(userId, playerName);
        LocalDateTime afterTest = LocalDateTime.now();

        // Then
        assertNotNull(addedPlayer.getCreatedAt());
        assertNotNull(addedPlayer.getUpdatedAt());

        // Verify timestamps are within the test execution window
        LocalDateTime createdAt = addedPlayer.getCreatedAt();
        LocalDateTime updatedAt = addedPlayer.getUpdatedAt();

        assert (createdAt.isEqual(updatedAt)); // Should be set to same time
        assert (!createdAt.isBefore(beforeTest));
        assert (!createdAt.isAfter(afterTest));
    }
}
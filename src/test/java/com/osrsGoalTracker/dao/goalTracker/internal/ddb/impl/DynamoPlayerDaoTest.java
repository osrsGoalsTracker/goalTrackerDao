package com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.osrsGoalTracker.dao.goalTracker.entity.PlayerEntity;

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

class DynamoPlayerDaoTest {
    private static final String TEST_TABLE_NAME = "test-table";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient mockDynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    @Captor
    private ArgumentCaptor<QueryRequest> queryRequestCaptor;

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

    @Test
    void testGetPlayersForUserSuccess() {
        // Given
        String userId = "test-user-id";
        String player1Name = "TestPlayer1";
        String player2Name = "TestPlayer2";
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DATE_TIME_FORMATTER);

        Map<String, AttributeValue> player1 = new HashMap<>();
        player1.put("name", AttributeValue.builder().s(player1Name).build());
        player1.put("createdAt", AttributeValue.builder().s(timestamp).build());
        player1.put("updatedAt", AttributeValue.builder().s(timestamp).build());

        Map<String, AttributeValue> player2 = new HashMap<>();
        player2.put("name", AttributeValue.builder().s(player2Name).build());
        player2.put("createdAt", AttributeValue.builder().s(timestamp).build());
        player2.put("updatedAt", AttributeValue.builder().s(timestamp).build());

        when(mockDynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(Arrays.asList(player1, player2))
                        .build());

        // When
        List<PlayerEntity> players = dynamoPlayerDao.getPlayersForUser(userId);

        // Then
        assertNotNull(players);
        assertEquals(2, players.size());

        // Verify first player
        PlayerEntity firstPlayer = players.get(0);
        assertEquals(player1Name, firstPlayer.getName());
        assertEquals(userId, firstPlayer.getUserId());
        assertEquals(now.format(DATE_TIME_FORMATTER),
                firstPlayer.getCreatedAt().format(DATE_TIME_FORMATTER));
        assertEquals(now.format(DATE_TIME_FORMATTER),
                firstPlayer.getUpdatedAt().format(DATE_TIME_FORMATTER));

        // Verify second player
        PlayerEntity secondPlayer = players.get(1);
        assertEquals(player2Name, secondPlayer.getName());
        assertEquals(userId, secondPlayer.getUserId());
        assertEquals(now.format(DATE_TIME_FORMATTER),
                secondPlayer.getCreatedAt().format(DATE_TIME_FORMATTER));
        assertEquals(now.format(DATE_TIME_FORMATTER),
                secondPlayer.getUpdatedAt().format(DATE_TIME_FORMATTER));

        // Verify DynamoDB query
        verify(mockDynamoDbClient).query(queryRequestCaptor.capture());
        QueryRequest capturedRequest = queryRequestCaptor.getValue();
        assertEquals(TEST_TABLE_NAME, capturedRequest.tableName());
        assertTrue(capturedRequest.keyConditionExpression().contains("begins_with(sk, :sk_prefix)"));
    }

    @Test
    void testGetPlayersForUserWhenNoPlayersExist() {
        // Given
        String userId = "test-user-id";

        when(mockDynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(Collections.emptyList())
                        .build());

        // When
        List<PlayerEntity> players = dynamoPlayerDao.getPlayersForUser(userId);

        // Then
        assertNotNull(players);
        assertTrue(players.isEmpty());
    }

    @Test
    void testGetPlayersForUserWithNullUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoPlayerDao.getPlayersForUser(null));
    }

    @Test
    void testGetPlayersForUserWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dynamoPlayerDao.getPlayersForUser(""));
    }
}
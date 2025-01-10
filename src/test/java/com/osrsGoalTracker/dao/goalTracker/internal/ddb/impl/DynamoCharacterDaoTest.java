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

import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;

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

class DynamoCharacterDaoTest {
        private static final String TEST_TABLE_NAME = "test-table";
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

        @Mock
        private DynamoDbClient mockDynamoDbClient;

        @Captor
        private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

        @Captor
        private ArgumentCaptor<QueryRequest> queryRequestCaptor;

        private DynamoCharacterDao dynamoCharacterDao;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                dynamoCharacterDao = new DynamoCharacterDao(mockDynamoDbClient, TEST_TABLE_NAME);
        }

        @Test
        void testAddCharacterToUserSuccess() {
                // Given
                String userId = "test-user-id";
                String characterName = "TestCharacter123";

                when(mockDynamoDbClient.putItem(any(PutItemRequest.class)))
                                .thenReturn(null); // DynamoDB PutItem returns null on success

                // When
                CharacterEntity addedCharacter = dynamoCharacterDao.addCharacterToUser(userId, characterName);

                // Then
                assertNotNull(addedCharacter);
                assertEquals(characterName, addedCharacter.getName());
                assertEquals(userId, addedCharacter.getUserId());
                assertNotNull(addedCharacter.getCreatedAt());
                assertNotNull(addedCharacter.getUpdatedAt());

                // Verify DynamoDB interaction
                verify(mockDynamoDbClient).putItem(putItemRequestCaptor.capture());
                PutItemRequest capturedRequest = putItemRequestCaptor.getValue();

                assertEquals(TEST_TABLE_NAME, capturedRequest.tableName());

                // Verify item attributes
                assertEquals("USER#" + userId,
                                capturedRequest.item().get("pk").s());
                assertNotNull(capturedRequest.item().get("sk").s());
                assertEquals(characterName,
                                capturedRequest.item().get("name").s());
                assertNotNull(capturedRequest.item().get("createdAt").s());
                assertNotNull(capturedRequest.item().get("updatedAt").s());
        }

        @Test
        void testAddCharacterToUserWithNullUserIdThrowsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class,
                                () -> dynamoCharacterDao.addCharacterToUser(null, "TestCharacter"));
        }

        @Test
        void testAddCharacterToUserWithEmptyUserIdThrowsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class,
                                () -> dynamoCharacterDao.addCharacterToUser("", "TestCharacter"));
        }

        @Test
        void testAddCharacterToUserWithNullCharacterNameThrowsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class,
                                () -> dynamoCharacterDao.addCharacterToUser("test-user-id", null));
        }

        @Test
        void testAddCharacterToUserWithEmptyCharacterNameThrowsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class,
                                () -> dynamoCharacterDao.addCharacterToUser("test-user-id", ""));
        }

        @Test
        void testAddCharacterToUserVerifyTimestamps() {
                // Given
                String userId = "test-user-id";
                String characterName = "TestCharacter123";
                LocalDateTime beforeTest = LocalDateTime.now();

                when(mockDynamoDbClient.putItem(any(PutItemRequest.class)))
                                .thenReturn(null);

                // When
                CharacterEntity addedCharacter = dynamoCharacterDao.addCharacterToUser(userId, characterName);
                LocalDateTime afterTest = LocalDateTime.now();

                // Then
                assertNotNull(addedCharacter.getCreatedAt());
                assertNotNull(addedCharacter.getUpdatedAt());

                // Verify timestamps are within the test execution window
                LocalDateTime createdAt = addedCharacter.getCreatedAt();
                LocalDateTime updatedAt = addedCharacter.getUpdatedAt();

                assert (createdAt.isEqual(updatedAt)); // Should be set to same time
                assert (!createdAt.isBefore(beforeTest));
                assert (!createdAt.isAfter(afterTest));
        }

        @Test
        void testGetCharactersForUserSuccess() {
                // Given
                String userId = "test-user-id";
                String character1Name = "TestCharacter1";
                String character2Name = "TestCharacter2";
                LocalDateTime now = LocalDateTime.now();
                String timestamp = now.format(DATE_TIME_FORMATTER);

                Map<String, AttributeValue> character1 = new HashMap<>();
                character1.put("name", AttributeValue.builder().s(character1Name).build());
                character1.put("createdAt", AttributeValue.builder().s(timestamp).build());
                character1.put("updatedAt", AttributeValue.builder().s(timestamp).build());

                Map<String, AttributeValue> character2 = new HashMap<>();
                character2.put("name", AttributeValue.builder().s(character2Name).build());
                character2.put("createdAt", AttributeValue.builder().s(timestamp).build());
                character2.put("updatedAt", AttributeValue.builder().s(timestamp).build());

                when(mockDynamoDbClient.query(any(QueryRequest.class)))
                                .thenReturn(QueryResponse.builder()
                                                .items(Arrays.asList(character1, character2))
                                                .build());

                // When
                List<CharacterEntity> characters = dynamoCharacterDao.getCharactersForUser(userId);

                // Then
                assertNotNull(characters);
                assertEquals(2, characters.size());

                // Verify first character
                CharacterEntity firstCharacter = characters.get(0);
                assertEquals(character1Name, firstCharacter.getName());
                assertEquals(userId, firstCharacter.getUserId());
                assertEquals(now.format(DATE_TIME_FORMATTER),
                                firstCharacter.getCreatedAt().format(DATE_TIME_FORMATTER));
                assertEquals(now.format(DATE_TIME_FORMATTER),
                                firstCharacter.getUpdatedAt().format(DATE_TIME_FORMATTER));

                // Verify second character
                CharacterEntity secondCharacter = characters.get(1);
                assertEquals(character2Name, secondCharacter.getName());
                assertEquals(userId, secondCharacter.getUserId());
                assertEquals(now.format(DATE_TIME_FORMATTER),
                                secondCharacter.getCreatedAt().format(DATE_TIME_FORMATTER));
                assertEquals(now.format(DATE_TIME_FORMATTER),
                                secondCharacter.getUpdatedAt().format(DATE_TIME_FORMATTER));

                // Verify DynamoDB query
                verify(mockDynamoDbClient).query(queryRequestCaptor.capture());
                QueryRequest capturedRequest = queryRequestCaptor.getValue();
                assertEquals(TEST_TABLE_NAME, capturedRequest.tableName());
                assertTrue(capturedRequest.keyConditionExpression().contains("begins_with(sk, :sk_prefix)"));
        }

        @Test
        void testGetCharactersForUserWhenNoCharactersExist() {
                // Given
                String userId = "test-user-id";

                when(mockDynamoDbClient.query(any(QueryRequest.class)))
                                .thenReturn(QueryResponse.builder()
                                                .items(Collections.emptyList())
                                                .build());

                // When
                List<CharacterEntity> characters = dynamoCharacterDao.getCharactersForUser(userId);

                // Then
                assertNotNull(characters);
                assertTrue(characters.isEmpty());
        }

        @Test
        void testGetCharactersForUserWithNullUserIdThrowsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class,
                                () -> dynamoCharacterDao.getCharactersForUser(null));
        }

        @Test
        void testGetCharactersForUserWithEmptyUserIdThrowsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class,
                                () -> dynamoCharacterDao.getCharactersForUser(""));
        }
}
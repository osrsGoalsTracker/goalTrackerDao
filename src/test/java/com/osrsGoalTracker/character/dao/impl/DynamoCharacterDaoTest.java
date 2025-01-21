package com.osrsGoalTracker.character.dao.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import com.osrsGoalTracker.character.dao.entity.CharacterEntity;
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
class DynamoCharacterDaoTest {
    private static final String TABLE_NAME = "test-table";
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_CHARACTER_NAME = "test-character";

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Captor
    private ArgumentCaptor<PutItemRequest> putItemRequestCaptor;

    @Captor
    private ArgumentCaptor<QueryRequest> queryRequestCaptor;

    private DynamoCharacterDao dynamoCharacterDao;

    @BeforeEach
    void setUp() {
        dynamoCharacterDao = new DynamoCharacterDao(dynamoDbClient, TABLE_NAME);
    }

    @Test
    void testAddCharacterToUserWithValidInputCreatesSuccessfully() {
        // When
        CharacterEntity createdCharacter = dynamoCharacterDao.addCharacterToUser(TEST_USER_ID, TEST_CHARACTER_NAME);

        // Then
        verify(dynamoDbClient).putItem(putItemRequestCaptor.capture());
        PutItemRequest putRequest = putItemRequestCaptor.getValue();
        assertThat(putRequest.tableName()).isEqualTo(TABLE_NAME);

        Map<String, AttributeValue> item = putRequest.item();
        assertThat(item.get("characterName").s()).isEqualTo(TEST_CHARACTER_NAME);
        assertThat(item.get("sk").s()).isEqualTo(SortKeyUtil.getCharacterMetadataSortKey(TEST_CHARACTER_NAME));

        assertThat(createdCharacter.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(createdCharacter.getName()).isEqualTo(TEST_CHARACTER_NAME);
        assertThat(createdCharacter.getCreatedAt()).isNotNull();
        assertThat(createdCharacter.getUpdatedAt()).isNotNull();
    }

    @Test
    void testGetCharactersForUserWithValidUserIdReturnsCharacters() {
        // Given
        Instant now = Instant.now();

        Map<String, AttributeValue> character1 = Map.of(
                "userId", AttributeValue.builder().s(TEST_USER_ID).build(),
                "characterName", AttributeValue.builder().s(TEST_CHARACTER_NAME).build(),
                "createdAt", AttributeValue.builder().s(now.toString()).build(),
                "updatedAt", AttributeValue.builder().s(now.toString()).build());

        Map<String, AttributeValue> character2 = Map.of(
                "userId", AttributeValue.builder().s(TEST_USER_ID).build(),
                "characterName", AttributeValue.builder().s("another-character").build(),
                "createdAt", AttributeValue.builder().s(now.toString()).build(),
                "updatedAt", AttributeValue.builder().s(now.toString()).build());

        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(Arrays.asList(character1, character2)).build());

        // When
        var characters = dynamoCharacterDao.getCharactersForUser(TEST_USER_ID);

        // Then
        verify(dynamoDbClient).query(queryRequestCaptor.capture());
        QueryRequest queryRequest = queryRequestCaptor.getValue();
        assertThat(queryRequest.tableName()).isEqualTo(TABLE_NAME);
        assertThat(queryRequest.keyConditionExpression()).contains("begins_with(sk, :sk_prefix)");

        assertThat(characters).hasSize(2);

        CharacterEntity firstCharacter = characters.get(0);
        assertThat(firstCharacter.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(firstCharacter.getName()).isEqualTo(TEST_CHARACTER_NAME);
        assertThat(firstCharacter.getCreatedAt()).isEqualTo(now);
        assertThat(firstCharacter.getUpdatedAt()).isEqualTo(now);

        CharacterEntity secondCharacter = characters.get(1);
        assertThat(secondCharacter.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(secondCharacter.getName()).isEqualTo("another-character");
        assertThat(secondCharacter.getCreatedAt()).isEqualTo(now);
        assertThat(secondCharacter.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testAddCharacterToUserWithNullUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoCharacterDao.addCharacterToUser(null, TEST_CHARACTER_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }

    @Test
    void testAddCharacterToUserWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoCharacterDao.addCharacterToUser("  ", TEST_CHARACTER_NAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }

    @Test
    void testAddCharacterToUserWithNullCharacterNameThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoCharacterDao.addCharacterToUser(TEST_USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Character name cannot be null or empty");
    }

    @Test
    void testAddCharacterToUserWithEmptyCharacterNameThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoCharacterDao.addCharacterToUser(TEST_USER_ID, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Character name cannot be null or empty");
    }

    @Test
    void testGetCharactersForUserWithNullUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoCharacterDao.getCharactersForUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }

    @Test
    void testGetCharactersForUserWithEmptyUserIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> dynamoCharacterDao.getCharactersForUser("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId cannot be null or empty");
    }
}
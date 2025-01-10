package com.osrsGoalTracker.dao.goalTracker.internal.ddb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.util.SortKeyUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoGoalTrackerDaoTest {
        private static final String TEST_USER_ID = "testUserId";
        private static final String TEST_PLAYER_NAME = "testCharacter";
        private static final String TEST_TABLE_NAME = "GoalsTable-test";

        @Mock
        private DynamoDbClient dynamoDbClient;

        private DynamoGoalTrackerDao goalsDao;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                goalsDao = new DynamoGoalTrackerDao(dynamoDbClient, TEST_TABLE_NAME);
        }

        @Test
        void testAddCharacterToUserSuccess() {
                CharacterEntity result = goalsDao.addCharacterToUser(TEST_USER_ID, TEST_PLAYER_NAME);

                assertThat(result.getName()).isEqualTo(TEST_PLAYER_NAME);
                assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
                assertThat(result.getCreatedAt()).isNotNull();
                assertThat(result.getUpdatedAt()).isNotNull();

                ArgumentCaptor<PutItemRequest> putItemCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
                verify(dynamoDbClient).putItem(putItemCaptor.capture());

                PutItemRequest putItemRequest = putItemCaptor.getValue();
                Map<String, AttributeValue> item = putItemRequest.item();
                assertThat(item.get("pk").s()).isEqualTo("USER#" + TEST_USER_ID);
                assertThat(item.get("sk").s()).isEqualTo(SortKeyUtil.getCharacterMetadataSortKey(TEST_PLAYER_NAME));
                assertThat(item.get("name").s()).isEqualTo(TEST_PLAYER_NAME);
                assertThat(item.get("createdAt").s()).isNotEmpty();
                assertThat(item.get("updatedAt").s()).isNotEmpty();
        }

        @Test
        void testAddCharacterToUserNullUserId() {
                assertThatThrownBy(() -> goalsDao.addCharacterToUser(null, TEST_PLAYER_NAME))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("cannot be null or empty");
        }

        @Test
        void testAddCharacterToUserEmptyUserId() {
                assertThatThrownBy(() -> goalsDao.addCharacterToUser("", TEST_PLAYER_NAME))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("cannot be null or empty");
        }

        @Test
        void testAddCharacterToUserNullCharacterName() {
                assertThatThrownBy(() -> goalsDao.addCharacterToUser(TEST_USER_ID, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("cannot be null or empty");
        }

        @Test
        void testAddCharacterToUserEmptyCharacterName() {
                assertThatThrownBy(() -> goalsDao.addCharacterToUser(TEST_USER_ID, ""))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("cannot be null or empty");
        }
}
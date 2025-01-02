package com.osrs.goal.dao;

import com.osrs.goal.dao.entity.RsnEntity;
import com.osrs.goal.dao.entity.UserEntity;
import com.osrs.goal.dao.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoGoalDaoTest {
    private static final String USER_ID = "testUser";
    private static final String EMAIL = "test@example.com";
    private static final String RSN = "testRsn";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoGoalDao goalDao;

    @BeforeEach
    void setUp() {
        goalDao = new DynamoGoalDao(dynamoDbClient);
    }

    @Test
    void shouldReturnUserEntityWhenUserExists() {
        // Arrange
        Map<String, AttributeValue> item = Map.of(
                "email", AttributeValue.builder().s(EMAIL).build(),
                "createdAt", AttributeValue.builder().s(NOW.format(FORMATTER)).build(),
                "updatedAt", AttributeValue.builder().s(NOW.format(FORMATTER)).build());

        GetItemResponse response = GetItemResponse.builder()
                .item(item)
                .build();

        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        // Act
        UserEntity result = goalDao.getUser(USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        GetItemResponse response = GetItemResponse.builder().build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> goalDao.getUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(USER_ID);
    }

    @Test
    void shouldReturnRsnListWhenRsnsExist() {
        // Arrange
        Map<String, AttributeValue> item = Map.of(
                "rsn", AttributeValue.builder().s(RSN).build(),
                "createdAt", AttributeValue.builder().s(NOW.format(FORMATTER)).build(),
                "updatedAt", AttributeValue.builder().s(NOW.format(FORMATTER)).build());

        QueryResponse response = QueryResponse.builder()
                .items(List.of(item))
                .build();

        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

        // Act
        List<RsnEntity> results = goalDao.getRsnsForUser(USER_ID);

        // Assert
        assertThat(results).hasSize(1);
        RsnEntity result = results.get(0);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getRsn()).isEqualTo(RSN);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        assertThat(result.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldReturnEmptyListWhenNoRsnsExist() {
        // Arrange
        QueryResponse response = QueryResponse.builder()
                .items(List.of())
                .build();

        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

        // Act
        List<RsnEntity> results = goalDao.getRsnsForUser(USER_ID);

        // Assert
        assertThat(results).isEmpty();
    }
}

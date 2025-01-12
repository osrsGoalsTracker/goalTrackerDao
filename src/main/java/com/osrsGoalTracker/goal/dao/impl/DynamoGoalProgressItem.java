package com.osrsGoalTracker.goal.dao.impl;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Represents a goal progress record in the DynamoDB table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DynamoGoalProgressItem {
    private String pk;
    private String sk;
    private String userId;
    private String characterName;
    private String goalId;
    private Long progressValue;
    private Instant timestamp;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("characterName")
    public String getCharacterName() {
        return characterName;
    }

    @DynamoDbAttribute("goalId")
    public String getGoalId() {
        return goalId;
    }

    @DynamoDbAttribute("progressValue")
    public Long getProgressValue() {
        return progressValue;
    }

    @DynamoDbAttribute("timestamp")
    public Instant getTimestamp() {
        return timestamp;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
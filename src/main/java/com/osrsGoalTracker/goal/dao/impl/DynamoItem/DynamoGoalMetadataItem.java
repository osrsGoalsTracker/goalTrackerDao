package com.osrsGoalTracker.goal.dao.impl.DynamoItem;

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
 * Represents a goal metadata record in the DynamoDB table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DynamoGoalMetadataItem {
    private String pk;
    private String sk;
    private String userId;
    private String characterName;
    private String goalId;
    private String targetAttribute;
    private String targetType;
    private Long targetValue;
    private Instant targetDate;
    private String notificationChannelType;
    private String frequency;
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

    @DynamoDbAttribute("targetAttribute")
    public String getTargetAttribute() {
        return targetAttribute;
    }

    @DynamoDbAttribute("targetType")
    public String getTargetType() {
        return targetType;
    }

    @DynamoDbAttribute("targetValue")
    public Long getTargetValue() {
        return targetValue;
    }

    @DynamoDbAttribute("targetDate")
    public Instant getTargetDate() {
        return targetDate;
    }

    @DynamoDbAttribute("notificationChannelType")
    public String getNotificationChannelType() {
        return notificationChannelType;
    }

    @DynamoDbAttribute("frequency")
    public String getFrequency() {
        return frequency;
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
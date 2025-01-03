package com.osrsGoalTracker.goals.dao.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a RuneScape username (RSN) associated with a user.
 * Contains RSN-specific metadata.
 * 
 * This class extends AbstractEntity to inherit common fields:
 * - userId: The user who owns this RSN
 * - createdAt: When the RSN was added
 * - updatedAt: When the RSN was last updated
 * 
 * In DynamoDB, RSN entries are stored with:
 * - Partition Key: USER#userId
 * - Sort Key: RSN#METADATA#rsn
 * 
 * Lombok annotations:
 * - @Getter: Generates getters for all fields
 * - @SuperBuilder: Enables builder pattern with inheritance support
 */
@Getter
@SuperBuilder
public class RsnEntity extends AbstractEntity {
    /**
     * The RuneScape username.
     * This is stored in the 'rsn' attribute in DynamoDB and is also part of the
     * sort key.
     */
    private final String rsn;

    /**
     * Constructor for RsnEntity.
     * Delegates to superclass constructor for common fields.
     *
     * @param userId    The unique identifier of the user
     * @param rsn       The RuneScape username
     * @param createdAt When the RSN was added
     * @param updatedAt When the RSN was last updated
     */
    public RsnEntity(String userId, String rsn, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(userId, createdAt, updatedAt);
        this.rsn = rsn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RsnEntity(userId=")
                .append(getUserId())
                .append(", rsn=")
                .append(rsn)
                .append(", createdAt=")
                .append(getCreatedAt())
                .append(", updatedAt=")
                .append(getUpdatedAt())
                .append(")");
        return sb.toString();
    }
}


package com.osrsGoalTracker.goalsTracker.dao.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for all entities in the system.
 * Contains common fields that are shared across all entities.
 * 
 * This class uses the @SuperBuilder annotation to enable
 * the builder pattern with inheritance support.
 * 
 * Common fields:
 * - userId: Links the entity to a specific user
 * - createdAt: Timestamp when the entity was created
 * - updatedAt: Timestamp when the entity was last updated
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEntity {
    /**
     * The unique identifier of the user who owns this entity.
     * Used as part of the partition key in DynamoDB.
     */
    private String userId;

    /**
     * The timestamp when this entity was created.
     * Stored in ISO-8601 format in DynamoDB.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when this entity was last updated.
     * Stored in ISO-8601 format in DynamoDB.
     */
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
                .append("(userId=")
                .append(userId)
                .append(", createdAt=")
                .append(createdAt)
                .append(", updatedAt=")
                .append(updatedAt)
                .append(")");
        return sb.toString();
    }
}

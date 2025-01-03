package com.osrsGoalTracker.ddb.dao.goals.entity;

import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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
@SuperBuilder
public abstract class AbstractEntity {
    /**
     * The unique identifier of the user who owns this entity.
     * Used as part of the partition key in DynamoDB.
     */
    private final String userId;

    /**
     * The timestamp when this entity was created.
     * Stored in ISO-8601 format in DynamoDB.
     */
    private final LocalDateTime createdAt;

    /**
     * The timestamp when this entity was last updated.
     * Stored in ISO-8601 format in DynamoDB.
     */
    private final LocalDateTime updatedAt;

    /**
     * Protected constructor for AbstractEntity.
     *
     * @param userId    The unique identifier of the user
     * @param createdAt When the entity was created
     * @param updatedAt When the entity was last updated
     */
    protected AbstractEntity(String userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return The last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

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


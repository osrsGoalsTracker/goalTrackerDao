package com.osrsGoalTracker.dao.goalTracker.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for all entities in the system.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEntity {
    private String userId;
    private LocalDateTime createdAt;
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
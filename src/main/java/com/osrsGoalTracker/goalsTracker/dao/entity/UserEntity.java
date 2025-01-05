package com.osrsGoalTracker.goalsTracker.dao.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a user in the system.
 * Extends AbstractEntity to inherit common fields like userId, createdAt, and
 * updatedAt.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserEntity extends AbstractEntity {
    private final String email;
}

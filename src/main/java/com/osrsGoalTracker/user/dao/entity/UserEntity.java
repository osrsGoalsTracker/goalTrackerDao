package com.osrsGoalTracker.user.dao.entity;

import com.osrsGoalTracker.shared.dao.entity.AbstractEntity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a user in the system.
 * Extends AbstractEntity to inherit common fields like userId, createdAt, and
 * updatedAt.
 */
@Getter
@SuperBuilder
public class UserEntity extends AbstractEntity {
    private String email;
}
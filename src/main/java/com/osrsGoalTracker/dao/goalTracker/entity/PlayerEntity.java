package com.osrsGoalTracker.dao.goalTracker.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a RuneScape player in the system.
 * Extends AbstractEntity to inherit common fields like createdAt and updatedAt.
 */
@Getter
@SuperBuilder
public class PlayerEntity extends AbstractEntity {
    private String name;
}
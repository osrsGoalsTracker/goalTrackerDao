package com.osrsGoalTracker.goal.dao.entity;

import java.time.Instant;

import com.osrsGoalTracker.shared.dao.entity.AbstractEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents the progress of a goal.
 */
@Getter
@Setter
@SuperBuilder
public class GoalProgressEntity extends AbstractEntity {
    private String characterName;
    private String goalId;
    private Long progressValue;
    private Instant createdAt;
}
package com.osrsGoalTracker.goal.dao.entity;

import java.time.Instant;

import com.osrsGoalTracker.shared.dao.entity.AbstractEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a goal.
 */
@Getter
@Setter
@SuperBuilder
public class GoalEntity extends AbstractEntity {
    private String userId;
    private String characterName;
    private String goalId;
    private String targetAttribute;
    private String targetType;
    private Long targetValue;
    private Instant targetDate;
    private String notificationChannelType;
    private String frequency;
}
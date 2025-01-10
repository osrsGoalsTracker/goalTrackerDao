package com.osrsGoalTracker.notificationChannel.dao.entity;

import com.osrsGoalTracker.dao.goalTracker.entity.AbstractEntity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a notification channel for a user.
 * A notification channel can be SMS, Discord, etc.
 */
@Getter
@SuperBuilder
public class NotificationChannelEntity extends AbstractEntity {
    private final String channelType;
    private final String identifier;
    private final boolean isActive;
}
package com.osrsGoalTracker.notificationChannel.dao;

import java.util.List;

import com.osrsGoalTracker.notificationChannel.dao.entity.NotificationChannelEntity;

/**
 * Interface for accessing and modifying notification data.
 */
public interface NotificationChannelDao {

    /**
     * Creates a new notification channel for a user.
     *
     * @param userId  The ID of the user to create the channel for
     * @param channel The notification channel entity to create
     * @return The created notification channel entity with timestamps
     * @throws IllegalArgumentException If userId is null/empty or channel
     *                                  validation fails
     */
    NotificationChannelEntity createNotificationChannel(String userId, NotificationChannelEntity channel);

    /**
     * Retrieves all notification channels for a user.
     *
     * @param userId The ID of the user to get channels for
     * @return List of notification channel entities associated with the user
     * @throws IllegalArgumentException If userId is null or empty
     */
    List<NotificationChannelEntity> getNotificationChannels(String userId);
}
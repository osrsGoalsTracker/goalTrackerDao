package com.osrsGoalTracker.dao.goalTracker;

import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

/**
 * Interface for the GoalTrackerDao.
 */
public interface GoalTrackerDao {
    /**
     * Creates a new user in the database.
     *
     * @param user the user to create
     * @return the created user
     */
    UserEntity createUser(UserEntity user);

    /**
     * Retrieves a user from the database.
     *
     * @param userId the ID of the user to retrieve
     * @return the retrieved user
     */
    UserEntity getUser(String userId);
}
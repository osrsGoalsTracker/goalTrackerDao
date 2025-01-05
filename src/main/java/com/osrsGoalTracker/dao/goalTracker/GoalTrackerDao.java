package com.osrsGoalTracker.dao.goalTracker;

import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

/**
 * Interface for the GoalTrackerDao.
 */
public interface GoalTrackerDao {
    /**
     * Creates a new user in the database. If a user with the given email already
     * exists, returns the existing user entity. Otherwise, generates a new UUID
     * for the user ID, maps the incoming entity to a new entity, and creates the
     * new user.
     *
     * @param user the user to create
     * @return the created or existing user
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
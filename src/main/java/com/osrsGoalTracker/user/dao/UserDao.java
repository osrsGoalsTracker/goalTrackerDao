package com.osrsGoalTracker.user.dao;

import com.osrsGoalTracker.user.dao.entity.UserEntity;

/**
 * Interface for interacting with the user data store.
 */
public interface UserDao {
    /**
     * Creates a new user in the database.
     *
     * @param user The user entity to create
     * @return The created user entity with generated ID and timestamps
     * @throws IllegalArgumentException If user is null or email is null/empty
     */
    UserEntity createUser(UserEntity user);

    /**
     * Retrieves a user from the database.
     *
     * @param userId The ID of the user to retrieve
     * @return The user entity
     * @throws IllegalArgumentException If userId is null or empty
     */
    UserEntity getUser(String userId);
}

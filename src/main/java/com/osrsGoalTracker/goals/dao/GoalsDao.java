package com.osrsGoalTracker.goals.dao;

import com.osrsGoalTracker.goals.dao.entity.UserEntity;
import com.osrsGoalTracker.goals.dao.exception.DuplicateUserException;
import com.osrsGoalTracker.goals.dao.exception.ResourceNotFoundException;

/**
 * Interface for accessing and managing user goals data.
 */
public interface GoalsDao {
    /**
     * Creates a new user.
     *
     * @param user The user entity to create
     * @return The created user entity with timestamps
     * @throws IllegalArgumentException if user is null or has invalid fields
     * @throws DuplicateUserException   if a user with the same ID already exists
     */
    UserEntity createUser(UserEntity user);

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user entity
     * @throws IllegalArgumentException  if userId is null or empty
     * @throws ResourceNotFoundException if the user is not found
     */
    UserEntity getUser(String userId);
}

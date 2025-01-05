package com.osrsGoalTracker.goalsTracker.dao;

import com.osrsGoalTracker.goalsTracker.dao.entity.UserEntity;
import com.osrsGoalTracker.goalsTracker.dao.exception.ResourceNotFoundException;

/**
 * Interface for accessing and managing goals tracking data.
 * Provides methods for CRUD operations on user and goal data.
 */
public interface GoalsTrackerDao {
    /**
     * Creates a new user in the goals tracking system.
     *
     * @param user The user entity to create
     * @return The created user entity
     */
    UserEntity createUser(UserEntity user);

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId The unique ID of the user
     * @return The user entity
     * @throws ResourceNotFoundException If the user is not found
     */
    UserEntity getUser(String userId);
}

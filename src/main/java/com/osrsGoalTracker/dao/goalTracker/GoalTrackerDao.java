package com.osrsGoalTracker.dao.goalTracker;

import java.util.List;

import com.osrsGoalTracker.dao.goalTracker.entity.PlayerEntity;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

/**
 * Interface for accessing and modifying goal tracker data.
 */
public interface GoalTrackerDao {
    /**
     * Creates a new user.
     *
     * @param user The user entity to create
     * @return The created user entity with generated ID and timestamps
     */
    UserEntity createUser(UserEntity user);

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve
     * @return The user entity
     */
    UserEntity getUser(String userId);

    /**
     * Adds a RuneScape player to a user's account.
     *
     * @param userId     The ID of the user to add the player to
     * @param playerName The name of the RuneScape player to add
     * @return The created player entity
     */
    PlayerEntity addPlayerToUser(String userId, String playerName);

    /**
     * Retrieves all players associated with a user.
     *
     * @param userId The ID of the user to get players for
     * @return List of player entities associated with the user
     */
    List<PlayerEntity> getPlayersForUser(String userId);
}
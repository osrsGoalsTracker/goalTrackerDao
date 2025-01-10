package com.osrsGoalTracker.dao.goalTracker;

import java.util.List;

import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;
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
     * Adds a RuneScape character to a user's account.
     *
     * @param userId        The ID of the user to add the character to
     * @param characterName The name of the RuneScape character to add
     * @return The created character entity
     */
    CharacterEntity addCharacterToUser(String userId, String characterName);

    /**
     * Retrieves all characters associated with a user.
     *
     * @param userId The ID of the user to get characters for
     * @return List of character entities associated with the user
     */
    List<CharacterEntity> getCharactersForUser(String userId);
}
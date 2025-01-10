package com.osrsGoalTracker.dao.goalTracker;

import java.util.List;

import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

/**
 * Interface for interacting with the goal tracker data store.
 */
public interface GoalTrackerDao {
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

    /**
     * Adds a RuneScape character to a user's account.
     *
     * @param userId        The ID of the user to add the character to
     * @param characterName The name of the RuneScape character to add
     * @return The created character entity
     * @throws IllegalArgumentException If userId or characterName is null or empty
     */
    CharacterEntity addCharacterToUser(String userId, String characterName);

    /**
     * Retrieves all characters associated with a user.
     *
     * @param userId The ID of the user to get characters for
     * @return List of character entities associated with the user
     * @throws IllegalArgumentException If userId is null or empty
     */
    List<CharacterEntity> getCharactersForUser(String userId);
}
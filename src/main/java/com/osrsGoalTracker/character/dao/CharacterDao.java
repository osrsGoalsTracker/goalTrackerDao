package com.osrsGoalTracker.character.dao;

import java.util.List;

import com.osrsGoalTracker.character.dao.entity.CharacterEntity;

/**
 * Interface for interacting with the character data store.
 */
public interface CharacterDao {
    
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

package com.osrs.goal.dao;

import com.osrs.goal.dao.entity.RsnEntity;
import com.osrs.goal.dao.entity.UserEntity;

import java.util.List;

/**
 * Data Access Object interface for goal tracking operations.
 * Provides methods to interact with user and RSN data.
 * 
 * This interface abstracts the underlying data store implementation,
 * allowing clients to work with domain objects without knowledge of:
 * - The database being used (DynamoDB)
 * - The table structure
 * - The key schema
 * - The attribute names
 * 
 * Thread Safety:
 * Implementations of this interface should be thread-safe.
 * The default implementation (DynamoGoalDao) is thread-safe.
 * 
 * Error Handling:
 * - Methods may throw ResourceNotFoundException when requested data doesn't
 * exist
 * - Other runtime exceptions may be thrown for database errors
 */
public interface GoalDao {
    /**
     * Retrieves user metadata for the given user ID.
     * 
     * This method fetches the user's metadata from the data store,
     * including their email address and timestamps.
     *
     * @param userId The unique identifier of the user
     * @return UserEntity containing user metadata
     * @throws ResourceNotFoundException if user doesn't exist
     */
    UserEntity getUser(String userId);

    /**
     * Retrieves all RSNs associated with the given user ID.
     * 
     * This method fetches all RuneScape usernames that have been
     * registered for the specified user. The list is unordered.
     * 
     * Performance Note:
     * This operation uses a query with a begins_with condition on the sort key,
     * making it efficient even with a large number of RSNs.
     *
     * @param userId The unique identifier of the user
     * @return List of RsnEntity objects, empty list if user has no RSNs
     */
    List<RsnEntity> getRsnsForUser(String userId);
}

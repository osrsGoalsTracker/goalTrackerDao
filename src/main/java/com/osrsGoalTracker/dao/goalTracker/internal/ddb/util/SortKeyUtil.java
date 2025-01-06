package com.osrsGoalTracker.dao.goalTracker.internal.ddb.util;

/**
 * Utility class for generating sort keys for DynamoDB items.
 * This class centralizes the sort key generation logic to make it easier to
 * modify
 * if needed.
 */
public final class SortKeyUtil {
    private static final String METADATA = "METADATA";
    private static final String PLAYER = "PLAYER";

    private SortKeyUtil() {
        throw new UnsupportedOperationException("This is a utility class and should not be instantiated");
    }

    /**
     * Returns the sort key for user metadata.
     * 
     * @return the sort key for user metadata
     */
    public static String getUserMetadataSortKey() {
        return METADATA;
    }

    /**
     * Returns the sort key for player metadata.
     * 
     * @param playerName the name of the player
     * @return the sort key for player metadata
     */
    public static String getPlayerMetadataSortKey(String playerName) {
        return String.format("%s#%s#%s", PLAYER, METADATA, playerName);
    }
}
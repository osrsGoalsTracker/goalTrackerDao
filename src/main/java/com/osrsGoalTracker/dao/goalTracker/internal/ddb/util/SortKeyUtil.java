package com.osrsGoalTracker.dao.goalTracker.internal.ddb.util;

/**
 * Utility class for constructing sort keys for DynamoDB items.
 */
public final class SortKeyUtil {
    private static final String METADATA = "METADATA";
    private static final String PLAYER_METADATA_PREFIX = "PLAYER#METADATA";

    private SortKeyUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the sort key for user metadata.
     *
     * @return The sort key for user metadata
     */
    public static String getUserMetadataSortKey() {
        return METADATA;
    }

    /**
     * Gets the sort key for character metadata.
     *
     * @return The sort key for character metadata
     */
    public static String getCharacterMetadataSortKey() {
        return PLAYER_METADATA_PREFIX;
    }
}

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
     * Gets the sort key for player metadata.
     *
     * @return The sort key for player metadata
     */
    public static String getPlayerMetadataSortKey() {
        return PLAYER_METADATA_PREFIX;
    }
}

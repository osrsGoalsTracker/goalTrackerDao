package com.osrsGoalTracker.goals.dao.internal.ddb.util;

/**
 * Utility class for constructing sort keys used in DynamoDB.
 */
public final class SortKeyUtil {
    private static final String METADATA = "METADATA";

    private SortKeyUtil() {
        // Prevent instantiation
    }

    /**
     * Gets the sort key for user metadata.
     *
     * @return The sort key for user metadata
     */
    public static String getUserMetadataSortKey() {
        return METADATA;
    }
}

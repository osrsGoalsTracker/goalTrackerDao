package com.osrsGoalTracker.goalsTracker.dao.internal.ddb.util;

/**
 * Utility class for constructing sort keys for DynamoDB operations.
 */
public final class SortKeyUtil {
    
    private SortKeyUtil() {
        throw new UnsupportedOperationException("This is a utility class and should not be instantiated");
    }

    /**
     * Returns the sort key for user metadata.
     *
     * @return The sort key for user metadata
     */
    public static String getUserMetadataSortKey() {
        return "METADATA";
    }
}

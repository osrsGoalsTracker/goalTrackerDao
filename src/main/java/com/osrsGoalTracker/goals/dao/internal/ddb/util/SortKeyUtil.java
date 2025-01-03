package com.osrsGoalTracker.goals.dao.internal.ddb.util;

/**
 * Utility class for constructing DynamoDB sort keys.
 * This class provides methods to create consistent sort keys for different
 * types of data in the single-table design.
 */
public final class SortKeyUtil {
    private static final String METADATA = "METADATA";
    private static final String RSN_PREFIX = "RSN#";
    private static final String METADATA_PREFIX = "METADATA#";

    private SortKeyUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the sort key for user metadata.
     * 
     * @return The sort key string
     */
    public static String getUserMetadataSortKey() {
        return METADATA;
    }

    /**
     * Gets the sort key for RSN metadata.
     * 
     * @param rsn The RuneScape username
     * @return The sort key string
     */
    public static String getRsnMetadataSortKey(String rsn) {
        return RSN_PREFIX + METADATA_PREFIX + rsn;
    }

    /**
     * Gets the prefix for RSN metadata sort keys.
     * This is used in begins_with queries to find all RSNs for a user.
     * 
     * @return The RSN metadata prefix
     */
    public static String getRsnMetadataPrefix() {
        return RSN_PREFIX + METADATA_PREFIX;
    }
}

package com.osrs.goal.dao.util;

/**
 * Utility class for constructing DynamoDB sort keys.
 * Provides methods to create standardized sort keys for different entity types.
 * 
 * Sort key formats:
 * - User metadata: METADATA
 * - RSN metadata: RSN#METADATA#rsn
 * 
 * The sort keys are designed to support efficient querying patterns:
 * - Get user metadata: exact match on METADATA
 * - Get all RSNs for a user: begins_with on RSN#METADATA#
 */
public final class SortKeyUtil {
    // Constants for sort key components
    private static final String METADATA = "METADATA"; // Used for metadata items
    private static final String RSN = "RSN"; // Prefix for RSN-related items
    private static final String DELIMITER = "#"; // Delimiter between sort key components

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class that should only contain static methods.
     */
    private SortKeyUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Gets the sort key for user metadata.
     * This key is used to store and retrieve user-specific metadata like email.
     *
     * @return The sort key for user metadata (METADATA)
     */
    public static String getUserMetadataSortKey() {
        return METADATA;
    }

    /**
     * Gets the sort key for RSN metadata.
     * This key is used to store and retrieve RSN-specific metadata.
     * The format RSN#METADATA#rsn allows for:
     * - Efficient querying of all RSNs for a user
     * - Unique identification of each RSN entry
     *
     * @param rsn The RuneScape username
     * @return The sort key in the format RSN#METADATA#rsn
     */
    public static String getRsnMetadataSortKey(String rsn) {
        return String.join(DELIMITER, RSN, METADATA, rsn);
    }

    /**
     * Gets the sort key prefix for querying RSN metadata.
     * This prefix is used with begins_with to find all RSNs for a user.
     * The format RSN#METADATA# allows for efficient querying of all RSNs
     * associated with a particular user.
     *
     * @return The sort key prefix for RSN queries (RSN#METADATA#)
     */
    public static String getRsnMetadataPrefix() {
        return String.join(DELIMITER, RSN, METADATA, "");
    }
}

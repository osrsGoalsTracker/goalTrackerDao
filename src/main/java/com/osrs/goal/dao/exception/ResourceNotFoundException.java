package com.osrs.goal.dao.exception;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * This exception is used to indicate that a requested entity does not exist
 * in the data store. It extends RuntimeException because:
 * 1. Resource not found is a common occurrence that callers should expect
 * 2. It reduces boilerplate by not requiring explicit exception declaration
 * 
 * Common scenarios:
 * - Attempting to fetch a user that doesn't exist
 * - Attempting to fetch RSNs for a non-existent user
 * 
 * Example usage:
 * 
 * <pre>
 * if (!response.hasItem()) {
 *     throw new ResourceNotFoundException("User not found with ID: " + userId);
 * }
 * </pre>
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified message.
     * 
     * The message should clearly indicate:
     * - What type of resource was not found
     * - The identifier that was used to look for it
     *
     * @param message The detail message explaining what was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified message and
     * cause.
     * 
     * This constructor is useful when wrapping lower-level exceptions
     * while maintaining the original error context.
     *
     * @param message The detail message explaining what was not found
     * @param cause   The underlying cause of the resource not being found
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


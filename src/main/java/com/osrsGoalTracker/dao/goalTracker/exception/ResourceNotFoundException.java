package com.osrsGoalTracker.dao.goalTracker.exception;

/**
 * Exception thrown when a resource is not found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later 
     *                retrieval by the {@link #getMessage()} method.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
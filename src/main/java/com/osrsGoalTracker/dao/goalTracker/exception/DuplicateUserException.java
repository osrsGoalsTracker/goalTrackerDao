package com.osrsGoalTracker.dao.goalTracker.exception;

/**
 * Exception thrown when a user already exists in the database.
 */
public class DuplicateUserException extends RuntimeException {
    /**
     * Constructs a new DuplicateUserException with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later 
     *                retrieval by the {@link #getMessage()} method.
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}
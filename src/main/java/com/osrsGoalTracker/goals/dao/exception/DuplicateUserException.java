package com.osrsGoalTracker.goals.dao.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 */
public class DuplicateUserException extends RuntimeException {

    /**
     * Constructs a new DuplicateUserException with the specified message.
     *
     * @param message The detail message
     */
    public DuplicateUserException(String message) {
        super(message);
    }

    /**
     * Constructs a new DuplicateUserException with the specified message and cause.
     *
     * @param message The detail message
     * @param cause   The cause of the exception
     */
    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
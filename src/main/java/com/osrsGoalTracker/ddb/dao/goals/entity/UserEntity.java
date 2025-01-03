package com.osrsGoalTracker.ddb.dao.goals.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a user in the system.
 * Contains user-specific metadata such as email.
 * 
 * This class extends AbstractEntity to inherit common fields:
 * - userId: The unique identifier of the user
 * - createdAt: When the user was created
 * - updatedAt: When the user was last updated
 * 
 * Lombok annotations:
 * - @Getter: Generates getters for all fields
 * - @SuperBuilder: Enables builder pattern with inheritance support
 */
@Getter
@SuperBuilder
public class UserEntity extends AbstractEntity {
    /**
     * The user's email address.
     * This is stored in the 'email' attribute in DynamoDB.
     */
    private final String email;

    /**
     * Constructor for UserEntity.
     * Delegates to superclass constructor for common fields.
     *
     * @param userId    The unique identifier of the user
     * @param email     The user's email address
     * @param createdAt When the user was created
     * @param updatedAt When the user was last updated
     */
    public UserEntity(String userId, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(userId, createdAt, updatedAt);
        this.email = email;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserEntity(userId=")
                .append(getUserId())
                .append(", email=")
                .append(email)
                .append(", createdAt=")
                .append(getCreatedAt())
                .append(", updatedAt=")
                .append(getUpdatedAt())
                .append(")");
        return sb.toString();
    }
}

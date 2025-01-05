package com.osrsGoalTracker.dao.goalTracker.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void testUserEntityBuilder() {
        String userId = "testUser";
        String email = "test@example.com";
        LocalDateTime now = LocalDateTime.now();

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .email(email)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(userId, user.getUserId());
        assertEquals(email, user.getEmail());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
}
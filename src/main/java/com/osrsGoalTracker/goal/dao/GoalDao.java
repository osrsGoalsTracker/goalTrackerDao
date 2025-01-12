package com.osrsGoalTracker.goal.dao;

import com.osrsGoalTracker.goal.dao.entity.GoalEntity;

/**
 * Interface for the GoalDao.
 */
public interface GoalDao {
    /**
     * Creates a new goal for a user's character and initializes its earliest
     * progress record.
     * This operation is atomic - both the goal metadata and earliest progress
     * record are created together.
     *
     * @param goalEntity   The goal entity containing all necessary goal information
     *                     including userId and characterName
     * @param currentValue The current value for the goal's progress
     * @return The created goal entity with the generated goal ID
     * @throws IllegalArgumentException If goalEntity is null or required fields are
     *                                  missing
     */
    GoalEntity createGoal(GoalEntity goalEntity, long currentValue);
}
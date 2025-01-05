package com.osrsGoalTracker.dao.goalTracker.module;

import com.google.inject.AbstractModule;
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.DynamoGoalTrackerDao;

/**
 * Module for the GoalTrackerDao.
 */
public class GoalTrackerDaoModule extends AbstractModule {
    /**
     * Configures the GoalTrackerDao module.
     */
    @Override
    protected void configure() {
        bind(GoalTrackerDao.class).to(DynamoGoalTrackerDao.class);
    }
}
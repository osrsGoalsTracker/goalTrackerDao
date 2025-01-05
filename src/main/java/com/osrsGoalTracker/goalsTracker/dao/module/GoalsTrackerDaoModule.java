package com.osrsGoalTracker.goalsTracker.dao.module;

import com.google.inject.AbstractModule;
import com.osrsGoalTracker.goalsTracker.dao.GoalsTrackerDao;
import com.osrsGoalTracker.goalsTracker.dao.internal.ddb.DynamoGoalsTrackerDao;

/**
 * Guice module for binding the GoalsTrackerDao interface to its DynamoDB
 * implementation.
 */
public class GoalsTrackerDaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GoalsTrackerDao.class).to(DynamoGoalsTrackerDao.class);
    }
}

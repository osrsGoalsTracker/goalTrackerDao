package com.osrsGoalTracker.goals.dao.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import com.osrsGoalTracker.goals.dao.GoalsDao;
import com.osrsGoalTracker.goals.dao.internal.ddb.DynamoGoalsDao;

/**
 * Guice module for configuring dependency injection.
 * This module provides the following bindings:
 * 
 * 1. Binding of GoalsDao interface to DynamoGoalsDao implementation
 * 
 * Usage:
 * This module should be installed in your Guice injector during application
 * startup.
 * 
 * Example:
 * ```java
 * Injector injector = Guice.createInjector(new GoalsDaoModule());
 * GoalsDao goalsDao = injector.getInstance(GoalsDao.class);
 * ```
 */
public class GoalsDaoModule extends AbstractModule {

    /**
     * Binds the GoalsDao interface to its DynamoDB implementation.
     * The implementation is bound as a singleton to ensure thread safety
     * and efficient resource usage.
     */
    @Override
    protected void configure() {
        bind(GoalsDao.class).to(DynamoGoalsDao.class).in(Singleton.class);
    }
}

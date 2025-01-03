package com.osrs.goal.dao.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.osrs.goal.dao.DynamoGoalDao;
import com.osrs.goal.dao.GoalDao;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Guice module for dependency injection configuration.
 * Configures bindings for the DAO layer.
 * 
 * This module provides:
 * 1. Binding of GoalDao interface to DynamoGoalDao implementation
 * 2. Singleton instance of DynamoDbClient
 * 
 * Usage:
 * 
 * <pre>
 * Injector injector = Guice.createInjector(new GoalDaoModule());
 * GoalDao goalDao = injector.getInstance(GoalDao.class);
 * </pre>
 */
public class GoalDaoModule extends AbstractModule {
    /**
     * Configures the Guice bindings.
     * Binds the GoalDao interface to its DynamoDB implementation.
     * The implementation is scoped as a singleton to ensure resource reuse.
     */
    @Override
    protected void configure() {
        bind(GoalDao.class).to(DynamoGoalDao.class).in(Singleton.class);
    }

    /**
     * Provides a singleton instance of DynamoDbClient.
     * The client is thread-safe and should be reused across the application.
     * 
     * Note: In a production environment, you might want to configure the client
     * with specific AWS credentials, region, and other settings.
     *
     * @return The DynamoDbClient instance
     */
    @Provides
    @Singleton
    DynamoDbClient provideDynamoDbClient() {
        return DynamoDbClient.builder().build();
    }
}


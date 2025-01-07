package com.osrsGoalTracker.dao.goalTracker.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.DynamoGoalTrackerDao;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Guice module for GoalTrackerDao dependencies.
 */
public class GoalTrackerDaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GoalTrackerDao.class).to(DynamoGoalTrackerDao.class);
    }

    @Provides
    @Singleton
    DynamoDbClient provideDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }
}
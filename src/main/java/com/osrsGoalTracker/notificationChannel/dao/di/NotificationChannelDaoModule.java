package com.osrsGoalTracker.notificationChannel.dao.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.osrsGoalTracker.notificationChannel.dao.NotificationChannelDao;
import com.osrsGoalTracker.notificationChannel.dao.internal.ddb.impl.DynamoNotificationChannelDao;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Guice module for NotificationDao dependencies.
 */
public class NotificationChannelDaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NotificationChannelDao.class).to(DynamoNotificationChannelDao.class);
    }

    @Provides
    @Singleton
    DynamoDbClient provideDynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }
}
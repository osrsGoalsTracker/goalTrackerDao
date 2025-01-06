package com.osrsGoalTracker.dao.goalTracker.internal.ddb;

import com.google.inject.Inject;
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.PlayerEntity;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl.DynamoPlayerDao;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl.DynamoUserDao;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * DynamoDB implementation of the GoalsTrackerDao interface.
 * Delegates to specialized DAOs for different entity types.
 */
public class DynamoGoalTrackerDao implements GoalTrackerDao {
    private final DynamoUserDao userDao;
    private final DynamoPlayerDao playerDao;

    /**
     * Constructor for DynamoGoalTrackerDao.
     * Uses Guice for dependency injection of the DynamoDB client.
     * Creates specialized DAOs for user and player operations.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     */
    @Inject
    public DynamoGoalTrackerDao(DynamoDbClient dynamoDbClient) {
        String tableName = getTableName();
        this.userDao = new DynamoUserDao(dynamoDbClient, tableName);
        this.playerDao = new DynamoPlayerDao(dynamoDbClient, tableName);
    }

    private static String getTableName() {
        String tableName = System.getenv("GOAL_TRACKER_TABLE_NAME");
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = System.getProperty("GOAL_TRACKER_TABLE_NAME");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalStateException(
                    "GOAL_TRACKER_TABLE_NAME must be set in environment variables or system properties");
        }
        return tableName;
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        return userDao.createUser(user);
    }

    @Override
    public UserEntity getUser(String userId) {
        return userDao.getUser(userId);
    }

    @Override
    public PlayerEntity addPlayerToUser(String userId, String playerName) {
        return playerDao.addPlayerToUser(userId, playerName);
    }
}
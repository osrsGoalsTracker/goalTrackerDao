package com.osrsGoalTracker.dao.goalTracker.internal.ddb;

import java.util.List;

import com.google.inject.Inject;
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl.DynamoCharacterDao;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl.DynamoUserDao;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * DynamoDB implementation of the GoalsTrackerDao interface.
 * Delegates to specialized DAOs for different entity types.
 */
public class DynamoGoalTrackerDao implements GoalTrackerDao {
    private final DynamoUserDao userDao;
    private final DynamoCharacterDao characterDao;

    /**
     * Constructor for DynamoGoalTrackerDao.
     * Uses Guice for dependency injection of the DynamoDB client.
     * Creates specialized DAOs for user and character operations.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     */
    @Inject
    public DynamoGoalTrackerDao(DynamoDbClient dynamoDbClient) {
        String tableName = getTableName();
        this.userDao = new DynamoUserDao(dynamoDbClient, tableName);
        this.characterDao = new DynamoCharacterDao(dynamoDbClient, tableName);
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
    public CharacterEntity addCharacterToUser(String userId, String characterName) {
        return characterDao.addCharacterToUser(userId, characterName);
    }

    @Override
    public List<CharacterEntity> getCharactersForUser(String userId) {
        return characterDao.getCharactersForUser(userId);
    }
}
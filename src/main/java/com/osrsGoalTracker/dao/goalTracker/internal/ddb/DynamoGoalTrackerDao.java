package com.osrsGoalTracker.dao.goalTracker.internal.ddb;

import java.util.List;

import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl.DynamoCharacterDao;
import com.osrsGoalTracker.dao.goalTracker.internal.ddb.impl.DynamoUserDao;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * DynamoDB implementation of the GoalTrackerDao interface.
 */
public class DynamoGoalTrackerDao implements GoalTrackerDao {
    private final DynamoUserDao userDao;
    private final DynamoCharacterDao characterDao;

    /**
     * Constructor for DynamoGoalTrackerDao.
     *
     * @param dynamoDbClient The AWS DynamoDB client
     * @param tableName      The name of the DynamoDB table
     */
    public DynamoGoalTrackerDao(DynamoDbClient dynamoDbClient, String tableName) {
        this.userDao = new DynamoUserDao(dynamoDbClient, tableName);
        this.characterDao = new DynamoCharacterDao(dynamoDbClient, tableName);
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
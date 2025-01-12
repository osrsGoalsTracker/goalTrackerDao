# OSRS Goal Tracker DAO Library

This library provides data access objects (DAOs) for interacting with the OSRS Goal Tracker DynamoDB database.

## Installation

Add the following to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.osrsGoalsTracker:goalTrackerDao:1.0-SNAPSHOT'
}
```

## Usage

### Goal Management

```java
import com.osrsGoalTracker.goal.dao.GoalDao;
import com.osrsGoalTracker.goal.dao.entity.GoalEntity;
import java.time.LocalDate;

// Create a new GoalEntity
GoalEntity goal = GoalEntity.builder()
    .userId("userId")
    .characterName("MyCharacter")
    .targetAttribute("Woodcutting")
    .targetType("xp")
    .targetValue(13034431L)
    .targetDate(LocalDate.of(2025, 3, 1))
    .notificationChannelType("SMS")
    .frequency("daily")
    .build();

// Create goal with initial progress value
GoalEntity createdGoal = goalDao.createGoal(goal, 1000L);

// the interface is `GoalEntity createGoal(GoalEntity goalEntity, long currentValue);`
```

The `GoalEntity` class has the following fields:
- `userId` (String): The ID of the user who owns the goal
- `characterName` (String): The name of the character this goal is for
- `goalId` (String): The unique identifier for the goal (generated on creation)
- `targetAttribute` (String): The skill or activity being tracked
- `targetType` (String): The type of target (e.g., "xp", "level", etc.)
- `targetValue` (Long): The target value to achieve
- `targetDate` (LocalDate): The date by which to achieve the goal
- `notificationChannelType` (String): The type of notification channel to use
- `frequency` (String): How often to check/notify about progress
- `createdAt` (Instant): When the goal was created
- `updatedAt` (Instant): When the goal was last updated

### User Management

```java
import com.osrsGoalTracker.user.dao.UserDao;
import com.osrsGoalTracker.user.dao.entity.UserEntity;
import com.osrsGoalTracker.shared.dao.exception.ResourceNotFoundException;
import com.osrsGoalTracker.user.dao.exception.DuplicateUserException;

// Create a user
UserEntity user = userDao.createUser(UserEntity.builder()
    .email("user@example.com")
    .build());

// Get a user
UserEntity user = userDao.getUser("userId");
```

### Character Management

```java
import com.osrsGoalTracker.character.dao.CharacterDao;
import com.osrsGoalTracker.character.dao.entity.CharacterEntity;
import com.osrsGoalTracker.shared.dao.exception.ResourceNotFoundException;
import java.util.List;

// Add a character to a user
CharacterEntity character = characterDao.addCharacterToUser("userId", "characterName");

// Get all characters for a user
List<CharacterEntity> characters = characterDao.getCharactersForUser("userId");
```

### Notification Channel Management

```java
import com.osrsGoalTracker.notificationChannel.dao.NotificationChannelDao;
import com.osrsGoalTracker.notificationChannel.dao.entity.NotificationChannelEntity;
import com.osrsGoalTracker.shared.dao.exception.ResourceNotFoundException;
import java.util.List;

// Create a Discord notification channel
NotificationChannelEntity channel = notificationChannelDao.createNotificationChannel("userId", 
    NotificationChannelEntity.builder()
        .channelType("DISCORD")
        .identifier("discord-channel-id")
        .isActive(true)
        .build());

// Get all notification channels for a user
List<NotificationChannelEntity> channels = notificationChannelDao.getNotificationChannels("userId");
```

## Dependency Injection Setup

This library is designed to work with Guice dependency injection but does not provide DI modules. Instead, create your own module in your application:

```java
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.osrsGoalTracker.user.dao.UserDao;
import com.osrsGoalTracker.user.dao.impl.DynamoUserDao;
import com.osrsGoalTracker.character.dao.CharacterDao;
import com.osrsGoalTracker.character.dao.impl.DynamoCharacterDao;
import com.osrsGoalTracker.notificationChannel.dao.NotificationChannelDao;
import com.osrsGoalTracker.notificationChannel.dao.impl.DynamoNotificationChannelDao;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DaoModule extends AbstractModule {
    @Override
    protected void configure() {
        // Bind your DAO implementations
        bind(UserDao.class).to(DynamoUserDao.class);
        bind(CharacterDao.class).to(DynamoCharacterDao.class);
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
```

Then you can inject the DAOs where needed:
```java
@Inject
private UserDao userDao;

@Inject
private CharacterDao characterDao;

@Inject
private NotificationChannelDao notificationChannelDao;
```

## API Reference

### UserEntity

| Field | Type | Description |
|-------|------|-------------|
| userId | String | Unique identifier for the user |
| email | String | User's email address |
| createdAt | LocalDateTime | When the user was created |
| updatedAt | LocalDateTime | When the user was last updated |

### CharacterEntity

| Field | Type | Description |
|-------|------|-------------|
| name | String | RuneScape character name |
| userId | String | ID of the user who owns this character |
| createdAt | LocalDateTime | When the character was added |
| updatedAt | LocalDateTime | When the character was last updated |

### NotificationChannelEntity

| Field | Type | Description |
|-------|------|-------------|
| channelType | String | Type of notification channel (e.g., SMS, Discord) |
| identifier | String | Channel-specific identifier (e.g., phone number, Discord channel ID) |
| isActive | boolean | Whether the notification channel is active |
| createdAt | LocalDateTime | When the channel was created |
| updatedAt | LocalDateTime | When the channel was last updated |

## Development

### Building

```bash
./gradlew build
```

### Testing

```bash
./gradlew test
```

### Environment Variables

- `AWS_REGION`: AWS region for DynamoDB (required)
- `GOAL_TRACKER_TABLE_NAME`: Name of the DynamoDB table (required)
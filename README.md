# OSRS Goal Tracker DAO

A Java library for managing RuneScape character goals and progress tracking in DynamoDB.

## Installation

Add the following to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.yourusername:osrsGoalTracker:VERSION'
}
```

## Usage

### Creating a User

```java
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

UserEntity user = goalTrackerDao.createUser(UserEntity.builder()
    .email("user@example.com")
    .build());
```

### Getting a User

```java
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

UserEntity user = goalTrackerDao.getUser("userId");
```

### Adding a Character to a User

```java
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;

CharacterEntity character = goalTrackerDao.addCharacterToUser("userId", "characterName");
```

### Getting All Characters for a User

```java
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.CharacterEntity;

List<CharacterEntity> characters = goalTrackerDao.getCharactersForUser("userId");
```

### Managing Notification Channels

```java
import com.osrsGoalTracker.notificationChannel.dao.NotificationChannelDao;
import com.osrsGoalTracker.notificationChannel.dao.entity.NotificationChannelEntity;

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

### Methods

#### createUser

Creates a new user in the database.

```java
UserEntity createUser(UserEntity user)
```

- **Parameters:**
  - `user`: The user entity to create (email is required)
- **Returns:** The created user entity with generated ID and timestamps
- **Throws:**
  - `IllegalArgumentException`: If user is null or email is null/empty
  - `DuplicateUserException`: If a user with the same email already exists

#### getUser

Retrieves a user from the database.

```java
UserEntity getUser(String userId)
```

- **Parameters:**
  - `userId`: The ID of the user to retrieve
- **Returns:** The user entity
- **Throws:**
  - `IllegalArgumentException`: If userId is null or empty
  - `ResourceNotFoundException`: If user is not found

#### addCharacterToUser

Adds a RuneScape character to a user's account.

```java
CharacterEntity addCharacterToUser(String userId, String characterName)
```

- **Parameters:**
  - `userId`: The ID of the user to add the character to
  - `characterName`: The name of the RuneScape character to add
- **Returns:** The created character entity
- **Throws:**
  - `IllegalArgumentException`: If userId or characterName is null or empty

#### getCharactersForUser

Retrieves all characters associated with a user.

```java
List<CharacterEntity> getCharactersForUser(String userId)
```

- **Parameters:**
  - `userId`: The ID of the user to get characters for
- **Returns:** List of character entities associated with the user
- **Throws:**
  - `IllegalArgumentException`: If userId is null or empty

#### createNotificationChannel

Creates a new notification channel for a user.

```java
NotificationChannelEntity createNotificationChannel(String userId, NotificationChannelEntity channel)
```

- **Parameters:**
  - `userId`: The ID of the user to create the channel for
  - `channel`: The notification channel entity to create (channelType, identifier, and isActive are required)
- **Returns:** The created notification channel entity with timestamps
- **Throws:**
  - `IllegalArgumentException`: If userId is null/empty or channel validation fails

#### getNotificationChannels

Retrieves all notification channels for a user.

```java
List<NotificationChannelEntity> getNotificationChannels(String userId)
```

- **Parameters:**
  - `userId`: The ID of the user to get channels for
- **Returns:** List of notification channel entities associated with the user
- **Throws:**
  - `IllegalArgumentException`: If userId is null or empty

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

- `GOAL_TRACKER_TABLE_NAME`: Name of the DynamoDB table (required)
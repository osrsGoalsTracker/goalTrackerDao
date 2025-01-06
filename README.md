# OSRS Goal Tracker DAO

A Java library for managing RuneScape player goals and progress tracking in DynamoDB.

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
UserEntity user = goalTrackerDao.createUser(UserEntity.builder()
    .email("user@example.com")
    .build());
```

### Getting a User

```java
UserEntity user = goalTrackerDao.getUser("userId");
```

### Adding a Player to a User

```java
PlayerEntity player = goalTrackerDao.addPlayerToUser("userId", "playerName");
```

### Getting All Players for a User

```java
List<PlayerEntity> players = goalTrackerDao.getPlayersForUser("userId");
```

## API Reference

### UserEntity

| Field | Type | Description |
|-------|------|-------------|
| userId | String | Unique identifier for the user |
| email | String | User's email address |
| createdAt | LocalDateTime | When the user was created |
| updatedAt | LocalDateTime | When the user was last updated |

### PlayerEntity

| Field | Type | Description |
|-------|------|-------------|
| name | String | RuneScape player name |
| userId | String | ID of the user who owns this player |
| createdAt | LocalDateTime | When the player was added |
| updatedAt | LocalDateTime | When the player was last updated |

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

#### addPlayerToUser

Adds a RuneScape player to a user's account.

```java
PlayerEntity addPlayerToUser(String userId, String playerName)
```

- **Parameters:**
  - `userId`: The ID of the user to add the player to
  - `playerName`: The name of the RuneScape player to add
- **Returns:** The created player entity
- **Throws:**
  - `IllegalArgumentException`: If userId or playerName is null or empty

#### getPlayersForUser

Retrieves all players associated with a user.

```java
List<PlayerEntity> getPlayersForUser(String userId)
```

- **Parameters:**
  - `userId`: The ID of the user to get players for
- **Returns:** List of player entities associated with the user
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
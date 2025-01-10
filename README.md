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
UserEntity user = goalTrackerDao.createUser(UserEntity.builder()
    .email("user@example.com")
    .build());
```

### Getting a User

```java
UserEntity user = goalTrackerDao.getUser("userId");
```

### Adding a Character to a User

```java
CharacterEntity character = goalTrackerDao.addCharacterToUser("userId", "characterName");
```

### Getting All Characters for a User

```java
List<CharacterEntity> characters = goalTrackerDao.getCharactersForUser("userId");
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
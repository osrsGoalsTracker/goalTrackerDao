# Goal DAO Library

A Java library for interacting with DynamoDB to manage OSRS goal tracking data. This library provides a clean abstraction over DynamoDB operations, handling the complexities of partition keys and sort keys internally.

## Features

- Seamless DynamoDB interaction
- Type-safe entity models
- Efficient data access patterns
- Comprehensive test coverage

## Requirements

- JDK 21
- Gradle 8.x

## Installation

First, add the JitPack repository to your build file:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

Then, add the dependency:

```groovy
dependencies {
    implementation 'com.github.osrsGoalTracker:goalDao:Tag'
}
```

## Usage

### Managing Users
```java
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.entity.UserEntity;

@Inject
public YourClass(GoalTrackerDao goalDao) {
    // Create a new user
    UserEntity userToCreate = UserEntity.builder()
        .userId("uniqueId")
        .email("user@example.com")
        .build();
    UserEntity newUser = goalDao.createUser(userToCreate);
    
    // Get user metadata
    UserEntity user = goalDao.getUser(newUser.getUserId());
}
```

## API Reference

### Package Structure

The library is organized under the following package structure:
```
com.osrsGoalTracker.dao.goalTracker
├── GoalTrackerDao.java         # Main interface
├── entity/                     # Public entity classes
│   ├── AbstractEntity.java     # Base entity class
│   └── UserEntity.java        # User entity
├── exception/                  # Public exceptions
│   ├── DuplicateUserException.java
│   └── ResourceNotFoundException.java
├── module/                     # Guice modules
│   └── GoalTrackerDaoModule.java
└── internal/                   # Implementation details
    └── ddb/                    # DynamoDB implementation
        ├── DynamoGoalTrackerDao.java
        └── util/               # Internal utilities
            └── SortKeyUtil.java
```

### Entities

#### UserEntity
Entity containing user data:
```java
{
    String userId;           // Unique identifier
    String email;           // Email address
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### Methods

#### createUser
```java
/**
 * Creates a new user in the system.
 *
 * @param user The UserEntity containing the user data
 * @return UserEntity containing the created user's data
 * @throws IllegalArgumentException if user is null or fields are empty
 * @throws DuplicateUserException if user already exists
 */
UserEntity createUser(UserEntity user);
```

#### getUser
```java
/**
 * Retrieves user metadata.
 *
 * @param userId The unique identifier of the user
 * @return UserEntity containing user metadata
 * @throws IllegalArgumentException if userId is null/empty
 * @throws ResourceNotFoundException if user doesn't exist
 */
UserEntity getUser(String userId);
```

### Dependency Injection

The library uses Guice for dependency injection:

```java
import com.osrsGoalTracker.dao.goalTracker.GoalTrackerDao;
import com.osrsGoalTracker.dao.goalTracker.module.GoalTrackerDaoModule;

Injector injector = Guice.createInjector(new GoalTrackerDaoModule());
GoalTrackerDao goalDao = injector.getInstance(GoalTrackerDao.class);
```

## Development

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
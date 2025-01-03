# Goals DAO Library

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

Then, add the dependency. Replace `Tag` with a specific version tag, commit hash, or `-SNAPSHOT` for the latest development version:

```groovy
dependencies {
    implementation 'com.github.osrsGoalsTracker:goalsDao:Tag'
}
```

To find the latest version:
1. Visit [JitPack - goalsDao](https://jitpack.io/#osrsGoalsTracker/goalsDao)
2. Click "Get it" on the version you want to use
3. Copy the version tag from the dependency snippet

## Usage

### Managing Users
```java
import com.osrsGoalTracker.goals.dao.GoalsDao;
import com.osrsGoalTracker.goals.dao.entity.UserEntity;
import com.osrsGoalTracker.goals.dao.entity.RsnEntity;

@Inject
public YourClass(GoalsDao goalsDao) {
    // Create a new user
    UserEntity newUser = goalsDao.createUser("user@example.com");
    
    // Get user metadata
    UserEntity user = goalsDao.getUser(newUser.getUserId());
    
    // Get list of RSNs for user
    List<RsnEntity> rsns = goalsDao.getRsnsForUser(newUser.getUserId());
}
```

## API Reference

### Package Structure

The library is organized under the following package structure:
```
com.osrsGoalTracker.goals.dao
├── GoalsDao.java         # Main interface
├── entity/              # Public entity classes (UserEntity, RsnEntity)
├── exception/           # Public exceptions
├── module/             # Guice modules for dependency injection
└── internal/           # Implementation details (not for client use)
    └── ddb/
        ├── DynamoGoalsDao.java
        └── util/        # Internal utilities
```

### Entities

#### UserEntity
Base entity containing common fields:
```java
{
    String userId;      // The user's unique identifier
    String email;       // User's email address
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

#### RsnEntity
```java
{
    String userId;      // The user's unique identifier
    String rsn;        // RuneScape username
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
 * @param email The email address of the user
 * @return UserEntity containing the created user's data
 * @throws IllegalArgumentException if email is null or empty
 */
UserEntity createUser(String email);
```

#### getUser
```java
/**
 * Retrieves user metadata for the given user ID.
 *
 * @param userId The unique identifier of the user
 * @return UserEntity containing user metadata
 * @throws ResourceNotFoundException if user doesn't exist
 */
UserEntity getUser(String userId);
```

#### getRsnsForUser
```java
/**
 * Retrieves all RSNs associated with the given user ID.
 *
 * @param userId The unique identifier of the user
 * @return List of RsnEntity objects, empty list if user has no RSNs
 */
List<RsnEntity> getRsnsForUser(String userId);
```

### Dependency Injection

The library uses Guice for dependency injection. To use it in your application:

```java
import com.osrsGoalTracker.goals.dao.GoalsDao;
import com.osrsGoalTracker.goals.dao.module.GoalsDaoModule;

// Create injector with the GoalsDaoModule
Injector injector = Guice.createInjector(new GoalsDaoModule());

// Get an instance of GoalsDao
GoalsDao goalsDao = injector.getInstance(GoalsDao.class);
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

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
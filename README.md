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
import com.osrsGoalTracker.goals.dao.GoalsTrackerDao;
import com.osrsGoalTracker.goals.dao.entity.UserEntity;

@Inject
public YourClass(GoalsTrackerDao goalsDao) {
    // Create a new user
    UserEntity userToCreate = UserEntity.builder()
        .userId("uniqueId")
        .email("user@example.com")
        .build();
    UserEntity newUser = goalsDao.createUser(userToCreate);
    
    // Get user metadata
    UserEntity user = goalsDao.getUser(newUser.getUserId());
}
```

## API Reference

### Package Structure

The library is organized under the following package structure:
```
com.osrsGoalTracker.goalsTracker.dao
├── GoalsTrackerDao.java              # Main interface
├── entity/                    # Public entity classes
│   ├── AbstractEntity.java    # Base entity class with common fields
│   └── UserEntity.java        # User entity implementation
├── exception/                 # Public exceptions
│   ├── DuplicateUserException.java
│   └── ResourceNotFoundException.java
├── module/                    # Guice modules for dependency injection
│   └── GoalsTrackerDaoModule.java
└── internal/                  # Implementation details (not for client use)
    └── ddb/                   # DynamoDB specific implementation
        ├── DynamoGoalsTrackerDao.java
        └── util/              # Internal utilities
            └── SortKeyUtil.java
```

### Naming Conventions

1. **Packages**
   - All packages start with base package `com.osrsGoalTracker.goalsTracker.dao`
   - Implementation-specific code goes under `.internal`
   - Database-specific code goes under `.internal.ddb`
   - Utility classes go under `.internal.ddb.util`

2. **Classes**
   - Entity classes end with `Entity` (e.g., `UserEntity`)
   - Abstract classes start with `Abstract` (e.g., `AbstractEntity`)
   - Utility classes end with `Util` (e.g., `SortKeyUtil`)
   - Exception classes end with `Exception`
   - Implementation classes start with their implementation type (e.g., `DynamoGoalsTrackerDao`)

3. **Methods**
   - CRUD operations use standard naming: `create`, `get`, `update`, `delete`
   - Boolean methods start with `is` or `has`
   - Utility methods should be descriptive and action-oriented

4. **Tests**
   - Test classes end with `Test` (e.g., `DynamoGoalsTrackerDaoTest`)
   - Test methods use descriptive names explaining the scenario and expected outcome
   - Test methods start with `test` (e.g., `testCreateUserSuccess`)

### Entities

#### UserEntity
Entity containing user data:
```java
{
    String userId;      // The user's unique identifier
    String email;       // User's email address
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
 * @param user The UserEntity containing the user data to create
 * @return UserEntity containing the created user's data
 * @throws IllegalArgumentException if user is null, or if userId or email is null or empty
 * @throws DuplicateUserException if a user with the same ID already exists
 */
UserEntity createUser(UserEntity user);
```

Required fields for UserEntity when creating a user:
- userId: String (non-null, non-empty)
- email: String (non-null, non-empty)

#### getUser
```java
/**
 * Retrieves user metadata for the given user ID.
 *
 * @param userId The unique identifier of the user
 * @return UserEntity containing user metadata
 * @throws IllegalArgumentException if userId is null or empty
 * @throws ResourceNotFoundException if user doesn't exist
 */
UserEntity getUser(String userId);
```

### Dependency Injection

The library uses Guice for dependency injection. To use it in your application:

```java
import com.osrsGoalTracker.goals.dao.GoalsTrackerDao;
import com.osrsGoalTracker.goals.dao.module.GoalsTrackerDaoModule;

// Create injector with the GoalsTrackerDaoModule
Injector injector = Guice.createInjector(new GoalsTrackerDaoModule());

// Get an instance of GoalsTrackerDao
GoalsTrackerDao goalsDao = injector.getInstance(GoalsTrackerDao.class);
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
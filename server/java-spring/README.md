# MongoDB Sample MFlix - Java Spring Boot Backend [DRAFT]

A Spring Boot REST API demonstrating MongoDB CRUD operations using the MongoDB Java Driver with the sample_mflix database.

## Overview

This application provides a REST API for managing movie data from MongoDB's sample_mflix database. It demonstrates:

- Direct usage of the MongoDB Java Driver (not Spring Data MongoDB)
- CRUD operations (Create, Read, Update, Delete)
- Text search functionality
- Filtering, sorting, and pagination
- Comprehensive error handling
- API documentation with Swagger/OpenAPI

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- MongoDB Atlas account or local MongoDB instance with sample_mflix database

## Project Structure

```
server/java-spring/
├── src/
│   ├── main/
│   │   ├── java/com/mongodb/samplemflix/
│   │   │   ├── SampleMflixApplication.java    # Main application class
│   │   │   ├── config/                         # Configuration classes
│   │   │   │   ├── MongoConfig.java           # MongoDB client configuration
│   │   │   │   ├── CorsConfig.java            # CORS configuration
│   │   │   │   └── DatabaseVerification.java  # Startup database verification
│   │   │   ├── controller/                     # REST controllers
│   │   │   ├── service/                        # Business logic layer
│   │   │   ├── repository/                     # Data access layer
│   │   │   ├── model/                          # Domain models and DTOs
│   │   │   ├── exception/                      # Custom exceptions
│   │   │   └── util/                           # Utility classes
│   │   └── resources/
│   │       └── application.properties          # Application configuration
│   └── test/                                   # Test classes
├── pom.xml                                     # Maven dependencies
└── README.md
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd server/java-spring
```

### 2. Configure Environment Variables

Copy the example environment file and update with your MongoDB connection details:

```bash
cp .env.example .env
```

Edit `.env` and set your MongoDB connection string:

```properties
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/?retryWrites=true&w=majority
PORT=3001
CORS_ORIGIN=http://localhost:3000
```

> **Note**: This project uses [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) to automatically load `.env` files, similar to Node.js applications. The `.env` file will be loaded automatically when you run the application.

### 3. Load Sample Data

If you haven't already, load the `sample_mflix` database into your MongoDB instance:

- **MongoDB Atlas**: Use the "Load Sample Dataset" option in your cluster
- **Local MongoDB**: Follow the [MongoDB sample data documentation](https://www.mongodb.com/docs/atlas/sample-data/)

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:3001` (or the port specified in your `.env` file).

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:3001/swagger-ui.html
- **OpenAPI JSON**: http://localhost:3001/api-docs

## API Endpoints

### Movies

- `GET /api/movies` - Get all movies (with filtering, sorting, pagination)
- `GET /api/movies/{id}` - Get a single movie by ID
- `POST /api/movies` - Create a new movie
- `POST /api/movies/batch` - Create multiple movies
- `PUT /api/movies/{id}` - Update a movie
- `PATCH /api/movies` - Update multiple movies
- `DELETE /api/movies/{id}` - Delete a movie
- `DELETE /api/movies` - Delete multiple movies
- `DELETE /api/movies/{id}/find-and-delete` - Find and delete a movie

> **Note**: Full endpoint implementation is planned for later phases. See the implementation plan for details.

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

### Building for Production

```bash
mvn clean package
java -jar target/sample-mflix-spring-1.0.0.jar
```

## Implementation Status

This project is being implemented in phases:

- ✅ **Phase 1**: Project Setup and Configuration (CURRENT)
- ⏳ **Phase 2**: Database Configuration and Connection
- ⏳ **Phase 3**: Model Layer Implementation
- ⏳ **Phase 4**: Repository Layer
- ⏳ **Phase 5**: Service Layer
- ⏳ **Phase 6**: Controller Layer
- ⏳ **Phase 7**: Error Handling
- ⏳ **Phase 8**: Testing
- ⏳ **Phase 9**: Documentation and Polish

See `JAVA-SPRING-IMPLEMENTATION-PLAN.md` in the repository root for the complete implementation plan.

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 17
- **MongoDB Driver**: MongoDB Java Driver 5.1.4 (Sync)
- **Build Tool**: Maven
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Educational Purpose

This application is designed as an educational sample to demonstrate:

1. How to use the MongoDB Java Driver directly (without Spring Data MongoDB)
2. Best practices for Spring Boot REST API development
3. Proper separation of concerns (Controller → Service → Repository)
4. MongoDB CRUD operations and query patterns
5. Error handling and validation in Spring Boot

## Troubleshooting

### Connection Issues

If you encounter connection issues:

1. Verify your `MONGODB_URI` is correct
2. Check that your IP address is whitelisted in MongoDB Atlas
3. Ensure the sample_mflix database exists and contains data
4. Check the application logs for detailed error messages

### Build Issues

If Maven build fails:

1. Ensure you have Java 17 or later installed: `java -version`
2. Ensure Maven is installed: `mvn -version`
3. Clear Maven cache: `mvn clean`
4. Try rebuilding: `mvn clean install`

## License

[TBD]

## Contributing

[TBD]

## Issues

[TBD]

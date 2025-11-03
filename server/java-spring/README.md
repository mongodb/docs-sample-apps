# sample-app-java-mflix (INTERNAL)

A Spring Boot REST API demonstrating MongoDB CRUD operations using Spring Data MongoDB with the sample_mflix database.

## Overview

This application provides a REST API for managing movie data from MongoDB's sample_mflix database. It demonstrates:

- Spring Data MongoDB for simplified data access
- CRUD operations (Create, Read, Update, Delete)
- MongoDB Atlas Search with multi-field search and compound operators
- Filtering, sorting, and pagination
- Comprehensive error handling
- API documentation with Swagger/OpenAPI
- MongoTemplate for complex queries and aggregation pipelines

## Prerequisites

- Java 21 or later
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

### Movies (✅ Implemented)

#### CRUD Operations
- `GET /api/movies` - Get all movies (with filtering, sorting, pagination)
- `GET /api/movies/{id}` - Get a single movie by ID
- `POST /api/movies` - Create a new movie
- `POST /api/movies/batch` - Create multiple movies
- `PATCH /api/movies/{id}` - Update a movie (partial update)
- `PATCH /api/movies` - Update multiple movies
- `DELETE /api/movies/{id}` - Delete a movie
- `DELETE /api/movies` - Delete multiple movies
- `DELETE /api/movies/{id}/find-and-delete` - Find and delete a movie

#### Aggregations
- `GET /api/movies/aggregations/comments` - Get movies with most comments
- `GET /api/movies/aggregations/years` - Aggregate movies by year with statistics
- `GET /api/movies/aggregations/directors` - Aggregate directors with most movies

#### Atlas Search
- `GET /api/movies/search` - Search movies using MongoDB Atlas Search

  **Query Parameters:**
  - `plot` (optional) - Search in plot field using phrase matching
  - `fullplot` (optional) - Search in fullplot field using phrase matching
  - `directors` (optional) - Search in directors field with fuzzy matching
  - `writers` (optional) - Search in writers field with fuzzy matching
  - `cast` (optional) - Search in cast field with fuzzy matching
  - `searchOperator` (optional) - Compound operator: `must` (default), `should`, `mustNot`, `filter`
  - `limit` (optional) - Maximum results to return (default: 20, max: 100)
  - `skip` (optional) - Number of results to skip for pagination (default: 0)

  **Examples:**
  ```bash
  # Search by plot
  GET /api/movies/search?plot=space+adventure

  # Search by multiple fields with AND logic
  GET /api/movies/search?directors=Coppola&cast=Pacino&searchOperator=must

  # Search by multiple fields with OR logic
  GET /api/movies/search?plot=crime&directors=Scorsese&searchOperator=should

  # Search with pagination
  GET /api/movies/search?cast=Tom+Hanks&limit=10&skip=20
  ```

  **Note:** At least one search field must be provided. The `searchOperator` determines how multiple search criteria are combined:
  - `must` - All criteria must match (AND logic)
  - `should` - At least one criterion should match (OR logic)
  - `mustNot` - Criteria must not match (NOT logic)
  - `filter` - Criteria must match but don't affect scoring

#### Vector Search
- `GET /api/movies/find-similar-movies` - Find similar movies using vector search on plot embeddings

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

### Completed Features

- **Movies CRUD API** - Full create, read, update, delete operations
- **Spring Data MongoDB** - Repository pattern with MongoTemplate for complex queries
- **MongoDB Atlas Search** - Multi-field search with compound operators (must, should, mustNot, filter)
  - Phrase matching on plot and fullplot fields
  - Fuzzy text matching on directors, writers, and cast fields
  - Support for complex search queries with multiple criteria
- **MongoDB Aggregations** - Statistical aggregations by year, directors, and comments
- **Vector Search** - Find similar movies using plot embeddings (requires Atlas Vector Search)
- **Filtering & Pagination** - Query parameters for filtering, sorting, and pagination
- **Custom Exception Handling** - Global exception handler with proper HTTP status codes
- **Type-Safe DTOs** - Specific response types instead of generic Maps
- **Comprehensive Testing** - 60 tests covering service, controller, and integration layers
  - 29 controller unit tests
  - 27 service unit tests
  - 4 Atlas Search integration tests (requires Atlas cluster)
- **OpenAPI Documentation** - Swagger UI available at `/swagger-ui.html`
- **Database Verification** - Startup checks for database connectivity and indexes


## Technology Stack

- **Framework**: Spring Boot 3.5.7
- **Java Version**: 21
- **MongoDB**: Spring Data MongoDB 4.5.5
- **Build Tool**: Maven
- **API Documentation**: SpringDoc OpenAPI 2.8.13
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Educational Purpose

This application is designed as an educational sample to demonstrate:

1. How to use Spring Data MongoDB for simplified data access
2. Best practices for Spring Boot REST API development
3. Proper separation of concerns (Controller → Service → Repository)
4. MongoDB CRUD operations and query patterns
5. **MongoDB Atlas Search** - Multi-field text search with compound operators
   - Phrase matching for exact phrase searches
   - Fuzzy text matching for typo-tolerant searches
   - Compound operators (must, should, mustNot, filter) for complex queries
6. **MongoDB Aggregation Pipelines** - Statistical aggregations and data transformations
7. **Vector Search** - Semantic similarity search using embeddings
8. Error handling and validation in Spring Boot
9. Using MongoTemplate for complex queries alongside Spring Data repositories
10. Comprehensive testing strategies (unit tests, integration tests)

## Troubleshooting

### Connection Issues

If you encounter connection issues:

1. Verify your `MONGODB_URI` is correct
2. Check that your IP address is whitelisted in MongoDB Atlas
3. Ensure the sample_mflix database exists and contains data
4. Check the application logs for detailed error messages

### Build Issues

If Maven build fails:

1. Ensure you have Java 21 or later installed: `java -version`
2. Ensure Maven is installed: `mvn -version`
3. Clear Maven cache: `mvn clean`
4. Try rebuilding: `mvn clean install`

## License

[TBD]

## Contributing

[TBD]

## Issues

[TBD]

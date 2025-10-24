# Java Spring Backend Implementation Plan

## Executive Summary

This document outlines the comprehensive implementation plan for translating the Express.js/TypeScript backend into a Java Spring Boot application. The implementation will maintain feature parity with the existing Express backend while following Spring Boot best practices and the MongoDB Java Driver conventions.

---

## 1. Project Overview

### 1.1 Current State Analysis

**Existing Express Backend Features:**
- ✅ Basic CRUD operations (insertOne, insertMany, findOne, find, updateOne, updateMany, deleteOne, deleteMany, findOneAndDelete)
- ✅ MongoDB connection management with pre-flight verification
- ✅ Text search index creation and verification
- ✅ Comprehensive error handling
- ✅ Request validation
- ✅ CORS configuration
- ✅ Environment variable configuration
- ✅ Unit tests with Jest

**Python Backend Status:**
- Partially implemented (only GET and batch POST endpoints)
- Uses FastAPI with PyMongo driver
- Async/await pattern

### 1.2 Target Architecture

**Java Spring Boot Stack:**
- **Framework:** Spring Boot 3.x
- **MongoDB Driver:** MongoDB Java Driver (latest stable version)
- **Build Tool:** Maven or Gradle
- **Java Version:** Java 17 or 21 (LTS)
- **Testing:** JUnit 5 + Mockito + Spring Boot Test
- **Documentation:** SpringDoc OpenAPI (Swagger)

---

## 2. Project Structure

### 2.1 Directory Layout

```
server/java-spring/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── mongodb/
│   │   │           └── samplemflix/
│   │   │               ├── SampleMflixApplication.java
│   │   │               ├── config/
│   │   │               │   ├── MongoConfig.java
│   │   │               │   ├── CorsConfig.java
│   │   │               │   └── DatabaseVerification.java
│   │   │               ├── controller/
│   │   │               │   └── MovieController.java
│   │   │               ├── service/
│   │   │               │   └── MovieService.java
│   │   │               ├── repository/
│   │   │               │   └── MovieRepository.java
│   │   │               ├── model/
│   │   │               │   ├── Movie.java
│   │   │               │   ├── Theater.java
│   │   │               │   ├── Comment.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── CreateMovieRequest.java
│   │   │               │   │   ├── UpdateMovieRequest.java
│   │   │               │   │   └── MovieSearchQuery.java
│   │   │               │   └── response/
│   │   │               │       ├── ApiResponse.java
│   │   │               │       ├── SuccessResponse.java
│   │   │               │       └── ErrorResponse.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── ValidationException.java
│   │   │               │   └── ResourceNotFoundException.java
│   │   │               └── util/
│   │   │                   └── ValidationUtils.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
│       └── java/
│           └── com/
│               └── mongodb/
│                   └── samplemflix/
│                       ├── controller/
│                       │   └── MovieControllerTest.java
│                       ├── service/
│                       │   └── MovieServiceTest.java
│                       └── integration/
│                           └── MovieIntegrationTest.java
├── pom.xml (or build.gradle)
├── README.md
└── .env.example
```

---

## 3. Implementation Phases

### Phase 1: Project Setup and Configuration (Days 1-2)

#### 1.1 Initialize Spring Boot Project
- [ ] Create Spring Boot project using Spring Initializr
- [ ] Configure Maven/Gradle with required dependencies
- [ ] Set up project structure following Spring conventions
- [ ] Configure `.gitignore` for Java/Spring projects

#### 1.2 Dependencies Configuration

**Required Dependencies:**
```xml
<!-- Core Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- MongoDB Driver -->
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok (optional, for reducing boilerplate) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- SpringDoc OpenAPI (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

#### 1.3 Environment Configuration
- [ ] Create `application.properties` with MongoDB connection settings
- [ ] Create `.env.example` file documenting required environment variables
- [ ] Implement environment variable loading (Spring Boot handles this natively)
- [ ] Configure CORS settings

**application.properties:**
```properties
# MongoDB Configuration
spring.data.mongodb.uri=${MONGODB_URI}
spring.data.mongodb.database=sample_mflix

# Server Configuration
server.port=${PORT:3001}

# CORS Configuration
cors.allowed.origins=${CORS_ORIGIN:http://localhost:3000}

# Application Info
spring.application.name=MongoDB Sample MFlix API
```

---

### Phase 2: Database Configuration and Connection (Days 2-3) ✅ COMPLETE

#### 2.1 MongoDB Configuration Class ✅
- [x] Create `MongoConfig.java` to configure MongoDB client
- [x] Implement connection pooling and timeout settings
- [x] Add connection lifecycle management

**Key Features:**
- ✅ Singleton MongoClient instance
- ✅ Connection string validation
- ✅ Database reference management
- ✅ Graceful shutdown handling (managed by Spring)
- ✅ Connection pool settings (max 100, min 10 connections)
- ✅ Socket timeouts (10s connect, 10s read)
- ✅ Server selection timeout (10s)

#### 2.2 Database Verification Service ✅
- [x] Create `DatabaseVerification.java` as a Spring component
- [x] Implement `@PostConstruct` method for startup verification
- [x] Check for movies collection and document count
- [x] Create text search indexes if missing
- [x] Log verification results

**Verification Checklist:**
- ✅ Movies collection exists
- ✅ Movies collection has documents (with warning if empty)
- ✅ Text search index on plot, title, fullplot fields
- ✅ Log warnings if data is missing
- ✅ Non-blocking verification (app starts even if verification fails)

---

### Phase 3: Model Layer Implementation (Days 3-4) ✅ COMPLETE

#### 3.1 Domain Models ✅
- [x] Create `Movie.java` entity class
- [x] Create `Theater.java` entity class
- [x] Create `Comment.java` entity class
- [x] Add BSON annotations for MongoDB mapping (using Lombok)
- [x] Implement nested objects (Awards, IMDB, Tomatoes)

**Movie.java Structure:** ✅
- ✅ All fields from TypeScript Movie interface
- ✅ Nested class: Awards (wins, nominations, text)
- ✅ Nested class: Imdb (rating, votes, id)
- ✅ Nested class: Tomatoes (viewer, critic, fresh, rotten, production, lastUpdated)
- ✅ Nested classes: Tomatoes.Viewer and Tomatoes.Critic
- ✅ Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- ✅ Comprehensive JavaDoc documentation

**Theater.java Structure:** ✅
- ✅ All fields from TypeScript Theater interface
- ✅ Nested class: Location (address, geo)
- ✅ Nested class: Location.Address (street1, city, state, zipcode)
- ✅ Nested class: Location.Geo (type, coordinates)
- ✅ GeoJSON format support for geospatial queries

**Comment.java Structure:** ✅
- ✅ All fields from TypeScript Comment interface
- ✅ Fields: id, name, email, movieId, text, date

#### 3.2 DTOs (Data Transfer Objects) ✅
- [x] Create `CreateMovieRequest.java`
- [x] Create `UpdateMovieRequest.java`
- [x] Create `MovieSearchQuery.java`
- [x] Add validation annotations (@NotBlank for required fields)

**CreateMovieRequest.java:** ✅
- ✅ Required field: title (with @NotBlank validation)
- ✅ Optional fields: year, plot, fullplot, genres, directors, writers, cast, countries, languages, rated, runtime, poster

**UpdateMovieRequest.java:** ✅
- ✅ All fields optional (for partial updates)
- ✅ Same fields as CreateMovieRequest

**MovieSearchQuery.java:** ✅
- ✅ Full-text search: q
- ✅ Filters: genre, year, minRating, maxRating
- ✅ Pagination: limit, skip
- ✅ Sorting: sortBy, sortOrder

#### 3.3 Response Models ✅
- [x] Create generic `ApiResponse` interface
- [x] Create `SuccessResponse<T>` class
- [x] Create `ErrorResponse` class
- [x] Add timestamp and metadata fields

**ApiResponse Interface:** ✅
- ✅ Methods: isSuccess(), getTimestamp()

**SuccessResponse<T>:** ✅
- ✅ Generic type parameter for data
- ✅ Fields: success (true), message, data, timestamp, pagination
- ✅ Nested class: Pagination (page, limit, total, pages)
- ✅ @JsonInclude(NON_NULL) to exclude null fields

**ErrorResponse:** ✅
- ✅ Fields: success (false), message, error, timestamp
- ✅ Nested class: ErrorDetails (message, code, details)
- ✅ Matches Express backend error format

---

### Phase 4: Repository Layer (Days 4-5)

#### 4.1 Custom Repository Implementation
- [ ] Create `MovieRepository.java` interface
- [ ] Implement custom repository using MongoTemplate or MongoCollection
- [ ] Add methods for all CRUD operations

**Repository Methods:**
```java
public interface MovieRepository {
    // Basic CRUD
    Movie insertOne(Movie movie);
    List<ObjectId> insertMany(List<Movie> movies);
    Optional<Movie> findById(ObjectId id);
    List<Movie> find(Document filter, Document sort, int skip, int limit);
    UpdateResult updateOne(ObjectId id, Document update);
    UpdateResult updateMany(Document filter, Document update);
    DeleteResult deleteOne(ObjectId id);
    DeleteResult deleteMany(Document filter);
    Optional<Movie> findOneAndDelete(ObjectId id);
    
    // Utility
    long countDocuments();
}
```

---

### Phase 5: Service Layer (Days 5-7)

#### 5.1 Movie Service Implementation
- [ ] Create `MovieService.java` with business logic
- [ ] Implement all CRUD operations
- [ ] Add query building logic for filtering
- [ ] Implement pagination and sorting
- [ ] Add validation logic

**Service Responsibilities:**
- Business logic and validation
- Query construction (filters, sorts, pagination)
- Data transformation between DTOs and entities
- Error handling and exception throwing

---

### Phase 6: Controller Layer (Days 7-9)

#### 6.1 REST API Endpoints
- [ ] Create `MovieController.java` with REST endpoints
- [ ] Implement all endpoints matching Express routes
- [ ] Add request validation
- [ ] Add API documentation annotations

**Endpoint Mapping:**
```
GET    /api/movies              -> getAllMovies()
GET    /api/movies/{id}         -> getMovieById()
POST   /api/movies              -> createMovie()
POST   /api/movies/batch        -> createMoviesBatch()
PUT    /api/movies/{id}         -> updateMovie()
PATCH  /api/movies              -> updateMoviesBatch()
DELETE /api/movies/{id}         -> deleteMovie()
DELETE /api/movies              -> deleteMoviesBatch()
DELETE /api/movies/{id}/find-and-delete -> findAndDeleteMovie()
```

#### 6.2 Request/Response Handling
- [ ] Implement consistent response wrapping
- [ ] Add query parameter parsing
- [ ] Implement request body validation
- [ ] Add proper HTTP status codes

---

### Phase 7: Error Handling (Days 9-10)

#### 7.1 Exception Hierarchy
- [ ] Create custom exception classes
- [ ] Implement `GlobalExceptionHandler` with @ControllerAdvice
- [ ] Handle MongoDB-specific exceptions
- [ ] Handle validation exceptions

**Exception Types:**
- `ResourceNotFoundException` (404)
- `ValidationException` (400)
- `DuplicateKeyException` (409)
- `DatabaseException` (500)

#### 7.2 Error Response Format
- [ ] Standardize error response structure
- [ ] Include error codes and messages
- [ ] Add timestamp and request path
- [ ] Log errors appropriately

---

### Phase 8: Testing (Days 10-12)

#### 8.1 Unit Tests
- [ ] Test MovieService methods
- [ ] Test MovieController endpoints
- [ ] Mock repository layer
- [ ] Achieve >80% code coverage

#### 8.2 Integration Tests
- [ ] Test with embedded MongoDB or Testcontainers
- [ ] Test full request/response cycle
- [ ] Test error scenarios
- [ ] Test database operations

#### 8.3 Test Configuration
- [ ] Create test application.properties
- [ ] Set up test fixtures and data
- [ ] Configure test MongoDB instance

---

### Phase 9: Documentation and Polish (Days 12-13)

#### 9.1 Code Documentation
- [ ] Add comprehensive JavaDoc comments
- [ ] Document MongoDB operations clearly
- [ ] Add inline comments explaining driver usage
- [ ] Note deviations from standard practices

#### 9.2 API Documentation
- [ ] Configure Swagger/OpenAPI
- [ ] Add endpoint descriptions
- [ ] Document request/response schemas
- [ ] Add example requests

#### 9.3 README and Setup Guide
- [ ] Create comprehensive README.md
- [ ] Document prerequisites (Java version, Maven/Gradle)
- [ ] Provide setup instructions
- [ ] Document environment variables
- [ ] Add troubleshooting section

---

## 4. Technical Implementation Details

### 4.1 MongoDB Driver Usage Patterns

**Connection Management:**
```java
@Configuration
public class MongoConfig {
    @Bean
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String uri) {
        return MongoClients.create(uri);
    }
    
    @Bean
    public MongoDatabase mongoDatabase(MongoClient client) {
        return client.getDatabase("sample_mflix");
    }
}
```

**Collection Access:**
```java
@Repository
public class MovieRepositoryImpl implements MovieRepository {
    private final MongoCollection<Document> collection;
    
    public MovieRepositoryImpl(MongoDatabase database) {
        this.collection = database.getCollection("movies");
    }
}
```

### 4.2 Query Building Examples

**Text Search:**
```java
Document filter = new Document("$text", new Document("$search", searchQuery));
```

**Range Queries:**
```java
Document ratingFilter = new Document()
    .append("$gte", minRating)
    .append("$lte", maxRating);
filter.append("imdb.rating", ratingFilter);
```

**Sorting:**
```java
Document sort = new Document(sortBy, sortOrder.equals("desc") ? -1 : 1);
```

### 4.3 Error Handling Pattern

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MongoException.class)
    public ResponseEntity<ErrorResponse> handleMongoException(MongoException ex) {
        // Parse MongoDB error codes
        // Return appropriate HTTP status and error response
    }
}
```

---

## 5. Migration Mapping

### 5.1 Express to Spring Equivalents

| Express Concept | Spring Boot Equivalent |
|----------------|------------------------|
| `app.ts` | `SampleMflixApplication.java` (main class) |
| Routes (`movies.ts`) | `@RestController` in `MovieController.java` |
| Controllers | `@Service` in `MovieService.java` |
| `database.ts` | `MongoConfig.java` + `DatabaseVerification.java` |
| `errorHandler.ts` | `GlobalExceptionHandler.java` |
| Types/Interfaces | Java classes with proper annotations |
| `asyncHandler` | Spring handles async automatically |
| `dotenv` | `application.properties` + environment variables |

### 5.2 Code Pattern Translations

**Express Pattern:**
```typescript
router.get("/", asyncHandler(movieController.getAllMovies));
```

**Spring Pattern:**
```java
@GetMapping
public ResponseEntity<SuccessResponse<List<Movie>>> getAllMovies(
    @RequestParam(required = false) String q,
    // ... other params
) {
    // Implementation
}
```

---

## 6. Quality Assurance Checklist

### 6.1 Functional Requirements
- [ ] All CRUD operations work correctly
- [ ] Text search functionality works
- [ ] Filtering and pagination work
- [ ] Sorting works correctly
- [ ] Batch operations work
- [ ] Error handling is comprehensive

### 6.2 Code Quality
- [ ] Code follows Spring Boot best practices
- [ ] Comprehensive comments explaining MongoDB operations
- [ ] Proper separation of concerns (Controller/Service/Repository)
- [ ] No hardcoded values
- [ ] Proper exception handling

### 6.3 Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Code coverage >80%
- [ ] Edge cases tested

### 6.4 Documentation
- [ ] README is comprehensive
- [ ] API documentation is complete
- [ ] Code comments are thorough
- [ ] Setup instructions are clear

---

## 7. Timeline and Milestones

| Phase | Duration | Deliverable |
|-------|----------|-------------|
| Phase 1: Setup | 2 days | Project structure, dependencies configured |
| Phase 2: Database | 1 day | MongoDB connection and verification working |
| Phase 3: Models | 1 day | All model classes implemented |
| Phase 4: Repository | 1 day | Repository layer complete |
| Phase 5: Service | 2 days | Business logic implemented |
| Phase 6: Controller | 2 days | All REST endpoints working |
| Phase 7: Error Handling | 1 day | Comprehensive error handling |
| Phase 8: Testing | 2 days | Tests written and passing |
| Phase 9: Documentation | 1 day | Documentation complete |
| **Total** | **13 days** | **Production-ready Java Spring backend** |

---

## 8. Post-Implementation Tasks

### 8.1 Integration with Frontend
- [ ] Test with existing Next.js frontend
- [ ] Verify CORS configuration
- [ ] Test all API endpoints from frontend
- [ ] Verify response format compatibility

### 8.2 Performance Optimization
- [ ] Add connection pooling configuration
- [ ] Optimize query performance
- [ ] Add caching if needed
- [ ] Profile and optimize hot paths

### 8.3 Deployment Preparation
- [ ] Create Docker configuration
- [ ] Document deployment process
- [ ] Create production configuration
- [ ] Add health check endpoints

---

## 9. Success Criteria

The Java Spring implementation will be considered complete when:

1. ✅ All CRUD operations match Express functionality
2. ✅ All tests pass with >80% coverage
3. ✅ API responses match Express format exactly
4. ✅ Frontend works without modifications
5. ✅ Documentation is comprehensive
6. ✅ Code follows Spring Boot best practices
7. ✅ MongoDB operations are well-documented
8. ✅ Error handling is robust
9. ✅ Pre-flight verification works
10. ✅ README provides clear setup instructions

---

## 10. Notes and Considerations

### 10.1 Design Decisions

**Why not Spring Data MongoDB?**
- The scope requires direct MongoDB Driver usage to demonstrate driver operations
- Spring Data MongoDB abstracts away driver details
- Educational value is in showing raw driver usage

**Lombok Usage:**
- Optional but recommended for reducing boilerplate
- Helps maintain readability
- Can be removed if team prefers explicit code

**Testing Strategy:**
- Unit tests for service layer
- Integration tests for full stack
- Consider Testcontainers for realistic MongoDB testing

### 10.2 Future Enhancements (Post-MVP)

- [ ] Aggregation pipeline endpoints
- [ ] Full-text search with Atlas Search
- [ ] Vector search implementation
- [ ] Geospatial queries for theaters
- [ ] Caching layer
- [ ] Rate limiting
- [ ] Metrics and monitoring

---

## Appendix A: Key Files Reference

### Express Backend Files to Reference
- `server/express/src/app.ts` - Main application setup
- `server/express/src/config/database.ts` - Database configuration
- `server/express/src/controllers/movieController.ts` - Business logic
- `server/express/src/routes/movies.ts` - Route definitions
- `server/express/src/types/index.ts` - Type definitions
- `server/express/src/utils/errorHandler.ts` - Error handling

### Python Backend Files (for comparison)
- `server/python/main.py` - FastAPI application
- `server/python/src/routers/movies.py` - Route handlers
- `server/python/src/database/mongo_client.py` - Database connection

---

## Appendix B: Environment Variables

Required environment variables for Java Spring backend:

```properties
# MongoDB Connection
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/?retryWrites=true&w=majority

# Server Configuration
PORT=3001

# CORS Configuration
CORS_ORIGIN=http://localhost:3000
```

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-23  
**Author:** Implementation Plan Generator  
**Status:** Ready for Implementation

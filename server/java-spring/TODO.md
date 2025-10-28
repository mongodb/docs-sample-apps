# TODO List - Java Spring Boot MongoDB Sample Application

This document tracks remaining functionality and improvements for the Java Spring Boot backend based on the **Requirements for Sample Repositories for MongoDB Atlas** and **Sample Application Scoping** documents.

## 📋 Scope Alignment

This sample application must demonstrate:
- ✅ **Basic CRUD** - insertOne(), insertMany(), findOne(), find(), updateOne(), updateMany(), deleteOne(), deleteMany(), findOneAndDelete()
- 🚧 **Aggregations** - Reporting by comments, year, and director
- 🚧 **Search** - Full-text search using Search Index on plot
- 🚧 **Vector Search** - Find similar movies based on plot embeddings
- 🚧 **Geospatial** - Find theaters near coordinates
- 🚧 **Recommendations** - Potentially using Vector Search

## ✅ Completed Features (In Scope)

- [x] **Basic CRUD operations for Movies collection**
  - [x] insertOne() - `POST /api/movies`
  - [x] insertMany() - `POST /api/movies/batch`
  - [x] findOne() - `GET /api/movies/{id}`
  - [x] find() - `GET /api/movies`
  - [x] updateOne() - `PUT /api/movies/{id}`
  - [x] updateMany() - `PATCH /api/movies`
  - [x] deleteOne() - `DELETE /api/movies/{id}`
  - [x] deleteMany() - `DELETE /api/movies`
  - [x] findOneAndDelete() - `DELETE /api/movies/{id}/find-and-delete`
- [x] Spring Data MongoDB migration
- [x] Filtering, sorting, and pagination
- [x] Custom exception handling
- [x] Type-safe response DTOs
- [x] Unit tests for service and controller layers (35 tests)
- [x] OpenAPI/Swagger documentation
- [x] Database verification on startup (checks indexes and data)
- [x] CORS configuration
- [x] Field name constants for type safety
- [x] Environment variable configuration (.env support)

## 🚧 High Priority TODOs (Required by Scope)

### 1. Aggregations (REQUIRED)

**Scope Requirement**: Implement aggregation pipelines for reporting

- [ ] **GET /api/movies/reportingByComments** - Aggregate movies with most comments
  - [ ] Create aggregation pipeline using `$lookup` to join with comments collection
  - [ ] Sort by comment count descending
  - [ ] Add pagination support
  - [ ] Add unit tests

- [ ] **GET /api/movies/reportingByYear** - Aggregate movies by year with average rating
  - [ ] Create aggregation pipeline grouping by year
  - [ ] Calculate average IMDB rating per year
  - [ ] Sort by year
  - [ ] Add unit tests

- [ ] **GET /api/movies/reportingByDirector** - Aggregate directors with most movies
  - [ ] Create aggregation pipeline using `$unwind` on directors array
  - [ ] Group by director and count movies
  - [ ] Sort by movie count descending
  - [ ] Add pagination support
  - [ ] Add unit tests

### 2. Search (REQUIRED)

**Scope Requirement**: Implement Full-Text Search and Vector Search

- [ ] **GET /api/movies/searchByPlot** - Text Search using Search Index
  - [ ] Verify text index exists on plot, fullplot, genres, title fields
  - [ ] Implement text search using Spring Data MongoDB TextCriteria
  - [ ] Add relevance scoring
  - [ ] Add pagination support
  - [ ] Add unit tests
  - [ ] Document index requirements in README

- [ ] **GET /api/movies/findSimilarMovies** - Vector Search for similar movies
  - [ ] Implement vector search on `plot_embedding` field
  - [ ] Use MongoDB Atlas Vector Search
  - [ ] Add similarity threshold parameter
  - [ ] Add limit parameter
  - [ ] Add unit tests
  - [ ] Document vector search index requirements

### 3. Geospatial (REQUIRED)

**Scope Requirement**: Find theaters near coordinates

- [ ] **GET /api/theaters/findTheatersNearCoordinates** - Geospatial query
  - [ ] Create `Theater` entity (already exists, needs repository)
  - [ ] Create `TheaterRepository` with geospatial query methods
  - [ ] Create `TheaterService` and `TheaterServiceImpl`
  - [ ] Create `TheaterController` with endpoint
  - [ ] Implement `$near` or `$geoNear` query
  - [ ] Add distance parameter (in meters/miles)
  - [ ] Add limit parameter
  - [ ] Verify geospatial index exists on `location.geo`
  - [ ] Add unit tests
  - [ ] Add integration tests

### 4. Recommendations (OPTIONAL - FUTURE)

**Scope Requirement**: Potentially use Vector Search to power recommendations

- [ ] Design recommendation algorithm
- [ ] Implement using Vector Search on plot embeddings
- [ ] Consider user preferences/history
- [ ] Add endpoint `GET /api/movies/recommendations`
- [ ] Add tests

### 5. Comments Collection Support (For Aggregations)

**Note**: Required to support "reportingByComments" aggregation

- [ ] Verify `Comment` entity model (already exists)
- [ ] Create `CommentRepository` interface
- [ ] Create `CommentService` and `CommentServiceImpl`
- [ ] Create `CommentController` with basic endpoints:
  - `GET /api/comments/movie/{movieId}` - Get comments for a specific movie
  - `POST /api/comments` - Create a new comment
- [ ] Add tests for Comment functionality

### 6. Testing (REQUIRED)

**Scope Requirement**: Unit tests and E2E tests

- [ ] **Unit Tests** - Test individual features and driver interactions
  - [x] Service layer unit tests (21 tests completed)
  - [x] Controller layer unit tests (14 tests completed)
  - [ ] Add tests for aggregation methods
  - [ ] Add tests for search methods
  - [ ] Add tests for geospatial queries
  - [ ] Add tests for vector search

- [ ] **Integration Tests** - Test complete flows
  - [ ] Set up Testcontainers for MongoDB
  - [ ] Create integration tests for all CRUD endpoints
  - [ ] Create integration tests for aggregations
  - [ ] Create integration tests for search
  - [ ] Create integration tests for geospatial queries
  - [ ] Test error handling and edge cases

### 7. Error Handling & Verification (REQUIRED)

**Scope Requirement**: Pre-flight checks and error handling

- [x] Verify requirements function on app start (DatabaseVerification.java)
  - [x] Check for indexes
  - [x] Check for sample data
  - [x] Create indexes if missing
- [x] Basic error handling implemented
  - [x] Connection errors
  - [x] Invalid input validation
  - [x] Custom exceptions (ValidationException, ResourceNotFoundException, DatabaseOperationException)
  - [x] Global exception handler
- [ ] Enhance error handling for new features:
  - [ ] Vector search errors
  - [ ] Geospatial query errors
  - [ ] Aggregation pipeline errors

### 8. Documentation (REQUIRED)

**Scope Requirement**: Clear README and documentation

- [x] README.md with prerequisites and setup
- [x] Environment variable configuration (.env.example)
- [x] OpenAPI/Swagger documentation
- [ ] Update README with:
  - [ ] Aggregation endpoint documentation
  - [ ] Search endpoint documentation
  - [ ] Vector search setup instructions
  - [ ] Geospatial query examples
  - [ ] Index requirements
- [ ] Add code comments for MongoDB features
  - [ ] Comment aggregation pipelines
  - [ ] Comment vector search implementation
  - [ ] Comment geospatial queries
  - [ ] Note where readability was prioritized over performance

## 📝 Medium Priority TODOs (Out of Scope - Future Enhancements)

### 9. Additional Collections (Optional)

**Note**: These collections are not required by the current scope but may be useful for future enhancements.

#### Users Collection (`sample_mflix.users`)
- [ ] Create `UserRepository` interface (model already exists)
- [ ] Create `UserService` and `UserServiceImpl`
- [ ] Implement password hashing (BCrypt)
- [ ] Add email validation
- [ ] Add tests for User functionality

#### Sessions Collection (`sample_mflix.sessions`)
- [ ] Create `SessionRepository` interface (model already exists)
- [ ] Create `SessionService` and `SessionServiceImpl`
- [ ] Implement JWT token generation and validation
- [ ] Add tests for Session functionality

#### Embedded Movies Collection (`sample_mflix.embedded_movies`)
- [ ] Investigate purpose and schema of this collection
- [ ] Determine if separate endpoints are needed
- [ ] Document relationship to main `movies` collection

### 10. Authentication & Authorization (Out of Scope)

**Note**: Not required by current scope, but useful for production applications

- [ ] Implement Spring Security
- [ ] Add JWT authentication filter
- [ ] Create `@PreAuthorize` annotations for role-based access
- [ ] Implement user roles (USER, ADMIN)
- [ ] Add authentication to existing endpoints
- [ ] Create authentication integration tests

### 11. API Improvements (Out of Scope)

- [ ] Add HATEOAS links to responses
- [ ] Implement API versioning (e.g., `/api/v1/movies`)
- [ ] Add request/response compression
- [ ] Implement rate limiting
- [ ] Add API key authentication option
- [ ] Create GraphQL endpoint as alternative to REST

### 12. Data Validation & Quality (Partially Complete)

- [x] Basic Jakarta Validation annotations
- [ ] Create custom validators for complex business rules
- [ ] Implement data sanitization for user inputs
- [ ] Add validation for embedded documents
- [ ] Create validation tests

### 13. Performance Optimization (Out of Scope)

**Note**: Scope prioritizes readability over performance

- [ ] Implement caching with Spring Cache (Redis/Caffeine)
- [ ] Add database query optimization
- [ ] Implement connection pooling tuning
- [ ] Create performance benchmarks
- [ ] Implement lazy loading for large collections
- [ ] Document performance vs readability tradeoffs

### 14. Monitoring & Observability (Out of Scope)

- [ ] Add Spring Boot Actuator endpoints
- [ ] Implement custom health checks
- [ ] Add metrics collection (Micrometer)
- [ ] Integrate with Prometheus/Grafana
- [ ] Add distributed tracing (Sleuth/Zipkin)
- [ ] Implement structured logging (JSON format)

### 15. Code Quality (Partially Complete)

- [x] Unit tests for core functionality (35 tests)
- [ ] Increase test coverage to >80%
- [ ] Add code coverage reporting (JaCoCo)
- [ ] Add mutation testing (PIT)
- [ ] Integrate SonarQube for code quality analysis
- [ ] Add Checkstyle/PMD for code style enforcement

### 16. Build & Deployment (Out of Scope)

**Note**: Deployment is handled by Growth team per scope document

- [ ] Create Dockerfile for containerization
- [ ] Add Docker Compose for local development
- [ ] Create Kubernetes deployment manifests
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [x] Automated dependency updates (Dependabot) - handled by DevDocs team

### 17. Security Enhancements (Out of Scope)

**Note**: Basic security implemented, advanced features out of scope

- [x] CORS configuration
- [x] Input validation
- [ ] Implement HTTPS/TLS
- [ ] Add CSRF protection
- [ ] Implement security headers (HSTS, CSP, etc.)
- [ ] Add security scanning (OWASP Dependency Check)

### 18. Developer Experience (Partially Complete)

- [x] Environment variable configuration
- [x] Database verification on startup
- [x] OpenAPI/Swagger documentation
- [ ] Add Spring Boot DevTools for hot reload
- [ ] Create seed data script for development
- [ ] Add API client examples (Postman collection)

## 🐛 Known Issues

- [ ] Update deprecated `@MockBean` to new Spring Boot 3.5+ alternative (warning in tests)

## 📚 Technical Debt

- None currently - code follows best practices and scope requirements

## 🎯 Next Immediate Steps (Priority Order)

Based on the **Sample Application Scoping** document, the next steps are:

### Phase 1: Complete Required Features (In Scope)
1. **Aggregations** (REQUIRED)
   - Implement `GET /api/movies/reportingByComments`
   - Implement `GET /api/movies/reportingByYear`
   - Implement `GET /api/movies/reportingByDirector`

2. **Search** (REQUIRED)
   - Implement `GET /api/movies/searchByPlot` (Full-Text Search)
   - Implement `GET /api/movies/findSimilarMovies` (Vector Search)

3. **Geospatial** (REQUIRED)
   - Implement `GET /api/theaters/findTheatersNearCoordinates`
   - Create Theater repository and service

4. **Comments Collection** (REQUIRED for aggregations)
   - Create CommentRepository and CommentService
   - Implement basic endpoints to support aggregations

5. **Testing** (REQUIRED)
   - Add unit tests for all new features
   - Create integration tests with Testcontainers

6. **Documentation** (REQUIRED)
   - Update README with new endpoints
   - Document index requirements
   - Add code comments explaining MongoDB features
   - Note readability vs performance tradeoffs

### Phase 2: Quality Assurance (In Scope)
7. **Error Handling** - Enhance for new features
8. **Code Comments** - Add more comments than typical production app
9. **Readability Review** - Ensure code prioritizes clarity over performance

### Phase 3: Future Enhancements (Out of Scope)
10. Recommendations using Vector Search
11. Authentication & Authorization
12. Additional collections (Users, Sessions)
13. Performance optimizations
14. Advanced features

## 📋 Scope Compliance Checklist

### Required Features
- [x] **Basic CRUD** - All methods implemented ✅
  - [x] insertOne(), insertMany()
  - [x] findOne(), find()
  - [x] updateOne(), updateMany()
  - [x] deleteOne(), deleteMany(), findOneAndDelete()
- [ ] **Aggregations** - Not yet implemented ⏳
  - [ ] Reporting by comments
  - [ ] Reporting by year
  - [ ] Reporting by director
- [ ] **Search** - Not yet implemented ⏳
  - [ ] Full-text search on plot
- [ ] **Vector Search** - Not yet implemented ⏳
  - [ ] Find similar movies
- [ ] **Geospatial** - Not yet implemented ⏳
  - [ ] Find theaters near coordinates

### Quality Standards
- [x] **Error Handling** - Basic implementation complete ✅
- [x] **Testing** - Unit tests complete (35 tests) ✅
- [ ] **Integration Tests** - Pending ⏳
- [x] **Documentation** - README complete ✅
- [ ] **Documentation Updates** - Needs updates for new features ⏳
- [x] **Readability** - Code follows best practices ✅
- [x] **Code Comments** - Good coverage, needs more for complex features ⏳
- [x] **Minimal Dependencies** - Using only Spring Boot and MongoDB ✅
- [x] **Environment Variables** - Implemented with .env.example ✅
- [x] **Apache License** - Applied ✅
- [x] **Conventional Commits** - Following standard ✅
- [x] **Clean Commit History** - Maintained ✅

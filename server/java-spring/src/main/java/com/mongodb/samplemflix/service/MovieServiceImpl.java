package com.mongodb.samplemflix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.samplemflix.exception.DatabaseOperationException;
import com.mongodb.samplemflix.exception.ResourceNotFoundException;
import com.mongodb.samplemflix.exception.ValidationException;
import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.BatchInsertResponse;
import com.mongodb.samplemflix.model.dto.BatchUpdateResponse;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.DeleteResponse;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.repository.MovieRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Service layer for movie business logic using Spring Data MongoDB.
 *
 * <p>This service handles:
 * <pre>
 * - Business logic and validation
 * - Query construction using Spring Data MongoDB Query API
 * - Data transformation between DTOs and entities
 * - Error handling and exception throwing
 * </pre>
 * Uses both:
 * <pre>
 * - MovieRepository (Spring Data) for simple CRUD operations
 * - MongoTemplate for complex queries and batch operations
 * </pre>
 */
@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public MovieServiceImpl(MovieRepository movieRepository, MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public List<Movie> getAllMovies(MovieSearchQuery query) {
        Query mongoQuery = buildQuery(query);

        int limit = Math.clamp(query.getLimit() != null ? query.getLimit() : 20, 1, 100);
        int skip = Math.max(query.getSkip() != null ? query.getSkip() : 0, 0);

        mongoQuery.skip(skip).limit(limit);
        mongoQuery.with(buildSort(query.getSortBy(), query.getSortOrder()));

        return mongoTemplate.find(mongoQuery, Movie.class);
    }
    
    @Override
    public Movie getMovieById(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ValidationException("Invalid movie ID format");
        }
        
        return movieRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
    }
    
    @Override
    public Movie createMovie(CreateMovieRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .year(request.getYear())
                .plot(request.getPlot())
                .fullplot(request.getFullplot())
                .genres(request.getGenres())
                .directors(request.getDirectors())
                .writers(request.getWriters())
                .cast(request.getCast())
                .countries(request.getCountries())
                .languages(request.getLanguages())
                .rated(request.getRated())
                .runtime(request.getRuntime())
                .poster(request.getPoster())
                .build();

        // Spring Data MongoDB's save() method inserts or updates
        return movieRepository.save(movie);
    }
    
    @Override
    public BatchInsertResponse createMoviesBatch(List<CreateMovieRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ValidationException("Request body must be a non-empty array of movie objects");
        }

        for (int i = 0; i < requests.size(); i++) {
            CreateMovieRequest request = requests.get(i);
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                throw new ValidationException("Movie at index " + i + ": Title is required");
            }
        }

        List<Movie> movies = requests.stream()
                .map(request -> Movie.builder()
                        .title(request.getTitle())
                        .year(request.getYear())
                        .plot(request.getPlot())
                        .fullplot(request.getFullplot())
                        .genres(request.getGenres())
                        .directors(request.getDirectors())
                        .writers(request.getWriters())
                        .cast(request.getCast())
                        .countries(request.getCountries())
                        .languages(request.getLanguages())
                        .rated(request.getRated())
                        .runtime(request.getRuntime())
                        .poster(request.getPoster())
                        .build())
                .toList();

        // Spring Data MongoDB's saveAll() method for batch insert
        List<Movie> savedMovies = movieRepository.saveAll(movies);

        // Extract IDs from saved movies
        Collection<BsonValue> insertedIds = savedMovies.stream()
                .map(movie -> new org.bson.BsonObjectId(movie.getId()))
                .collect(Collectors.toList());

        return new BatchInsertResponse(
                savedMovies.size(),
                insertedIds
        );
    }
    
    @Override
    public Movie updateMovie(String id, UpdateMovieRequest request) {
        if (!ObjectId.isValid(id)) {
            throw new ValidationException("Invalid movie ID format");
        }

        if (request == null || isUpdateRequestEmpty(request)) {
            throw new ValidationException("No update data provided");
        }

        ObjectId objectId = new ObjectId(id);

        // Build Spring Data MongoDB Update object
        Update update = buildUpdate(request);

        // Use MongoTemplate for update operation
        Query query = new Query(Criteria.where("_id").is(objectId));
        UpdateResult result = mongoTemplate.updateFirst(query, update, Movie.class);

        if (result.getMatchedCount() == 0) {
            throw new ResourceNotFoundException("Movie not found");
        }

        return movieRepository.findById(objectId)
                .orElseThrow(() -> new DatabaseOperationException("Failed to retrieve updated movie"));
    }
    
    @Override
    public BatchUpdateResponse updateMoviesBatch(Document filter, Document update) {
        if (filter == null || update == null) {
            throw new ValidationException("Both filter and update objects are required");
        }

        if (update.isEmpty()) {
            throw new ValidationException("Update object cannot be empty");
        }

        // Convert Document filter to Spring Data Query
        Query query = new Query();
        filter.forEach((key, value) -> query.addCriteria(Criteria.where(key).is(value)));

        // Convert Document update to Spring Data Update
        Update mongoUpdate = new Update();
        update.forEach(mongoUpdate::set);

        UpdateResult result = mongoTemplate.updateMulti(query, mongoUpdate, Movie.class);

        return new BatchUpdateResponse(
                result.getMatchedCount(),
                result.getModifiedCount()
        );
    }
    
    @Override
    public DeleteResponse deleteMovie(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ValidationException("Invalid movie ID format");
        }

        ObjectId objectId = new ObjectId(id);

        // Check if movie exists before deleting
        if (!movieRepository.existsById(objectId)) {
            throw new ResourceNotFoundException("Movie not found");
        }

        movieRepository.deleteById(objectId);

        return new DeleteResponse(1L);
    }
    
    @Override
    public DeleteResponse deleteMoviesBatch(Document filter) {
        if (filter == null || filter.isEmpty()) {
            throw new ValidationException("Filter object is required and cannot be empty. This prevents accidental deletion of all documents.");
        }

        // Convert Document filter to Spring Data Query
        Query query = new Query();
        filter.forEach((key, value) -> query.addCriteria(Criteria.where(key).is(value)));

        DeleteResult result = mongoTemplate.remove(query, Movie.class);

        return new DeleteResponse(result.getDeletedCount());
    }
    
    @Override
    public Movie findAndDeleteMovie(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ValidationException("Invalid movie ID format");
        }

        ObjectId objectId = new ObjectId(id);
        Query query = new Query(Criteria.where("_id").is(objectId));

        Movie movie = mongoTemplate.findAndRemove(query, Movie.class);

        if (movie == null) {
            throw new ResourceNotFoundException("Movie not found");
        }

        return movie;
    }
    
    /**
     * Builds a Spring Data MongoDB Query from the search parameters.
     */
    private Query buildQuery(MovieSearchQuery query) {
        Query mongoQuery = new Query();

        // Text search
        if (query.getQ() != null && !query.getQ().trim().isEmpty()) {
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(query.getQ());
            mongoQuery.addCriteria(textCriteria);
        }

        // Genre filter (case-insensitive regex)
        if (query.getGenre() != null && !query.getGenre().trim().isEmpty()) {
            mongoQuery.addCriteria(Criteria.where(Movie.Fields.GENRES)
                    .regex(Pattern.compile(query.getGenre(), Pattern.CASE_INSENSITIVE)));
        }

        // Year filter
        if (query.getYear() != null) {
            mongoQuery.addCriteria(Criteria.where(Movie.Fields.YEAR).is(query.getYear()));
        }

        // Rating range filter
        if (query.getMinRating() != null || query.getMaxRating() != null) {
            Criteria ratingCriteria = Criteria.where(Movie.Fields.IMDB_RATING);
            if (query.getMinRating() != null) {
                ratingCriteria = ratingCriteria.gte(query.getMinRating());
            }
            if (query.getMaxRating() != null) {
                ratingCriteria = ratingCriteria.lte(query.getMaxRating());
            }
            mongoQuery.addCriteria(ratingCriteria);
        }

        return mongoQuery;
    }

    /**
     * Builds a Spring Data Sort object from sort parameters.
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : Movie.Fields.TITLE;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
    
    /**
     * Checks if the update request has any non-null fields.
     */
    private boolean isUpdateRequestEmpty(UpdateMovieRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> requestMap = objectMapper.convertValue(request, Map.class);
        return requestMap.values().stream().allMatch(java.util.Objects::isNull);
    }

    /**
     * Builds a Spring Data MongoDB Update object from the update request.
     */
    private Update buildUpdate(UpdateMovieRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> requestMap = objectMapper.convertValue(request, Map.class);

        Update update = new Update();
        requestMap.forEach((key, value) -> {
            if (value != null) {
                update.set(key, value);
            }
        });

        return update;
    }

    // TODO: Add advanced query methods
    // - getMoviesByGenreStatistics() - Aggregation pipeline for genre statistics
    // - getTopRatedMovies(int limit) - Movies sorted by rating
    // - getMoviesByDecade(int decade) - Movies from a specific decade
    // - getDirectorFilmography(String director) - All movies by a director
    // - getActorFilmography(String actor) - All movies featuring an actor
    // - searchSimilarMovies(String movieId) - Vector search on plot_embedding field
    // - getMovieRecommendations(String userId) - Personalized recommendations
}

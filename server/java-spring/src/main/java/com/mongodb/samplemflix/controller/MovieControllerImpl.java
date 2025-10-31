package com.mongodb.samplemflix.controller;

import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.BatchInsertResponse;
import com.mongodb.samplemflix.model.dto.BatchUpdateResponse;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.DeleteResponse;
import com.mongodb.samplemflix.model.dto.DirectorStatisticsResult;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.MovieWithCommentsResult;
import com.mongodb.samplemflix.model.dto.MoviesByYearResult;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.model.response.SuccessResponse;
import com.mongodb.samplemflix.service.MovieService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for movie-related endpoints.
 *
 * <p>This controller handles all HTTP requests for movie operations including:
 * <pre>
 * - GET /api/movies - Get all movies with filtering, sorting, and pagination
 * - GET /api/movies/{id} - Get a single movie by ID
 * - POST /api/movies - Create a new movie
 * - POST /api/movies/batch - Create multiple movies
 * - PUT /api/movies/{id} - Update a movie
 * - PATCH /api/movies - Update multiple movies
 * - DELETE /api/movies/{id} - Delete a movie
 * - DELETE /api/movies - Delete multiple movies
 * - DELETE /api/movies/{id}/find-and-delete - Find and delete a movie
 * - GET /api/movies/aggregations/comments - Aggregate movies with most comments
 * - GET /api/movies/aggregations/years - Aggregate movies by year with statistics
 * - GET /api/movies/aggregations/directors - Aggregate directors with most movies
 * - GET /api/movies/searchByPlot - Text search using Atlas Search Index based on plot
 * - GET /api/movies/findSimilarMovies - Vector search to find similar movies based on plot embeddings
 * </pre>
 */
@RestController
@RequestMapping("/api/movies")
public class MovieControllerImpl {
    
    private final MovieService movieService;
    
    public MovieControllerImpl(MovieService movieService) {
        this.movieService = movieService;
    }
    
    /**
     * GET /api/movies
     *
     * <p>Retrieves multiple movies with optional filtering, sorting, and pagination.
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<List<Movie>>> getAllMovies(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer skip,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        MovieSearchQuery query = MovieSearchQuery.builder()
                .q(q)
                .genre(genre)
                .year(year)
                .minRating(minRating)
                .maxRating(maxRating)
                .limit(limit)
                .skip(skip)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();
        
        List<Movie> movies = movieService.getAllMovies(query);
        
        SuccessResponse<List<Movie>> response = SuccessResponse.<List<Movie>>builder()
                .success(true)
                .message("Found " + movies.size() + " movies")
                .data(movies)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/movies/{id}
     *
     * <p>Retrieves a single movie by its ObjectId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<Movie>> getMovieById(@PathVariable String id) {
        Movie movie = movieService.getMovieById(id);
        
        SuccessResponse<Movie> response = SuccessResponse.<Movie>builder()
                .success(true)
                .message("Movie retrieved successfully")
                .data(movie)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/movies
     *
     * <p>Creates a single new movie document.
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<Movie>> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        Movie movie = movieService.createMovie(request);
        
        SuccessResponse<Movie> response = SuccessResponse.<Movie>builder()
                .success(true)
                .message("Movie '" + request.getTitle() + "' created successfully")
                .data(movie)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/movies/batch
     *
     * <p>Creates multiple movie documents in a single operation.
     */
    @PostMapping("/batch")
    public ResponseEntity<SuccessResponse<BatchInsertResponse>> createMoviesBatch(
            @RequestBody List<CreateMovieRequest> requests) {
        BatchInsertResponse result = movieService.createMoviesBatch(requests);

        SuccessResponse<BatchInsertResponse> response = SuccessResponse.<BatchInsertResponse>builder()
                .success(true)
                .message("Successfully created " + result.getInsertedCount() + " movies")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/movies/{id}
     *
     * <p>Updates a single movie document.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<Movie>> updateMovie(
            @PathVariable String id,
            @RequestBody UpdateMovieRequest request) {
        Movie movie = movieService.updateMovie(id, request);
        
        SuccessResponse<Movie> response = SuccessResponse.<Movie>builder()
                .success(true)
                .message("Movie updated successfully")
                .data(movie)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * PATCH /api/movies
     *
     * <p>Updates multiple movies based on a filter.
     */
    @SuppressWarnings("unchecked")
    @PatchMapping
    public ResponseEntity<SuccessResponse<BatchUpdateResponse>> updateMoviesBatch(
            @RequestBody Map<String, Object> body) {
        Document filter = new Document((Map<String, Object>) body.get("filter"));
        Document update = new Document((Map<String, Object>) body.get("update"));

        BatchUpdateResponse result = movieService.updateMoviesBatch(filter, update);

        SuccessResponse<BatchUpdateResponse> response = SuccessResponse.<BatchUpdateResponse>builder()
                .success(true)
                .message("Update operation completed. Matched " + result.getMatchedCount() +
                        " documents, modified " + result.getModifiedCount() + " documents.")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/movies/{id}/find-and-delete
     *
     * <p>Finds and deletes a movie in a single atomic operation.
     */
    @DeleteMapping("/{id}/find-and-delete")
    public ResponseEntity<SuccessResponse<Movie>> findAndDeleteMovie(@PathVariable String id) {
        Movie movie = movieService.findAndDeleteMovie(id);
        
        SuccessResponse<Movie> response = SuccessResponse.<Movie>builder()
                .success(true)
                .message("Movie found and deleted successfully")
                .data(movie)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/movies/{id}
     *
     * <p>Deletes a single movie document.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<DeleteResponse>> deleteMovie(@PathVariable String id) {
        DeleteResponse result = movieService.deleteMovie(id);

        SuccessResponse<DeleteResponse> response = SuccessResponse.<DeleteResponse>builder()
                .success(true)
                .message("Movie deleted successfully")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/movies
     *
     * <p>Deletes multiple movies based on a filter.
     */
    @SuppressWarnings("unchecked")
    @DeleteMapping
    public ResponseEntity<SuccessResponse<DeleteResponse>> deleteMoviesBatch(
            @RequestBody Map<String, Object> body) {
        Document filter = new Document((Map<String, Object>) body.get("filter"));

        DeleteResponse result = movieService.deleteMoviesBatch(filter);

        SuccessResponse<DeleteResponse> response = SuccessResponse.<DeleteResponse>builder()
                .success(true)
                .message("Delete operation completed. Removed " + result.getDeletedCount() + " documents.")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }

    // Aggregation endpoints for reporting

    /**
     * GET /api/movies/aggregations/comments
     *
     * <p>Aggregates movies with their most recent comments.
     * Demonstrates MongoDB $lookup (join) operation to combine movies with comments.
     *
     * @param limit Maximum number of movies to return (default: 10, max: 50)
     * @param movieId Optional movie ID to filter by specific movie
     * @return List of movies with their recent comments
     */
    @GetMapping("/aggregations/comments")
    public ResponseEntity<SuccessResponse<List<MovieWithCommentsResult>>> getMoviesWithMostComments(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String movieId) {

        List<MovieWithCommentsResult> results = movieService.getMoviesWithMostComments(limit, movieId);

        // Calculate total comments across all movies
        int totalComments = results.stream()
                .mapToInt(result -> result.getTotalComments() != null ? result.getTotalComments() : 0)
                .sum();

        String message = movieId != null
                ? String.format("Found %d comments from movie", totalComments)
                : String.format("Found %d comments from %d movie%s",
                        totalComments, results.size(), results.size() != 1 ? "s" : "");

        SuccessResponse<List<MovieWithCommentsResult>> response =
                SuccessResponse.<List<MovieWithCommentsResult>>builder()
                        .success(true)
                        .message(message)
                        .data(results)
                        .timestamp(Instant.now().toString())
                        .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/movies/aggregations/years
     *
     * <p>Aggregates movies by year with statistics.
     * Demonstrates MongoDB $group operation for statistical aggregation.
     *
     * @return List of yearly statistics including movie count and average rating
     */
    @GetMapping("/aggregations/years")
    public ResponseEntity<SuccessResponse<List<MoviesByYearResult>>> getMoviesByYearWithStats() {

        List<MoviesByYearResult> results = movieService.getMoviesByYearWithStats();

        SuccessResponse<List<MoviesByYearResult>> response =
                SuccessResponse.<List<MoviesByYearResult>>builder()
                        .success(true)
                        .message(String.format("Aggregated statistics for %d years", results.size()))
                        .data(results)
                        .timestamp(Instant.now().toString())
                        .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/movies/aggregations/directors
     *
     * <p>Aggregates directors with the most movies.
     * Demonstrates MongoDB $unwind operation for array flattening and aggregation.
     *
     * @param limit Maximum number of directors to return (default: 20, max: 100)
     * @return List of directors with their movie count and average rating
     */
    @GetMapping("/aggregations/directors")
    public ResponseEntity<SuccessResponse<List<DirectorStatisticsResult>>> getDirectorsWithMostMovies(
            @RequestParam(defaultValue = "20") Integer limit) {

        List<DirectorStatisticsResult> results = movieService.getDirectorsWithMostMovies(limit);

        SuccessResponse<List<DirectorStatisticsResult>> response =
                SuccessResponse.<List<DirectorStatisticsResult>>builder()
                        .success(true)
                        .message(String.format("Found %d directors with most movies", results.size()))
                        .data(results)
                        .timestamp(Instant.now().toString())
                        .build();

        return ResponseEntity.ok(response);
    }

    // Atlas Search endpoints

    /**
     * GET /api/movies/searchByPlot
     *
     * <p>Searches movies by plot using MongoDB Atlas Search.
     * Demonstrates text search using Atlas Search Index based on plot field.
     *
     * @param plot Text to search in the plot field (required)
     * @param limit Maximum number of movies to return (default: 20, max: 100)
     * @param skip Number of results to skip for pagination (default: 0)
     * @return List of movies matching the search criteria
     */
    @GetMapping("/searchByPlot")
    public ResponseEntity<SuccessResponse<List<Movie>>> searchMoviesByPlot(
            @RequestParam String plot,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer skip) {

        List<Movie> movies = movieService.searchMoviesByPlot(plot, limit, skip);

        SuccessResponse<List<Movie>> response = SuccessResponse.<List<Movie>>builder()
                .success(true)
                .message(String.format("Found %d movies matching the search criteria", movies.size()))
                .data(movies)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/movies/findSimilarMovies
     *
     * <p>Finds similar movies using vector search on plot embeddings.
     * Demonstrates MongoDB Atlas Vector Search to find movies with similar plots.
     *
     * @param movieId ID of the movie to find similar movies for (required)
     * @param limit Maximum number of similar movies to return (default: 10, max: 50)
     * @return List of similar movies based on plot embeddings
     */
    @GetMapping("/findSimilarMovies")
    public ResponseEntity<SuccessResponse<List<Movie>>> findSimilarMovies(
            @RequestParam String movieId,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<Movie> movies = movieService.findSimilarMovies(movieId, limit);

        SuccessResponse<List<Movie>> response = SuccessResponse.<List<Movie>>builder()
                .success(true)
                .message(String.format("Found %d similar movies", movies.size()))
                .data(movies)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}

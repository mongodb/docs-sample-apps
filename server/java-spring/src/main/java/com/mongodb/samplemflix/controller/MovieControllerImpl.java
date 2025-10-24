package com.mongodb.samplemflix.controller;

import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.model.response.SuccessResponse;
import com.mongodb.samplemflix.service.MovieService;
import jakarta.validation.Valid;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST controller for movie-related endpoints.
 * <p>
 * This controller handles all HTTP requests for movie operations including:
 * - GET /api/movies - Get all movies with filtering, sorting, and pagination
 * - GET /api/movies/{id} - Get a single movie by ID
 * - POST /api/movies - Create a new movie
 * - POST /api/movies/batch - Create multiple movies
 * - PUT /api/movies/{id} - Update a movie
 * - PATCH /api/movies - Update multiple movies
 * - DELETE /api/movies/{id} - Delete a movie
 * - DELETE /api/movies - Delete multiple movies
 * - DELETE /api/movies/{id}/find-and-delete - Find and delete a movie
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
     * <p>
     * Retrieves multiple movies with optional filtering, sorting, and pagination.
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
     * <p>
     * Retrieves a single movie by its ObjectId.
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
     * <p>
     * Creates a single new movie document.
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
     * <p>
     * Creates multiple movie documents in a single operation.
     */
    @PostMapping("/batch")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> createMoviesBatch(
            @RequestBody List<CreateMovieRequest> requests) {
        Map<String, Object> result = movieService.createMoviesBatch(requests);
        
        SuccessResponse<Map<String, Object>> response = SuccessResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Successfully created " + result.get("insertedCount") + " movies")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/movies/{id}
     * <p>
     * Updates a single movie document.
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
     * <p>
     * Updates multiple movies based on a filter.
     */
    @SuppressWarnings("unchecked")
    @PatchMapping
    public ResponseEntity<SuccessResponse<Map<String, Object>>> updateMoviesBatch(
            @RequestBody Map<String, Object> body) {
        Document filter = new Document((Map<String, Object>) body.get("filter"));
        Document update = new Document((Map<String, Object>) body.get("update"));

        Map<String, Object> result = movieService.updateMoviesBatch(filter, update);

        SuccessResponse<Map<String, Object>> response = SuccessResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Update operation completed. Matched " + result.get("matchedCount") +
                        " documents, modified " + result.get("modifiedCount") + " documents.")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/movies/{id}/find-and-delete
     * <p>
     * Finds and deletes a movie in a single atomic operation.
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
     * <p>
     * Deletes a single movie document.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> deleteMovie(@PathVariable String id) {
        Map<String, Object> result = movieService.deleteMovie(id);
        
        SuccessResponse<Map<String, Object>> response = SuccessResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Movie deleted successfully")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/movies
     * <p>
     * Deletes multiple movies based on a filter.
     */
    @SuppressWarnings("unchecked")
    @DeleteMapping
    public ResponseEntity<SuccessResponse<Map<String, Object>>> deleteMoviesBatch(
            @RequestBody Map<String, Object> body) {
        Document filter = new Document((Map<String, Object>) body.get("filter"));

        Map<String, Object> result = movieService.deleteMoviesBatch(filter);

        SuccessResponse<Map<String, Object>> response = SuccessResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Delete operation completed. Removed " + result.get("deletedCount") + " documents.")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}

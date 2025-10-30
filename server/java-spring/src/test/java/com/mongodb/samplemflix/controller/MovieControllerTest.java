package com.mongodb.samplemflix.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.samplemflix.exception.ResourceNotFoundException;
import com.mongodb.samplemflix.exception.ValidationException;
import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.BatchInsertResponse;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.DeleteResponse;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.service.MovieService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for MovieControllerImpl.
 *
 * These tests verify the REST API endpoints by mocking the service layer.
 * Uses Spring's MockMvc for testing HTTP requests and responses.
 */
@WebMvcTest(MovieControllerImpl.class)
@DisplayName("MovieController Unit Tests")
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovieService movieService;

    private ObjectId testId;
    private Movie testMovie;
    private CreateMovieRequest createRequest;
    private UpdateMovieRequest updateRequest;

    @BeforeEach
    void setUp() {
        testId = new ObjectId();

        testMovie = Movie.builder()
                .id(testId)
                .title("Test Movie")
                .year(2024)
                .plot("A test plot")
                .genres(Arrays.asList("Action", "Drama"))
                .build();

        createRequest = CreateMovieRequest.builder()
                .title("New Movie")
                .year(2024)
                .plot("A new movie plot")
                .build();

        updateRequest = UpdateMovieRequest.builder()
                .title("Updated Title")
                .year(2025)
                .build();
    }

    // ==================== GET ALL MOVIES TESTS ====================

    @Test
    @DisplayName("GET /api/movies - Should return list of movies")
    void testGetAllMovies_Success() throws Exception {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie);
        when(movieService.getAllMovies(any(MovieSearchQuery.class))).thenReturn(movies);

        // Act & Assert
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Test Movie"))
                .andExpect(jsonPath("$.data[0].year").value(2024));
    }

    @Test
    @DisplayName("GET /api/movies - Should handle query parameters")
    void testGetAllMovies_WithQueryParams() throws Exception {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie);
        when(movieService.getAllMovies(any(MovieSearchQuery.class))).thenReturn(movies);

        // Act & Assert
        mockMvc.perform(get("/api/movies")
                        .param("q", "test")
                        .param("genre", "Action")
                        .param("year", "2024")
                        .param("limit", "10")
                        .param("skip", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET MOVIE BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/movies/{id} - Should return movie by ID")
    void testGetMovieById_Success() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.getMovieById(movieId)).thenReturn(testMovie);

        // Act & Assert
        mockMvc.perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Movie"))
                .andExpect(jsonPath("$.data.year").value(2024));
    }

    @Test
    @DisplayName("GET /api/movies/{id} - Should return 404 when movie not found")
    void testGetMovieById_NotFound() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.getMovieById(movieId))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // Act & Assert
        mockMvc.perform(get("/api/movies/{id}", movieId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Movie not found"));
    }

    @Test
    @DisplayName("GET /api/movies/{id} - Should return 400 for invalid ID")
    void testGetMovieById_InvalidId() throws Exception {
        // Arrange
        String invalidId = "invalid-id";
        when(movieService.getMovieById(invalidId))
                .thenThrow(new ValidationException("Invalid movie ID format"));

        // Act & Assert
        mockMvc.perform(get("/api/movies/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    // ==================== CREATE MOVIE TESTS ====================

    @Test
    @DisplayName("POST /api/movies - Should create movie successfully")
    void testCreateMovie_Success() throws Exception {
        // Arrange
        when(movieService.createMovie(any(CreateMovieRequest.class))).thenReturn(testMovie);

        // Act & Assert
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Movie"));
    }

    @Test
    @DisplayName("POST /api/movies - Should return 400 for validation error")
    void testCreateMovie_ValidationError() throws Exception {
        // Arrange
        when(movieService.createMovie(any(CreateMovieRequest.class)))
                .thenThrow(new ValidationException("Title is required"));

        // Act & Assert
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/movies/batch - Should create movies batch successfully")
    void testCreateMoviesBatch_Success() throws Exception {
        // Arrange
        List<CreateMovieRequest> requests = Arrays.asList(createRequest, createRequest);
        Map<Integer, org.bson.BsonValue> insertedIds = new HashMap<>();
        insertedIds.put(0, new BsonObjectId(new ObjectId()));
        insertedIds.put(1, new BsonObjectId(new ObjectId()));
        BatchInsertResponse response = new BatchInsertResponse(2, insertedIds.values());

        when(movieService.createMoviesBatch(anyList())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/movies/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.insertedCount").value(2));
    }

    // ==================== UPDATE MOVIE TESTS ====================

    @Test
    @DisplayName("PUT /api/movies/{id} - Should update movie successfully")
    void testUpdateMovie_Success() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        Movie updatedMovie = Movie.builder()
                .id(testId)
                .title("Updated Title")
                .year(2025)
                .build();

        when(movieService.updateMovie(eq(movieId), any(UpdateMovieRequest.class)))
                .thenReturn(updatedMovie);

        // Act & Assert
        mockMvc.perform(put("/api/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.year").value(2025));
    }

    @Test
    @DisplayName("PUT /api/movies/{id} - Should return 404 when movie not found")
    void testUpdateMovie_NotFound() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.updateMovie(eq(movieId), any(UpdateMovieRequest.class)))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // Act & Assert
        mockMvc.perform(put("/api/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    // ==================== DELETE MOVIE TESTS ====================

    @Test
    @DisplayName("DELETE /api/movies/{id} - Should delete movie successfully")
    void testDeleteMovie_Success() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        DeleteResponse response = new DeleteResponse(1L);

        when(movieService.deleteMovie(movieId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/movies/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deletedCount").value(1));
    }

    @Test
    @DisplayName("DELETE /api/movies/{id} - Should return 404 when movie not found")
    void testDeleteMovie_NotFound() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.deleteMovie(movieId))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/movies/{id}", movieId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/movies/{id}/find-and-delete - Should find and delete movie successfully")
    void testFindAndDeleteMovie_Success() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.findAndDeleteMovie(movieId)).thenReturn(testMovie);

        // Act & Assert
        mockMvc.perform(delete("/api/movies/{id}/find-and-delete", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Movie"));
    }

    @Test
    @DisplayName("DELETE /api/movies/{id}/find-and-delete - Should return 404 when movie not found")
    void testFindAndDeleteMovie_NotFound() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.findAndDeleteMovie(movieId))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // Act & Assert
        mockMvc.perform(delete("/api/movies/{id}/find-and-delete", movieId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
}

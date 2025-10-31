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
import com.mongodb.samplemflix.model.dto.DirectorStatisticsResult;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.MovieWithCommentsResult;
import com.mongodb.samplemflix.model.dto.MoviesByYearResult;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.service.MovieService;
import java.util.Arrays;
import java.util.Date;
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

    // ==================== AGGREGATION ENDPOINT TESTS ====================

    @Test
    @DisplayName("GET /api/movies/aggregations/comments - Should return movies with most comments")
    void testGetMoviesWithMostComments_Success() throws Exception {
        // Arrange
        MovieWithCommentsResult.CommentInfo comment = MovieWithCommentsResult.CommentInfo.builder()
                .id(new ObjectId().toHexString())
                .name("John Doe")
                .email("john@example.com")
                .text("Great movie!")
                .date(new Date())
                .build();

        MovieWithCommentsResult.ImdbInfo imdb = MovieWithCommentsResult.ImdbInfo.builder()
                .rating(8.5)
                .votes(1000)
                .build();

        MovieWithCommentsResult result = MovieWithCommentsResult.builder()
                .id(testId.toHexString())
                .title("Test Movie")
                .year(2024)
                .plot("Test plot")
                .poster("http://example.com/poster.jpg")
                .genres(Arrays.asList("Action", "Drama"))
                .imdb(imdb)
                .recentComments(Arrays.asList(comment))
                .totalComments(5)
                .mostRecentCommentDate(new Date())
                .build();

        when(movieService.getMoviesWithMostComments(anyInt(), isNull())).thenReturn(Arrays.asList(result));

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Test Movie"))
                .andExpect(jsonPath("$.data[0].year").value(2024))
                .andExpect(jsonPath("$.data[0].totalComments").value(5))
                .andExpect(jsonPath("$.data[0].recentComments", hasSize(1)))
                .andExpect(jsonPath("$.data[0].recentComments[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/movies/aggregations/comments - Should accept limit parameter")
    void testGetMoviesWithMostComments_WithLimit() throws Exception {
        // Arrange
        when(movieService.getMoviesWithMostComments(eq(5), isNull())).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/comments")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/movies/aggregations/comments - Should accept movieId parameter")
    void testGetMoviesWithMostComments_WithMovieId() throws Exception {
        // Arrange
        String movieId = testId.toHexString();
        when(movieService.getMoviesWithMostComments(anyInt(), eq(movieId))).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/comments")
                        .param("movieId", movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/movies/aggregations/comments - Should return 400 for invalid movieId")
    void testGetMoviesWithMostComments_InvalidMovieId() throws Exception {
        // Arrange
        String invalidMovieId = "invalid-id";
        when(movieService.getMoviesWithMostComments(anyInt(), eq(invalidMovieId)))
                .thenThrow(new ValidationException("Invalid movie ID format"));

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/comments")
                        .param("movieId", invalidMovieId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/movies/aggregations/years - Should return movies by year with statistics")
    void testGetMoviesByYearWithStats_Success() throws Exception {
        // Arrange
        MoviesByYearResult result1 = MoviesByYearResult.builder()
                .year(2024)
                .movieCount(10)
                .averageRating(7.5)
                .highestRating(9.0)
                .lowestRating(6.0)
                .totalVotes(5000L)
                .build();

        MoviesByYearResult result2 = MoviesByYearResult.builder()
                .year(2023)
                .movieCount(15)
                .averageRating(7.8)
                .highestRating(9.5)
                .lowestRating(6.5)
                .totalVotes(7500L)
                .build();

        when(movieService.getMoviesByYearWithStats()).thenReturn(Arrays.asList(result1, result2));

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].year").value(2024))
                .andExpect(jsonPath("$.data[0].movieCount").value(10))
                .andExpect(jsonPath("$.data[0].averageRating").value(7.5))
                .andExpect(jsonPath("$.data[1].year").value(2023))
                .andExpect(jsonPath("$.data[1].movieCount").value(15));
    }

    @Test
    @DisplayName("GET /api/movies/aggregations/directors - Should return directors with most movies")
    void testGetDirectorsWithMostMovies_Success() throws Exception {
        // Arrange
        DirectorStatisticsResult result1 = DirectorStatisticsResult.builder()
                .director("Christopher Nolan")
                .movieCount(10)
                .averageRating(8.5)
                .build();

        DirectorStatisticsResult result2 = DirectorStatisticsResult.builder()
                .director("Steven Spielberg")
                .movieCount(25)
                .averageRating(8.2)
                .build();

        when(movieService.getDirectorsWithMostMovies(anyInt())).thenReturn(Arrays.asList(result1, result2));

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].director").value("Christopher Nolan"))
                .andExpect(jsonPath("$.data[0].movieCount").value(10))
                .andExpect(jsonPath("$.data[0].averageRating").value(8.5))
                .andExpect(jsonPath("$.data[1].director").value("Steven Spielberg"))
                .andExpect(jsonPath("$.data[1].movieCount").value(25));
    }

    @Test
    @DisplayName("GET /api/movies/aggregations/directors - Should accept limit parameter")
    void testGetDirectorsWithMostMovies_WithLimit() throws Exception {
        // Arrange
        when(movieService.getDirectorsWithMostMovies(eq(10))).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/movies/aggregations/directors")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== ATLAS SEARCH ENDPOINT TESTS ====================

    @Test
    @DisplayName("GET /api/movies/searchByPlot - Should search movies by plot successfully")
    void testSearchMoviesByPlot_Success() throws Exception {
        // Arrange
        Movie movie1 = Movie.builder()
                .id(new ObjectId())
                .title("Space Adventure")
                .year(2024)
                .plot("An epic space adventure across the galaxy")
                .genres(Arrays.asList("Sci-Fi", "Adventure"))
                .build();

        Movie movie2 = Movie.builder()
                .id(new ObjectId())
                .title("Space Quest")
                .year(2023)
                .plot("A thrilling space adventure to save humanity")
                .genres(Arrays.asList("Sci-Fi", "Action"))
                .build();

        when(movieService.searchMoviesByPlot(eq("space adventure"), eq(20), eq(0)))
                .thenReturn(Arrays.asList(movie1, movie2));

        // Act & Assert
        mockMvc.perform(get("/api/movies/searchByPlot")
                        .param("plot", "space adventure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("Space Adventure"))
                .andExpect(jsonPath("$.data[0].plot").value(containsString("space adventure")))
                .andExpect(jsonPath("$.data[1].title").value("Space Quest"));
    }

    @Test
    @DisplayName("GET /api/movies/searchByPlot - Should accept limit and skip parameters")
    void testSearchMoviesByPlot_WithPagination() throws Exception {
        // Arrange
        when(movieService.searchMoviesByPlot(eq("adventure"), eq(10), eq(5)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/movies/searchByPlot")
                        .param("plot", "adventure")
                        .param("limit", "10")
                        .param("skip", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/movies/searchByPlot - Should return 400 when plot parameter is missing")
    void testSearchMoviesByPlot_MissingPlotParameter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/movies/searchByPlot"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/movies/searchByPlot - Should return 400 for validation error")
    void testSearchMoviesByPlot_ValidationError() throws Exception {
        // Arrange
        when(movieService.searchMoviesByPlot(anyString(), anyInt(), anyInt()))
                .thenThrow(new ValidationException("Plot query cannot be empty"));

        // Act & Assert
        mockMvc.perform(get("/api/movies/searchByPlot")
                        .param("plot", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/movies/searchByPlot - Should return empty list when no matches found")
    void testSearchMoviesByPlot_NoResults() throws Exception {
        // Arrange
        when(movieService.searchMoviesByPlot(eq("nonexistent"), eq(20), eq(0)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/movies/searchByPlot")
                        .param("plot", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}

package com.mongodb.samplemflix.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.samplemflix.exception.ResourceNotFoundException;
import com.mongodb.samplemflix.exception.ValidationException;
import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.BatchInsertResponse;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.DeleteResponse;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.repository.MovieRepository;
import java.util.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Unit tests for MovieServiceImpl using Spring Data MongoDB.
 *
 * These tests verify the business logic of the service layer
 * by mocking the repository and MongoTemplate dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Unit Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MovieServiceImpl movieService;

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
    @DisplayName("Should get all movies with default pagination")
    void testGetAllMovies_WithDefaults() {
        // Arrange
        MovieSearchQuery query = MovieSearchQuery.builder().build();
        List<Movie> expectedMovies = Arrays.asList(testMovie);

        when(mongoTemplate.find(any(Query.class), eq(Movie.class)))
                .thenReturn(expectedMovies);

        // Act
        List<Movie> result = movieService.getAllMovies(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMovie.getTitle(), result.get(0).getTitle());
        verify(mongoTemplate).find(any(Query.class), eq(Movie.class));
    }

    @Test
    @DisplayName("Should get all movies with custom pagination")
    void testGetAllMovies_WithCustomPagination() {
        // Arrange
        MovieSearchQuery query = MovieSearchQuery.builder()
                .limit(50)
                .skip(10)
                .build();
        List<Movie> expectedMovies = Arrays.asList(testMovie);

        when(mongoTemplate.find(any(Query.class), eq(Movie.class)))
                .thenReturn(expectedMovies);

        // Act
        List<Movie> result = movieService.getAllMovies(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate).find(any(Query.class), eq(Movie.class));
    }

    @Test
    @DisplayName("Should enforce maximum limit of 100")
    void testGetAllMovies_EnforcesMaxLimit() {
        // Arrange
        MovieSearchQuery query = MovieSearchQuery.builder()
                .limit(200)
                .build();

        when(mongoTemplate.find(any(Query.class), eq(Movie.class)))
                .thenReturn(Collections.emptyList());

        // Act
        movieService.getAllMovies(query);

        // Assert
        verify(mongoTemplate).find(any(Query.class), eq(Movie.class));
    }

    @Test
    @DisplayName("Should enforce minimum limit of 1")
    void testGetAllMovies_EnforcesMinLimit() {
        // Arrange
        MovieSearchQuery query = MovieSearchQuery.builder()
                .limit(0)
                .build();

        when(mongoTemplate.find(any(Query.class), eq(Movie.class)))
                .thenReturn(Collections.emptyList());

        // Act
        movieService.getAllMovies(query);

        // Assert
        verify(mongoTemplate).find(any(Query.class), eq(Movie.class));
    }

    // ==================== GET MOVIE BY ID TESTS ====================

    @Test
    @DisplayName("Should get movie by valid ID")
    void testGetMovieById_ValidId() {
        // Arrange
        String validId = testId.toHexString();
        when(movieRepository.findById(testId)).thenReturn(Optional.of(testMovie));

        // Act
        Movie result = movieService.getMovieById(validId);

        // Assert
        assertNotNull(result);
        assertEquals(testMovie.getTitle(), result.getTitle());
        verify(movieRepository).findById(testId);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID format")
    void testGetMovieById_InvalidIdFormat() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.getMovieById(invalidId));
        verify(movieRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie not found")
    void testGetMovieById_NotFound() {
        // Arrange
        String validId = testId.toHexString();
        when(movieRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(validId));
        verify(movieRepository).findById(testId);
    }

    // ==================== CREATE MOVIE TESTS ====================

    @Test
    @DisplayName("Should create movie successfully")
    void testCreateMovie_Success() {
        // Arrange
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // Act
        Movie result = movieService.createMovie(createRequest);

        // Assert
        assertNotNull(result);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when title is null")
    void testCreateMovie_NullTitle() {
        // Arrange
        CreateMovieRequest invalidRequest = CreateMovieRequest.builder()
                .title(null)
                .year(2024)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.createMovie(invalidRequest));
        verify(movieRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when title is empty")
    void testCreateMovie_EmptyTitle() {
        // Arrange
        CreateMovieRequest invalidRequest = CreateMovieRequest.builder()
                .title("   ")
                .year(2024)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.createMovie(invalidRequest));
        verify(movieRepository, never()).save(any());
    }

    // ==================== CREATE MOVIES BATCH TESTS ====================

    @Test
    @DisplayName("Should create movies batch successfully")
    void testCreateMoviesBatch_Success() {
        // Arrange
        List<CreateMovieRequest> requests = Arrays.asList(createRequest, createRequest);
        List<Movie> savedMovies = Arrays.asList(testMovie, testMovie);

        when(movieRepository.saveAll(anyList())).thenReturn(savedMovies);

        // Act
        BatchInsertResponse result = movieService.createMoviesBatch(requests);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getInsertedCount());
        assertNotNull(result.getInsertedIds());
        verify(movieRepository).saveAll(anyList());
    }

    // ==================== UPDATE MOVIE TESTS ====================

    @Test
    @DisplayName("Should update movie successfully")
    void testUpdateMovie_Success() {
        // Arrange
        String validId = testId.toHexString();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("title", "Updated Title");
        requestMap.put("year", 2025);

        when(objectMapper.convertValue(updateRequest, Map.class)).thenReturn(requestMap);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.Update.class), any(Class.class)))
                .thenReturn(updateResult);
        when(movieRepository.findById(testId)).thenReturn(Optional.of(testMovie));

        // Act
        Movie result = movieService.updateMovie(validId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(mongoTemplate).updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.Update.class), any(Class.class));
        verify(movieRepository).findById(testId);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID in update")
    void testUpdateMovie_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.updateMovie(invalidId, updateRequest));
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.Update.class), any(Class.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when update request is empty")
    void testUpdateMovie_EmptyRequest() {
        // Arrange
        String validId = testId.toHexString();
        UpdateMovieRequest emptyRequest = UpdateMovieRequest.builder().build();
        Map<String, Object> emptyMap = new HashMap<>();

        when(objectMapper.convertValue(emptyRequest, Map.class)).thenReturn(emptyMap);

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.updateMovie(validId, emptyRequest));
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.Update.class), any(Class.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie to update not found")
    void testUpdateMovie_NotFound() {
        // Arrange
        String validId = testId.toHexString();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("title", "Updated Title");

        when(objectMapper.convertValue(updateRequest, Map.class)).thenReturn(requestMap);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        when(mongoTemplate.updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.Update.class), any(Class.class)))
                .thenReturn(updateResult);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.updateMovie(validId, updateRequest));
        verify(mongoTemplate).updateFirst(any(Query.class), any(org.springframework.data.mongodb.core.query.Update.class), any(Class.class));
        verify(movieRepository, never()).findById(any());
    }

    // ==================== DELETE MOVIE TESTS ====================

    @Test
    @DisplayName("Should delete movie successfully")
    void testDeleteMovie_Success() {
        // Arrange
        String validId = testId.toHexString();
        when(movieRepository.existsById(testId)).thenReturn(true);

        // Act
        DeleteResponse result = movieService.deleteMovie(validId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getDeletedCount());
        verify(movieRepository).existsById(testId);
        verify(movieRepository).deleteById(testId);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID in delete")
    void testDeleteMovie_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.deleteMovie(invalidId));
        verify(movieRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie to delete not found")
    void testDeleteMovie_NotFound() {
        // Arrange
        String validId = testId.toHexString();
        when(movieRepository.existsById(testId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.deleteMovie(validId));
        verify(movieRepository).existsById(testId);
        verify(movieRepository, never()).deleteById(any());
    }

    // ==================== FIND AND DELETE MOVIE TESTS ====================

    @Test
    @DisplayName("Should find and delete movie successfully")
    void testFindAndDeleteMovie_Success() {
        // Arrange
        String validId = testId.toHexString();
        when(mongoTemplate.findAndRemove(any(Query.class), eq(Movie.class))).thenReturn(testMovie);

        // Act
        Movie result = movieService.findAndDeleteMovie(validId);

        // Assert
        assertNotNull(result);
        assertEquals(testMovie.getTitle(), result.getTitle());
        verify(mongoTemplate).findAndRemove(any(Query.class), eq(Movie.class));
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID in find and delete")
    void testFindAndDeleteMovie_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.findAndDeleteMovie(invalidId));
        verify(mongoTemplate, never()).findAndRemove(any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie to find and delete not found")
    void testFindAndDeleteMovie_NotFound() {
        // Arrange
        String validId = testId.toHexString();
        when(mongoTemplate.findAndRemove(any(Query.class), eq(Movie.class))).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.findAndDeleteMovie(validId));
        verify(mongoTemplate).findAndRemove(any(Query.class), eq(Movie.class));
    }
}

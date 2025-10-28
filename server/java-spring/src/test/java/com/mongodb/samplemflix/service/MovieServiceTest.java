package com.mongodb.samplemflix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.samplemflix.exception.DatabaseOperationException;
import com.mongodb.samplemflix.exception.ResourceNotFoundException;
import com.mongodb.samplemflix.exception.ValidationException;
import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.BatchInsertResponse;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.DeleteResponse;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.repository.MovieRepository;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MovieServiceImpl.
 *
 * These tests verify the business logic of the service layer
 * by mocking the repository layer dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Unit Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

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

        when(movieRepository.find(any(Document.class), any(Document.class), eq(0), eq(20)))
                .thenReturn(expectedMovies);

        // Act
        List<Movie> result = movieService.getAllMovies(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMovie.getTitle(), result.get(0).getTitle());
        verify(movieRepository).find(any(Document.class), any(Document.class), eq(0), eq(20));
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

        when(movieRepository.find(any(Document.class), any(Document.class), eq(10), eq(50)))
                .thenReturn(expectedMovies);

        // Act
        List<Movie> result = movieService.getAllMovies(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movieRepository).find(any(Document.class), any(Document.class), eq(10), eq(50));
    }

    @Test
    @DisplayName("Should enforce maximum limit of 100")
    void testGetAllMovies_EnforcesMaxLimit() {
        // Arrange
        MovieSearchQuery query = MovieSearchQuery.builder()
                .limit(200)
                .build();

        when(movieRepository.find(any(Document.class), any(Document.class), eq(0), eq(100)))
                .thenReturn(Collections.emptyList());

        // Act
        movieService.getAllMovies(query);

        // Assert
        verify(movieRepository).find(any(Document.class), any(Document.class), eq(0), eq(100));
    }

    @Test
    @DisplayName("Should enforce minimum limit of 1")
    void testGetAllMovies_EnforcesMinLimit() {
        // Arrange
        MovieSearchQuery query = MovieSearchQuery.builder()
                .limit(0)
                .build();

        when(movieRepository.find(any(Document.class), any(Document.class), eq(0), eq(1)))
                .thenReturn(Collections.emptyList());

        // Act
        movieService.getAllMovies(query);

        // Assert
        verify(movieRepository).find(any(Document.class), any(Document.class), eq(0), eq(1));
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
        InsertOneResult insertResult = mock(InsertOneResult.class);
        when(insertResult.wasAcknowledged()).thenReturn(true);
        when(insertResult.getInsertedId()).thenReturn(new BsonObjectId(testId));
        when(movieRepository.insertOne(any(Movie.class))).thenReturn(insertResult);
        when(movieRepository.findById(testId)).thenReturn(Optional.of(testMovie));

        // Act
        Movie result = movieService.createMovie(createRequest);

        // Assert
        assertNotNull(result);
        verify(movieRepository).insertOne(any(Movie.class));
        verify(movieRepository).findById(testId);
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
        verify(movieRepository, never()).insertOne(any());
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
        verify(movieRepository, never()).insertOne(any());
    }

    @Test
    @DisplayName("Should throw DatabaseOperationException when insert not acknowledged")
    void testCreateMovie_NotAcknowledged() {
        // Arrange
        InsertOneResult insertResult = mock(InsertOneResult.class);
        when(insertResult.wasAcknowledged()).thenReturn(false);
        when(movieRepository.insertOne(any(Movie.class))).thenReturn(insertResult);

        // Act & Assert
        assertThrows(DatabaseOperationException.class, () -> movieService.createMovie(createRequest));
        verify(movieRepository).insertOne(any(Movie.class));
        verify(movieRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw DatabaseOperationException when created movie not found")
    void testCreateMovie_CreatedMovieNotFound() {
        // Arrange
        InsertOneResult insertResult = mock(InsertOneResult.class);
        when(insertResult.wasAcknowledged()).thenReturn(true);
        when(insertResult.getInsertedId()).thenReturn(new BsonObjectId(testId));
        when(movieRepository.insertOne(any(Movie.class))).thenReturn(insertResult);
        when(movieRepository.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DatabaseOperationException.class, () -> movieService.createMovie(createRequest));
        verify(movieRepository).insertOne(any(Movie.class));
        verify(movieRepository).findById(testId);
    }

    // ==================== CREATE MOVIES BATCH TESTS ====================

    @Test
    @DisplayName("Should create movies batch successfully")
    void testCreateMoviesBatch_Success() {
        // Arrange
        List<CreateMovieRequest> requests = Arrays.asList(createRequest, createRequest);
        InsertManyResult insertResult = mock(InsertManyResult.class);
        Map<Integer, org.bson.BsonValue> insertedIds = new HashMap<>();
        insertedIds.put(0, new BsonObjectId(new ObjectId()));
        insertedIds.put(1, new BsonObjectId(new ObjectId()));

        when(insertResult.wasAcknowledged()).thenReturn(true);
        when(insertResult.getInsertedIds()).thenReturn(insertedIds);
        when(movieRepository.insertMany(anyList())).thenReturn(insertResult);

        // Act
        BatchInsertResponse result = movieService.createMoviesBatch(requests);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getInsertedCount());
        assertNotNull(result.getInsertedIds());
        verify(movieRepository).insertMany(anyList());
    }

    @Test
    @DisplayName("Should throw DatabaseOperationException when batch insert not acknowledged")
    void testCreateMoviesBatch_NotAcknowledged() {
        // Arrange
        List<CreateMovieRequest> requests = Arrays.asList(createRequest);
        InsertManyResult insertResult = mock(InsertManyResult.class);
        when(insertResult.wasAcknowledged()).thenReturn(false);
        when(movieRepository.insertMany(anyList())).thenReturn(insertResult);

        // Act & Assert
        assertThrows(DatabaseOperationException.class, () -> movieService.createMoviesBatch(requests));
        verify(movieRepository).insertMany(anyList());
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
        when(movieRepository.updateOne(eq(testId), any(Document.class))).thenReturn(updateResult);
        when(movieRepository.findById(testId)).thenReturn(Optional.of(testMovie));

        // Act
        Movie result = movieService.updateMovie(validId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(movieRepository).updateOne(eq(testId), any(Document.class));
        verify(movieRepository).findById(testId);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID in update")
    void testUpdateMovie_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.updateMovie(invalidId, updateRequest));
        verify(movieRepository, never()).updateOne(any(), any());
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
        verify(movieRepository, never()).updateOne(any(), any());
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
        when(movieRepository.updateOne(eq(testId), any(Document.class))).thenReturn(updateResult);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.updateMovie(validId, updateRequest));
        verify(movieRepository).updateOne(eq(testId), any(Document.class));
        verify(movieRepository, never()).findById(any());
    }

    // ==================== DELETE MOVIE TESTS ====================

    @Test
    @DisplayName("Should delete movie successfully")
    void testDeleteMovie_Success() {
        // Arrange
        String validId = testId.toHexString();
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(movieRepository.deleteOne(testId)).thenReturn(deleteResult);

        // Act
        DeleteResponse result = movieService.deleteMovie(validId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getDeletedCount());
        verify(movieRepository).deleteOne(testId);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID in delete")
    void testDeleteMovie_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.deleteMovie(invalidId));
        verify(movieRepository, never()).deleteOne(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie to delete not found")
    void testDeleteMovie_NotFound() {
        // Arrange
        String validId = testId.toHexString();
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(movieRepository.deleteOne(testId)).thenReturn(deleteResult);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.deleteMovie(validId));
        verify(movieRepository).deleteOne(testId);
    }

    // ==================== FIND AND DELETE MOVIE TESTS ====================

    @Test
    @DisplayName("Should find and delete movie successfully")
    void testFindAndDeleteMovie_Success() {
        // Arrange
        String validId = testId.toHexString();
        when(movieRepository.findOneAndDelete(testId)).thenReturn(Optional.of(testMovie));

        // Act
        Movie result = movieService.findAndDeleteMovie(validId);

        // Assert
        assertNotNull(result);
        assertEquals(testMovie.getTitle(), result.getTitle());
        verify(movieRepository).findOneAndDelete(testId);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid ID in find and delete")
    void testFindAndDeleteMovie_InvalidId() {
        // Arrange
        String invalidId = "invalid-id";

        // Act & Assert
        assertThrows(ValidationException.class, () -> movieService.findAndDeleteMovie(invalidId));
        verify(movieRepository, never()).findOneAndDelete(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie to find and delete not found")
    void testFindAndDeleteMovie_NotFound() {
        // Arrange
        String validId = testId.toHexString();
        when(movieRepository.findOneAndDelete(testId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> movieService.findAndDeleteMovie(validId));
        verify(movieRepository).findOneAndDelete(testId);
    }
}

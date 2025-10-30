package com.mongodb.samplemflix.service;

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
import java.util.List;
import org.bson.Document;

/**
 * Service interface for movie business logic.
 */
public interface MovieService {

    List<Movie> getAllMovies(MovieSearchQuery query);

    Movie getMovieById(String id);

    Movie createMovie(CreateMovieRequest request);

    BatchInsertResponse createMoviesBatch(List<CreateMovieRequest> requests);

    Movie updateMovie(String id, UpdateMovieRequest request);

    BatchUpdateResponse updateMoviesBatch(Document filter, Document update);

    DeleteResponse deleteMovie(String id);

    DeleteResponse deleteMoviesBatch(Document filter);

    Movie findAndDeleteMovie(String id);

    // Aggregation endpoints for reporting

    /**
     * Aggregates movies with their most recent comments.
     *
     * @param limit Maximum number of movies to return
     * @param movieId Optional movie ID to filter by specific movie
     * @return List of movies with their recent comments
     */
    List<MovieWithCommentsResult> getMoviesWithMostComments(Integer limit, String movieId);

    /**
     * Aggregates movies by year with statistics.
     *
     * @return List of yearly statistics including movie count and average rating
     */
    List<MoviesByYearResult> getMoviesByYearWithStats();

    /**
     * Aggregates directors with the most movies.
     *
     * @param limit Maximum number of directors to return
     * @return List of directors with their movie count and average rating
     */
    List<DirectorStatisticsResult> getDirectorsWithMostMovies(Integer limit);
}

package com.mongodb.samplemflix.service;

import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * Service interface for movie business logic.
 */
public interface MovieService {

    List<Movie> getAllMovies(MovieSearchQuery query);

    Movie getMovieById(String id);

    Movie createMovie(CreateMovieRequest request);

    Map<String, Object> createMoviesBatch(List<CreateMovieRequest> requests);

    Movie updateMovie(String id, UpdateMovieRequest request);

    Map<String, Object> updateMoviesBatch(Document filter, Document update);

    Map<String, Object> deleteMovie(String id);

    Map<String, Object> deleteMoviesBatch(Document filter);

    Movie findAndDeleteMovie(String id);
}

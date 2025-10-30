package com.mongodb.samplemflix.service;

import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.BatchInsertResponse;
import com.mongodb.samplemflix.model.dto.BatchUpdateResponse;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.DeleteResponse;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
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
}

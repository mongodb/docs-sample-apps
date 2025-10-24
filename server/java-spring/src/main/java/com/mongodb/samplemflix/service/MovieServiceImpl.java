package com.mongodb.samplemflix.service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.samplemflix.exception.ResourceNotFoundException;
import com.mongodb.samplemflix.exception.ValidationException;
import com.mongodb.samplemflix.model.Movie;
import com.mongodb.samplemflix.model.dto.CreateMovieRequest;
import com.mongodb.samplemflix.model.dto.MovieSearchQuery;
import com.mongodb.samplemflix.model.dto.UpdateMovieRequest;
import com.mongodb.samplemflix.repository.MovieRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service layer for movie business logic.
 * 
 * This service handles:
 * - Business logic and validation
 * - Query construction (filters, sorts, pagination)
 * - Data transformation between DTOs and entities
 * - Error handling and exception throwing
 */
@Service
public class MovieServiceImpl implements MovieService {
    
    private final MovieRepository movieRepository;
    
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }
    
    @Override
    public List<Movie> getAllMovies(MovieSearchQuery query) {
        Document filter = buildFilter(query);
        Document sort = buildSort(query.getSortBy(), query.getSortOrder());
        
        int limit = Math.min(Math.max(query.getLimit() != null ? query.getLimit() : 20, 1), 100);
        int skip = Math.max(query.getSkip() != null ? query.getSkip() : 0, 0);
        
        return movieRepository.find(filter, sort, skip, limit);
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
        
        InsertOneResult result = movieRepository.insertOne(movie);
        
        if (!result.wasAcknowledged()) {
            throw new RuntimeException("Movie insertion was not acknowledged by the database");
        }
        
        return movieRepository.findById(result.getInsertedId().asObjectId().getValue())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created movie"));
    }
    
    @Override
    public Map<String, Object> createMoviesBatch(List<CreateMovieRequest> requests) {
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
        
        InsertManyResult result = movieRepository.insertMany(movies);
        
        if (!result.wasAcknowledged()) {
            throw new RuntimeException("Batch movie insertion was not acknowledged by the database");
        }
        
        return Map.of(
                "insertedCount", result.getInsertedIds().size(),
                "insertedIds", result.getInsertedIds().values()
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
        
        Document update = new Document("$set", buildUpdateDocument(request));
        UpdateResult result = movieRepository.updateOne(new ObjectId(id), update);
        
        if (result.getMatchedCount() == 0) {
            throw new ResourceNotFoundException("Movie not found");
        }
        
        return movieRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Failed to retrieve updated movie"));
    }
    
    @Override
    public Map<String, Object> updateMoviesBatch(Document filter, Document update) {
        if (filter == null || update == null) {
            throw new ValidationException("Both filter and update objects are required");
        }
        
        if (update.isEmpty()) {
            throw new ValidationException("Update object cannot be empty");
        }
        
        Document setUpdate = new Document("$set", update);
        UpdateResult result = movieRepository.updateMany(filter, setUpdate);
        
        return Map.of(
                "matchedCount", result.getMatchedCount(),
                "modifiedCount", result.getModifiedCount()
        );
    }
    
    @Override
    public Map<String, Object> deleteMovie(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ValidationException("Invalid movie ID format");
        }
        
        DeleteResult result = movieRepository.deleteOne(new ObjectId(id));
        
        if (result.getDeletedCount() == 0) {
            throw new ResourceNotFoundException("Movie not found");
        }
        
        return Map.of("deletedCount", result.getDeletedCount());
    }
    
    @Override
    public Map<String, Object> deleteMoviesBatch(Document filter) {
        if (filter == null || filter.isEmpty()) {
            throw new ValidationException("Filter object is required and cannot be empty. This prevents accidental deletion of all documents.");
        }
        
        DeleteResult result = movieRepository.deleteMany(filter);
        
        return Map.of("deletedCount", result.getDeletedCount());
    }
    
    @Override
    public Movie findAndDeleteMovie(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ValidationException("Invalid movie ID format");
        }
        
        return movieRepository.findOneAndDelete(new ObjectId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
    }
    
    private Document buildFilter(MovieSearchQuery query) {
        Document filter = new Document();
        
        if (query.getQ() != null && !query.getQ().trim().isEmpty()) {
            filter.append("$text", new Document("$search", query.getQ()));
        }
        
        if (query.getGenre() != null && !query.getGenre().trim().isEmpty()) {
            filter.append("genres", new Document("$regex", Pattern.compile(query.getGenre(), Pattern.CASE_INSENSITIVE)));
        }
        
        if (query.getYear() != null) {
            filter.append("year", query.getYear());
        }
        
        if (query.getMinRating() != null || query.getMaxRating() != null) {
            Document ratingFilter = new Document();
            if (query.getMinRating() != null) {
                ratingFilter.append("$gte", query.getMinRating());
            }
            if (query.getMaxRating() != null) {
                ratingFilter.append("$lte", query.getMaxRating());
            }
            filter.append("imdb.rating", ratingFilter);
        }
        
        return filter;
    }
    
    private Document buildSort(String sortBy, String sortOrder) {
        String field = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : "title";
        int order = "desc".equalsIgnoreCase(sortOrder) ? -1 : 1;
        return new Document(field, order);
    }
    
    private boolean isUpdateRequestEmpty(UpdateMovieRequest request) {
        return request.getTitle() == null &&
               request.getYear() == null &&
               request.getPlot() == null &&
               request.getFullplot() == null &&
               request.getGenres() == null &&
               request.getDirectors() == null &&
               request.getWriters() == null &&
               request.getCast() == null &&
               request.getCountries() == null &&
               request.getLanguages() == null &&
               request.getRated() == null &&
               request.getRuntime() == null &&
               request.getPoster() == null;
    }
    
    private Document buildUpdateDocument(UpdateMovieRequest request) {
        Document doc = new Document();
        
        if (request.getTitle() != null) {
            doc.append("title", request.getTitle());
        }
        if (request.getYear() != null) {
            doc.append("year", request.getYear());
        }
        if (request.getPlot() != null) {
            doc.append("plot", request.getPlot());
        }
        if (request.getFullplot() != null) {
            doc.append("fullplot", request.getFullplot());
        }
        if (request.getGenres() != null) {
            doc.append("genres", request.getGenres());
        }
        if (request.getDirectors() != null) {
            doc.append("directors", request.getDirectors());
        }
        if (request.getWriters() != null) {
            doc.append("writers", request.getWriters());
        }
        if (request.getCast() != null) {
            doc.append("cast", request.getCast());
        }
        if (request.getCountries() != null) {
            doc.append("countries", request.getCountries());
        }
        if (request.getLanguages() != null) {
            doc.append("languages", request.getLanguages());
        }
        if (request.getRated() != null) {
            doc.append("rated", request.getRated());
        }
        if (request.getRuntime() != null) {
            doc.append("runtime", request.getRuntime());
        }
        if (request.getPoster() != null) {
            doc.append("poster", request.getPoster());
        }
        
        return doc;
    }
}


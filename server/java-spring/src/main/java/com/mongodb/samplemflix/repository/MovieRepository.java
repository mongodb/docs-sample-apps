package com.mongodb.samplemflix.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.samplemflix.model.Movie;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for movie data access.
 *
 * This repository provides methods for all CRUD operations using the MongoDB Java Driver directly.
 * The implementation uses MongoCollection<Document> for direct control over BSON documents.
 */
public interface MovieRepository {

    /**
     * Inserts a single movie document.
     *
     * @param movie the movie to insert
     * @return the result of the insert operation
     */
    InsertOneResult insertOne(Movie movie);

    /**
     * Inserts multiple movie documents.
     *
     * @param movies the list of movies to insert
     * @return the result of the insert operation
     */
    InsertManyResult insertMany(List<Movie> movies);

    /**
     * Finds a single movie by its ID.
     *
     * @param id the movie ID
     * @return Optional containing the movie if found, empty otherwise
     */
    Optional<Movie> findById(ObjectId id);

    /**
     * Finds multiple movies with filtering, sorting, and pagination.
     *
     * @param filter the filter document
     * @param sort the sort document
     * @param skip number of documents to skip
     * @param limit maximum number of documents to return
     * @return list of movies matching the criteria
     */
    List<Movie> find(Document filter, Document sort, int skip, int limit);

    /**
     * Updates a single movie by ID.
     *
     * @param id the movie ID
     * @param update the update document
     * @return the result of the update operation
     */
    UpdateResult updateOne(ObjectId id, Document update);

    /**
     * Updates multiple movies matching the filter.
     *
     * @param filter the filter document
     * @param update the update document
     * @return the result of the update operation
     */
    UpdateResult updateMany(Document filter, Document update);

    /**
     * Deletes a single movie by ID.
     *
     * @param id the movie ID
     * @return the result of the delete operation
     */
    DeleteResult deleteOne(ObjectId id);

    /**
     * Deletes multiple movies matching the filter.
     *
     * @param filter the filter document
     * @return the result of the delete operation
     */
    DeleteResult deleteMany(Document filter);

    /**
     * Finds and deletes a single movie in one atomic operation.
     *
     * @param id the movie ID
     * @return Optional containing the deleted movie if found, empty otherwise
     */
    Optional<Movie> findOneAndDelete(ObjectId id);

    /**
     * Counts the total number of documents in the movies collection.
     *
     * @return the count of documents
     */
    long countDocuments();
}

package com.mongodb.samplemflix.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Database verification component that runs on application startup.
 *
 * This component performs pre-flight checks to ensure the MongoDB database
 * is properly configured and contains the expected data and indexes.
 *
 * Verification steps:
 * 1. Check if the movies collection exists
 * 2. Verify the collection contains documents
 * 3. Check for text search indexes on plot, title, and fullplot fields
 * 4. Create text search index if missing
 *
 * This matches the behavior of the Express.js backend's verifyRequirements() function.
 * The verification is non-blocking - the application will start even if verification fails,
 * but warnings will be logged to help developers identify configuration issues.
 */
@Component
public class DatabaseVerification {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerification.class);

    private static final String MOVIES_COLLECTION = "movies";
    private static final String TEXT_INDEX_NAME = "text_search_index";

    private final MongoDatabase database;

    public DatabaseVerification(MongoDatabase database) {
        this.database = database;
    }

    /**
     * Runs database verification checks after the bean is constructed.
     *
     * This method is called automatically by Spring after dependency injection
     * is complete. It performs all verification steps and logs the results.
     *
     * The method catches all exceptions to prevent application startup failure,
     * but logs errors to help developers identify issues.
     */
    @PostConstruct
    public void verifyDatabase() {
        logger.info("Starting database verification for '{}'...", database.getName());

        try {
            // Verify movies collection exists and has data
            verifyMoviesCollection();

            logger.info("Database verification completed successfully");

        } catch (Exception e) {
            logger.error("Database verification failed: {}", e.getMessage(), e);
            // Don't throw exception - allow application to start even if verification fails
            // This allows developers to troubleshoot connection issues without preventing startup
        }
    }

    /**
     * Verifies the movies collection exists, contains data, and has required indexes.
     *
     * This method:
     * 1. Checks if the movies collection exists (implicitly by accessing it)
     * 2. Counts documents to verify sample data is loaded
     * 3. Creates a text search index on plot, title, and fullplot fields
     *
     * The text search index enables full-text search functionality across movie
     * descriptions and titles, which is used by the search endpoint.
     */
    private void verifyMoviesCollection() {
        MongoCollection<Document> moviesCollection = database.getCollection(MOVIES_COLLECTION);

        // Check if collection has documents
        // Using estimatedDocumentCount() for better performance (doesn't scan all documents)
        long count = moviesCollection.estimatedDocumentCount();

        logger.info("Movies collection found with {} documents", count);

        if (count == 0) {
            logger.warn(
                "Movies collection is empty. Please ensure sample_mflix data is loaded. " +
                "Visit https://www.mongodb.com/docs/atlas/sample-data/ for instructions."
            );
        }

        // Create text search index for full-text search functionality
        createTextSearchIndex(moviesCollection);
    }

    /**
     * Creates a text search index on the movies collection if it doesn't already exist.
     *
     * The index is created on three fields:
     * - plot: Short movie description
     * - title: Movie title
     * - fullplot: Full movie description
     *
     * This enables the $text search operator to perform full-text search across
     * these fields, which is used by the search endpoint in the API.
     *
     * The index is created in the background to avoid blocking other operations.
     * If the index already exists, MongoDB will ignore the duplicate creation request.
     *
     * @param moviesCollection the movies collection to create the index on
     */
    private void createTextSearchIndex(MongoCollection<Document> moviesCollection) {
        try {
            // Create compound text index on plot, title, and fullplot fields
            // The background option allows the index to be built without blocking other operations
            IndexOptions indexOptions = new IndexOptions()
                    .name(TEXT_INDEX_NAME)
                    .background(true);

            // Create the text index
            // MongoDB will automatically ignore this if the index already exists
            moviesCollection.createIndex(
                Indexes.compoundIndex(
                    Indexes.text("plot"),
                    Indexes.text("title"),
                    Indexes.text("fullplot")
                ),
                indexOptions
            );

            logger.info("Text search index '{}' created/verified for movies collection", TEXT_INDEX_NAME);

        } catch (Exception e) {
            // Log error but don't fail - the application can still function without the index
            // (though text search queries will fail)
            logger.error("Could not create text search index: {}", e.getMessage());
            logger.warn("Text search functionality may not work without the index");
        }
    }
}

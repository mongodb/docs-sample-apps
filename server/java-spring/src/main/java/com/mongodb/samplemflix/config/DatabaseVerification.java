package com.mongodb.samplemflix.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.samplemflix.model.Movie;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Database verification component that runs on application startup.
 *
 * <p>This component performs pre-flight checks to ensure the MongoDB database
 * is properly configured and contains the expected data and indexes.
 *
 * <p>Verification steps:
 * 1. Check if the movies collection exists
 * 2. Verify the collection contains documents
 * 3. Check for text search indexes on plot, title, and fullplot fields
 * 4. Create text search index if missing
 * 5. Verify embedded_movies collection for vector search
 * 6. Create vector search index if missing
 * <p>
 * This matches the behavior of the Express.js backend's verifyRequirements() function.
 * The verification is non-blocking - the application will start even if verification fails,
 * but warnings will be logged to help developers identify configuration issues.
 */
@Component
public class DatabaseVerification {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerification.class);

    private static final String MOVIES_COLLECTION = "movies";
    private static final String COMMENTS_COLLECTION = "comments";
    private static final String EMBEDDED_MOVIES_COLLECTION = "embedded_movies";
    private static final String TEXT_INDEX_NAME = "text_search_index";
    private static final String YEAR_INDEX_NAME = "year_index";
    private static final String MOVIE_ID_INDEX_NAME = "movie_id_index";
    private static final String VECTOR_INDEX_NAME = "vector_index";

    private final MongoDatabase database;

    public DatabaseVerification(MongoDatabase database) {
        this.database = database;
    }

    /**
     * Runs database verification checks after the bean is constructed.
     *
     * <p>This method is called automatically by Spring after dependency injection
     * is complete. It performs all verification steps and logs the results.
     *
     * <p>The method catches all exceptions to prevent application startup failure,
     * but logs errors to help developers identify issues.
     */
    @PostConstruct
    public void verifyDatabase() {
        logger.info("Starting database verification for '{}'...", database.getName());

        try {
            // Verify movies collection exists and has data
            verifyMoviesCollection();

            // Verify comments collection and create indexes for aggregation performance
            verifyCommentsCollection();

            // Verify embedded_movies collection and create vector search index
            verifyEmbeddedMoviesCollection();

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
     * <p>This method:
     * <pre>
     * 1. Checks if the movies collection exists (implicitly by accessing it)
     * 2. Counts documents to verify sample data is loaded
     * 3. Creates a text search index on plot, title, and fullplot fields
     *</pre>
     * <p>The text search index enables full-text search functionality across movie
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

        // Create year index for aggregation performance
        createYearIndex(moviesCollection);
    }

    /**
     * Creates a text search index on the movies collection if it doesn't already exist.
     *
     * <p>The index is created on three fields:
     * <pre>
     * - plot: Short movie description
     * - title: Movie title
     * - fullplot: Full movie description
     * </pre>
     * <p>This enables the $text search operator to perform full-text search across
     * these fields, which is used by the search endpoint in the API.
     *
     * <p>The index is created in the background to avoid blocking other operations.
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

            // Create the text index using field name constants from Movie.Fields
            // This makes the coupling between Movie class and index creation explicit
            // and allows IDE "Find Usages" to track dependencies
            // MongoDB will automatically ignore this if the index already exists
            moviesCollection.createIndex(
                Indexes.compoundIndex(
                    Indexes.text(Movie.Fields.PLOT),
                    Indexes.text(Movie.Fields.TITLE),
                    Indexes.text(Movie.Fields.FULLPLOT)
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

    /**
     * Creates an index on the year field for the movies collection.
     *
     * <p>This index improves performance for aggregation queries that filter by year,
     * such as the movies with comments aggregation.
     *
     * @param moviesCollection the movies collection to create the index on
     */
    private void createYearIndex(MongoCollection<Document> moviesCollection) {
        try {
            IndexOptions indexOptions = new IndexOptions()
                    .name(YEAR_INDEX_NAME)
                    .background(true);

            moviesCollection.createIndex(
                Indexes.ascending(Movie.Fields.YEAR),
                indexOptions
            );

            logger.info("Year index '{}' created/verified for movies collection", YEAR_INDEX_NAME);

        } catch (Exception e) {
            logger.error("Could not create year index: {}", e.getMessage());
            logger.warn("Aggregation queries filtering by year may be slower without the index");
        }
    }

    /**
     * Verifies the comments collection and creates necessary indexes.
     *
     * <p>This method creates an index on the movie_id field to improve $lookup performance
     * when joining movies with comments in aggregation pipelines.
     */
    private void verifyCommentsCollection() {
        MongoCollection<Document> commentsCollection = database.getCollection(COMMENTS_COLLECTION);

        // Check if collection has documents
        long count = commentsCollection.estimatedDocumentCount();

        logger.info("Comments collection found with {} documents", count);

        if (count == 0) {
            logger.warn(
                "Comments collection is empty. Please ensure sample_mflix data is loaded."
            );
        }

        // Create movie_id index for $lookup performance
        createMovieIdIndex(commentsCollection);
    }

    /**
     * Creates an index on the movie_id field for the comments collection.
     *
     * <p>This index is critical for $lookup performance when joining movies with comments.
     * Without this index, the $lookup operation will perform a collection scan for each movie,
     * which can cause timeouts on large datasets.
     *
     * @param commentsCollection the comments collection to create the index on
     */
    private void createMovieIdIndex(MongoCollection<Document> commentsCollection) {
        try {
            IndexOptions indexOptions = new IndexOptions()
                    .name(MOVIE_ID_INDEX_NAME)
                    .background(true);

            commentsCollection.createIndex(
                Indexes.ascending("movie_id"),
                indexOptions
            );

            logger.info("Movie ID index '{}' created/verified for comments collection", MOVIE_ID_INDEX_NAME);

        } catch (Exception e) {
            logger.error("Could not create movie_id index: {}", e.getMessage());
            logger.warn("$lookup aggregations joining movies with comments may timeout without the index");
        }
    }

    /**
     * Verifies the embedded_movies collection and creates the vector search index.
     *
     * <p>The embedded_movies collection contains movie documents with plot embeddings
     * generated by the Voyage AI model. This method checks if the collection exists
     * and creates a vector search index for semantic similarity search.
     *
     * <p>Note: Vector search indexes can only be created through the Atlas UI or API,
     * not through the MongoDB driver. This method logs instructions for manual creation.
     */
    private void verifyEmbeddedMoviesCollection() {
        MongoCollection<Document> embeddedMoviesCollection = database.getCollection(EMBEDDED_MOVIES_COLLECTION);

        // Check if collection has documents
        long count = embeddedMoviesCollection.estimatedDocumentCount();

        if (count == 0) {
            logger.warn(
                "Embedded movies collection is empty. Vector search functionality will not work. " +
                "Please ensure the embedded_movies collection is populated with plot embeddings."
            );
            return;
        }

        logger.info("Embedded movies collection found with {} documents", count);

        // Check if documents have the required embedding field
        Document sampleDoc = embeddedMoviesCollection.find().first();
        if (sampleDoc != null && !sampleDoc.containsKey("plot_embedding_voyage_3_large")) {
            logger.warn(
                "Documents in embedded_movies collection do not have 'plot_embedding_voyage_3_large' field. " +
                "Vector search functionality will not work."
            );
            return;
        }

        // Create vector search index programmatically
        createVectorSearchIndex(embeddedMoviesCollection);
    }

    /**
     * Creates a vector search index on the embedded_movies collection if it doesn't already exist.
     *
     * <p>This method creates a vector search index named 'vector_index' for the
     * plot_embedding_voyage_3_large field with 2048 dimensions and cosine similarity.
     *
     * @param embeddedMoviesCollection the embedded_movies collection to create the index on
     */
    private void createVectorSearchIndex(MongoCollection<Document> embeddedMoviesCollection) {
        try {
            // Check if the vector search index already exists
            boolean indexExists = false;
            for (Document index : embeddedMoviesCollection.listSearchIndexes()) {
                if (VECTOR_INDEX_NAME.equals(index.getString("name"))) {
                    indexExists = true;
                    logger.info("Vector search index '{}' already exists", VECTOR_INDEX_NAME);
                    break;
                }
            }

            if (!indexExists) {
                // Define the vector search index specification
                Document vectorField = new Document()
                        .append("type", "vector")
                        .append("path", "plot_embedding_voyage_3_large")
                        .append("numDimensions", 2048)
                        .append("similarity", "cosine");

                Document indexDefinition = new Document()
                        .append("fields", java.util.Arrays.asList(vectorField));

                Document searchIndexModel = new Document()
                        .append("name", VECTOR_INDEX_NAME)
                        .append("type", "vectorSearch")
                        .append("definition", indexDefinition);

                // Create the index using the createSearchIndex command
                String indexName = embeddedMoviesCollection.createSearchIndex(searchIndexModel);

                logger.info("Vector search index '{}' created successfully. Index may take a few moments to build.", indexName);
                logger.info("Vector search is now ready to use on the '{}' collection", EMBEDDED_MOVIES_COLLECTION);
            }

        } catch (Exception e) {
            logger.error("Failed to create vector search index: {}", e.getMessage());
            logger.warn(
                "To manually create the vector search index, visit the Atlas UI and create an index named '{}' with:\n" +
                "  - Field: plot_embedding_voyage_3_large\n" +
                "  - Dimensions: 2048\n" +
                "  - Similarity: cosine\n" +
                "Visit: https://www.mongodb.com/docs/atlas/atlas-vector-search/create-index/",
                VECTOR_INDEX_NAME
            );
        }
    }
}

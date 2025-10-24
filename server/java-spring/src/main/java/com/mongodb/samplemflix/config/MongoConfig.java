package com.mongodb.samplemflix.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB configuration class for the Sample MFlix application.
 * <p>
 * This class configures the MongoDB client connection using the MongoDB Java Driver.
 * It creates singleton beans for MongoClient and MongoDatabase that will be injected
 * throughout the application.
 * <p>
 * Key features:
 * - Connection pooling with configurable settings (max 100 connections, min 10)
 * - Connection timeout configuration (10 seconds for connect and read)
 * - Graceful shutdown handling (managed by Spring's bean lifecycle)
 * - Connection string validation
 * <p>
 * The MongoClient is thread-safe and designed to be shared across the application.
 * Spring automatically manages the lifecycle and closes the client on shutdown.
 */
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * Creates and configures the MongoDB client with connection pooling and timeout settings.
     * <p>
     * Connection Pool Settings:
     * - Max pool size: 100 connections (handles high concurrent load)
     * - Min pool size: 10 connections (maintains ready connections)
     * - Max connection idle time: 60 seconds (releases idle connections)
     * - Max wait time: 10 seconds (time to wait for available connection)
     * <p>
     * Socket Settings:
     * - Connect timeout: 10 seconds (time to establish connection)
     * - Read timeout: 10 seconds (time to wait for server response)
     * <p>
     * The MongoClient is thread-safe and should be shared across the application.
     * Spring will automatically close this client when the application shuts down.
     *
     * @return configured MongoClient instance
     * @throws IllegalArgumentException if connection string is invalid
     */
    @Bean
    public MongoClient mongoClient() {
        // Validate connection string is not empty
        if (mongoUri == null || mongoUri.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "MONGODB_URI is not configured. Please check application.properties"
            );
        }

        // Parse and validate the connection string
        ConnectionString connectionString = new ConnectionString(mongoUri);

        // Build client settings with connection pooling and timeouts
        // These settings optimize for both performance and resource management
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                // Configure connection pool for optimal performance
                .applyToConnectionPoolSettings(builder ->
                    builder.maxSize(100)                                    // Maximum connections in pool
                           .minSize(10)                                     // Minimum connections to maintain
                           .maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS)  // Release idle connections after 60s
                           .maxWaitTime(10000, TimeUnit.MILLISECONDS)       // Wait up to 10s for available connection
                )
                // Configure socket timeouts to prevent hanging connections
                .applyToSocketSettings(builder ->
                    builder.connectTimeout(10000, TimeUnit.MILLISECONDS)    // 10s to establish connection
                           .readTimeout(10000, TimeUnit.MILLISECONDS)       // 10s to wait for server response
                )
                // Configure server selection timeout
                .applyToClusterSettings(builder ->
                    builder.serverSelectionTimeout(10000, TimeUnit.MILLISECONDS)  // 10s to select server
                )
                .build();

        return MongoClients.create(settings);
    }

    /**
     * Creates a reference to the MongoDB database.
     * <p>
     * This bean provides access to the sample_mflix database and can be injected
     * into repositories and services throughout the application.
     * <p>
     * The database name is configured in application.properties and defaults to "sample_mflix".
     *
     * @param mongoClient the MongoDB client (injected by Spring)
     * @return MongoDatabase instance for the configured database
     */
    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(databaseName);
    }
}

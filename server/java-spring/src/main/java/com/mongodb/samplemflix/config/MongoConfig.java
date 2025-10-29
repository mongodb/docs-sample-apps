package com.mongodb.samplemflix.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration class for the Sample MFlix application using Spring Data MongoDB.
 *
 * <p>This class extends AbstractMongoClientConfiguration to customize MongoDB client settings
 * while leveraging Spring Data MongoDB's auto-configuration for repositories and templates.
 *
 * <p>Key features:
 * <pre>
 * - Connection pooling with configurable settings (max 100 connections, min 10)
 * - Connection timeout configuration (10 seconds for connect and read)
 * - Automatic POJO mapping (no manual codec configuration needed)
 * - Repository scanning and auto-configuration
 * - MongoTemplate bean creation for complex queries
 * </pre>
 * <p>Spring Data MongoDB automatically:
 * <pre>
 * - Creates MongoClient and MongoTemplate beans
 * - Handles POJO to BSON conversion
 * - Manages connection lifecycle
 * - Provides repository implementations
 * </pre>
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.mongodb.samplemflix.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        // Validate connection string is not empty
        if (mongoUri == null || mongoUri.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "MONGODB_URI is not configured. Please check application.properties"
            );
        }

        // Parse and validate the connection string
        ConnectionString connectionString = new ConnectionString(mongoUri);

        // Apply connection string and custom settings
        builder.applyConnectionString(connectionString)
                // Configure connection pool for optimal performance
                .applyToConnectionPoolSettings(poolBuilder ->
                    poolBuilder.maxSize(100)                                    // Maximum connections in pool
                           .minSize(10)                                         // Minimum connections to maintain
                           .maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS) // Release idle connections after 60s
                           .maxWaitTime(10000, TimeUnit.MILLISECONDS)           // Wait up to 10s for available connection
                )
                // Configure socket timeouts to prevent hanging connections
                .applyToSocketSettings(socketBuilder ->
                    socketBuilder.connectTimeout(10000, TimeUnit.MILLISECONDS)  // 10s to establish connection
                           .readTimeout(10000, TimeUnit.MILLISECONDS)           // 10s to wait for server response
                )
                // Configure server selection timeout
                .applyToClusterSettings(clusterBuilder ->
                    clusterBuilder.serverSelectionTimeout(10000, TimeUnit.MILLISECONDS)  // 10s to select server
                );
    }

    /**
     * Provides a MongoDatabase bean for direct MongoDB driver access.
     *
     * <p>This bean is needed for components that require direct access to the MongoDB
     * driver API (like DatabaseVerification), while still using Spring Data MongoDB
     * for repository operations.
     *
     * @return the configured MongoDatabase instance
     */
    @Bean
    public MongoDatabase mongoDatabase() {
        return mongoClient().getDatabase(databaseName);
    }
}

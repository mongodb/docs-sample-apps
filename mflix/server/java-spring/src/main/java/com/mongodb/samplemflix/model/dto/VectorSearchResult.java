package com.mongodb.samplemflix.model.dto;

import lombok.Builder;

/**
 * Data Transfer Object for vector search results.
 *
 * <p>This DTO represents the result of a MongoDB Vector Search query,
 * containing the movie information and similarity score.
 */
@Builder
public record VectorSearchResult (
    
    /**
     * Movie ObjectId as a string.
     */
    String id,
    
    /**
     * Movie title.
     */
    String title,
    
    /**
     * Movie plot summary.
     */
    String plot,
    
    /**
     * Movie poster URL.
     */
    String poster,
    
    /**
     * Movie release year.
     */
    Integer year,
    
    /**
     * Movie genres.
     */
    java.util.List<String> genres,
    
    /**
     * Movie directors.
     */
    java.util.List<String> directors,
    
    /**
     * Movie cast members.
     */
    java.util.List<String> cast,
    
    /**
     * Vector search similarity score (0.0 to 1.0, higher = more similar).
     */
    Double score) {}


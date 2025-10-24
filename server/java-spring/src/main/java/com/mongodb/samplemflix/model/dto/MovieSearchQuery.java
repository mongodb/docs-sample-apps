package com.mongodb.samplemflix.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for movie search query parameters.
 * <p>
 * This DTO is used to parse and validate query parameters for GET /api/movies requests.
 * It supports full-text search, filtering by genre/year/rating, sorting, and pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSearchQuery {
    
    /**
     * Full-text search query.
     * Searches across plot, title, and fullplot fields using MongoDB text index.
     */
    private String q;
    
    /**
     * Filter by genre (case-insensitive partial match).
     */
    private String genre;
    
    /**
     * Filter by exact year.
     */
    private Integer year;
    
    /**
     * Minimum IMDB rating (inclusive).
     */
    private Double minRating;
    
    /**
     * Maximum IMDB rating (inclusive).
     */
    private Double maxRating;
    
    /**
     * Number of results to return (default: 20, max: 100).
     */
    private Integer limit;
    
    /**
     * Number of results to skip for pagination (default: 0).
     */
    private Integer skip;
    
    /**
     * Field to sort by (e.g., "title", "year", "imdb.rating").
     * Default: "title"
     */
    private String sortBy;
    
    /**
     * Sort order: "asc" or "desc".
     * Default: "asc"
     */
    private String sortOrder;
}

package com.mongodb.samplemflix.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for MongoDB Search query parameters.
 *
 * <p>This DTO is used to parse and validate query parameters for GET /api/movies/search requests.
 * It supports searching across multiple fields using MongoDB Search with compound operators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSearchRequest {
    
    /**
     * Search query for the plot field.
     * Uses phrase operator for exact phrase matching.
     */
    private String plot;
    
    /**
     * Search query for the fullplot field.
     * Uses phrase operator for exact phrase matching.
     */
    private String fullplot;
    
    /**
     * Search query for the directors field.
     * Uses text operator with fuzzy matching (maxEdits=1, prefixLength=5).
     */
    private String directors;
    
    /**
     * Search query for the writers field.
     * Uses text operator with fuzzy matching (maxEdits=1, prefixLength=5).
     */
    private String writers;
    
    /**
     * Search query for the cast field.
     * Uses text operator with fuzzy matching (maxEdits=1, prefixLength=5).
     */
    private String cast;
    
    /**
     * Maximum number of results to return.
     * Default: 20, Range: 1-100
     */
    private Integer limit;
    
    /**
     * Number of results to skip for pagination.
     * Default: 0, Minimum: 0
     */
    private Integer skip;
    
    /**
     * Compound search operator to use.
     * Valid values: "must", "should", "mustNot", "filter"
     * Default: "must"
     * 
     * <ul>
     * <li><b>must</b> - All clauses must match (AND logic)</li>
     * <li><b>should</b> - At least one clause should match (OR logic)</li>
     * <li><b>mustNot</b> - Clauses must not match (NOT logic)</li>
     * <li><b>filter</b> - Clauses must match but don't affect scoring</li>
     * </ul>
     */
    private String searchOperator;
    
    /**
     * Checks if at least one search field is provided.
     * 
     * @return true if at least one search field has a value
     */
    public boolean hasSearchFields() {
        return (plot != null && !plot.trim().isEmpty()) ||
               (fullplot != null && !fullplot.trim().isEmpty()) ||
               (directors != null && !directors.trim().isEmpty()) ||
               (writers != null && !writers.trim().isEmpty()) ||
               (cast != null && !cast.trim().isEmpty());
    }
}

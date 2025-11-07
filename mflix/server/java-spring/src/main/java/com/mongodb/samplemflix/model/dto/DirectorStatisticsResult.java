package com.mongodb.samplemflix.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for director statistics aggregation result.
 *
 * <p>This class represents the result of the reportingByDirectors aggregation
 * which finds directors with the most movies and their statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DirectorStatisticsResult {

    /**
     * Director name.
     */
    private String director;

    /**
     * Number of movies directed by this director.
     */
    private Integer movieCount;

    /**
     * Average IMDB rating of this director's movies.
     */
    private Double averageRating;
}


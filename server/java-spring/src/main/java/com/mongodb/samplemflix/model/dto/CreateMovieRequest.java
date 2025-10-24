package com.mongodb.samplemflix.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for creating a new movie.
 *
 * This DTO is used for POST /api/movies requests.
 * It includes validation annotations to ensure required fields are present.
 *
 * The structure matches the TypeScript CreateMovieRequest interface from the Express backend.
 * Only the title field is required; all other fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieRequest {

    /**
     * Movie title (required).
     * Must not be blank.
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Release year (optional).
     */
    private Integer year;

    /**
     * Short plot summary (optional).
     */
    private String plot;

    /**
     * Full plot description (optional).
     */
    private String fullplot;

    /**
     * List of genres (optional).
     */
    private List<String> genres;

    /**
     * List of directors (optional).
     */
    private List<String> directors;

    /**
     * List of writers (optional).
     */
    private List<String> writers;

    /**
     * List of cast members (optional).
     */
    private List<String> cast;

    /**
     * List of countries (optional).
     */
    private List<String> countries;

    /**
     * List of languages (optional).
     */
    private List<String> languages;

    /**
     * Movie rating (optional).
     */
    private String rated;

    /**
     * Runtime in minutes (optional).
     */
    private Integer runtime;

    /**
     * Poster image URL (optional).
     */
    private String poster;
}

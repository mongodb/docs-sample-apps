package com.mongodb.samplemflix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Domain model representing a movie document from the MongoDB movies collection.
 *
 * This class maps to the movies collection in the sample_mflix database.
 * It includes all fields from the movie documents including nested objects
 * for awards, IMDB ratings, and Tomatoes ratings.
 *
 * The structure matches the TypeScript Movie interface from the Express backend
 * to ensure API compatibility.
 *
 * Note: We use Lombok annotations to reduce boilerplate code:
 * - @Data: Generates getters, setters, toString, equals, and hashCode
 * - @Builder: Provides a fluent builder pattern for object construction
 * - @NoArgsConstructor: Generates a no-argument constructor (required by MongoDB driver)
 * - @AllArgsConstructor: Generates a constructor with all fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    /**
     * MongoDB document ID.
     * Maps to the _id field in MongoDB.
     * Can be null for new documents (MongoDB will generate it).
     */
    private ObjectId id;

    /**
     * Movie title (required field).
     */
    private String title;

    /**
     * Release year.
     */
    private Integer year;

    /**
     * Short plot summary.
     */
    private String plot;

    /**
     * Full plot description.
     */
    private String fullplot;

    /**
     * Release date.
     */
    private Date released;

    /**
     * Runtime in minutes.
     */
    private Integer runtime;

    /**
     * Poster image URL.
     */
    private String poster;

    /**
     * List of genres (e.g., "Action", "Drama", "Comedy").
     */
    private List<String> genres;

    /**
     * List of directors.
     */
    private List<String> directors;

    /**
     * List of writers.
     */
    private List<String> writers;

    /**
     * List of cast members.
     */
    private List<String> cast;

    /**
     * List of countries where the movie was produced.
     */
    private List<String> countries;

    /**
     * List of languages in the movie.
     */
    private List<String> languages;

    /**
     * Movie rating (e.g., "PG", "PG-13", "R").
     */
    private String rated;

    /**
     * Awards information (wins, nominations, text).
     */
    private Awards awards;

    /**
     * IMDB rating information.
     */
    private Imdb imdb;

    /**
     * Rotten Tomatoes rating information.
     */
    private Tomatoes tomatoes;

    /**
     * Metacritic score.
     */
    private Integer metacritic;

    /**
     * Type of content (e.g., "movie", "series").
     */
    private String type;

    /**
     * Nested class representing awards information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Awards {
        /**
         * Number of awards won.
         */
        private Integer wins;

        /**
         * Number of nominations.
         */
        private Integer nominations;

        /**
         * Text description of awards.
         */
        private String text;
    }

    /**
     * Nested class representing IMDB rating information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Imdb {
        /**
         * IMDB rating (0.0 to 10.0).
         */
        private Double rating;

        /**
         * Number of votes.
         */
        private Integer votes;

        /**
         * IMDB ID number.
         */
        private Integer id;
    }

    /**
     * Nested class representing Rotten Tomatoes rating information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tomatoes {
        /**
         * Viewer ratings information.
         */
        private Viewer viewer;

        /**
         * Critic ratings information.
         */
        private Critic critic;

        /**
         * Number of fresh reviews.
         */
        private Integer fresh;

        /**
         * Number of rotten reviews.
         */
        private Integer rotten;

        /**
         * Production company.
         */
        private String production;

        /**
         * Last updated date.
         */
        private Date lastUpdated;

        /**
         * Nested class for viewer ratings.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Viewer {
            /**
             * Viewer rating (0.0 to 5.0).
             */
            private Double rating;

            /**
             * Number of viewer reviews.
             */
            private Integer numReviews;

            /**
             * Viewer meter percentage (0-100).
             */
            private Integer meter;
        }

        /**
         * Nested class for critic ratings.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Critic {
            /**
             * Critic rating (0.0 to 5.0).
             */
            private Double rating;

            /**
             * Number of critic reviews.
             */
            private Integer numReviews;

            /**
             * Critic meter percentage (0-100).
             */
            private Integer meter;
        }
    }
}

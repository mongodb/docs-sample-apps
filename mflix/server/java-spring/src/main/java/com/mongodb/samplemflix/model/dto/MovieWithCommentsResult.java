package com.mongodb.samplemflix.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for movies with their most recent comments aggregation result.
 *
 * <p>This class represents the result of the reportingByComments aggregation
 * which joins movies with their comments and returns movies with the most comments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieWithCommentsResult {

    /**
     * Movie ID as string.
     */
    private String _id;

    /**
     * Movie title.
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
     * Poster image URL.
     */
    private String poster;

    /**
     * List of genres.
     */
    private List<String> genres;

    /**
     * IMDB rating (0.0 to 10.0).
     */
    private Double imdbRating;

    /**
     * Most recent comments for this movie.
     */
    private List<CommentInfo> recentComments;

    /**
     * Total number of comments for this movie.
     */
    private Integer totalComments;

    /**
     * Date of the most recent comment.
     */
    private Date mostRecentCommentDate;

    /**
     * Nested class for comment information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentInfo {
        /**
         * Comment ID as string.
         */
        private String id;

        /**
         * Commenter name.
         */
        private String name;

        /**
         * Commenter email.
         */
        private String email;

        /**
         * Comment text.
         */
        private String text;

        /**
         * Comment date.
         */
        private Date date;
    }
}


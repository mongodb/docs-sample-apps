package com.mongodb.samplemflix.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Domain model representing a comment document from the MongoDB comments collection.
 *
 * <p>This class maps to the comments collection in the sample_mflix database.
 * Comments are user reviews/comments associated with movies.
 *
 * <p>TODO: Implement Comment functionality:
 * - Create CommentRepository extending MongoRepository
 * - Create CommentService and CommentServiceImpl
 * - Create CommentController with REST endpoints
 * - Add validation annotations (@NotNull, @Email, etc.)
 * - Add unit tests for Comment service and controller
 * - Add integration tests
 * - Implement query methods (findByMovieId, findByEmail, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {

    /**
     * MongoDB document ID.
     * Maps to the _id field in MongoDB.
     */
    @Id
    private ObjectId id;

    /**
     * Name of the commenter.
     */
    private String name;

    /**
     * Email address of the commenter.
     */
    private String email;

    /**
     * ID of the movie this comment is associated with.
     * References a document in the movies collection.
     */
    @Field("movie_id")
    private ObjectId movieId;

    /**
     * Comment text content.
     */
    private String text;

    /**
     * Date when the comment was posted.
     */
    private Date date;

}

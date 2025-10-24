package com.mongodb.samplemflix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Domain model representing a comment document from the MongoDB comments collection.
 * <p>
 * This class maps to the comments collection in the sample_mflix database.
 * Comments are user reviews/comments associated with movies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    /**
     * MongoDB document ID.
     * Maps to the _id field in MongoDB.
     */
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

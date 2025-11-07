package com.mongodb.samplemflix.exception;

/**
 * Exception thrown when a database operation fails unexpectedly.
 * 
 * This exception results in a 500 Internal Server Error response.
 * Used for cases where database operations are not acknowledged or
 * fail to complete as expected.
 */
public class DatabaseOperationException extends RuntimeException {
    
    public DatabaseOperationException(String message) {
        super(message);
    }
}


package com.mongodb.samplemflix.exception;

/**
 * Exception thrown when request validation fails.
 * 
 * This exception results in a 400 Bad Request response.
 *
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
}


package com.mongodb.samplemflix.exception;

/**
 * Exception thrown when request validation fails.
 * 
 * This exception results in a 400 Bad Request response.
 * 
 * TODO: Phase 7 - Implement custom exception
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


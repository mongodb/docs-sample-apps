package com.mongodb.samplemflix.exception;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * This exception results in a 404 Not Found response.
 * 
 * TODO: Phase 7 - Implement custom exception
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


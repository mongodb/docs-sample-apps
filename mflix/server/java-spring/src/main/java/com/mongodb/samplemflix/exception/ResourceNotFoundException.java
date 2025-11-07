package com.mongodb.samplemflix.exception;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * This exception results in a 404 Not Found response.
 *
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}


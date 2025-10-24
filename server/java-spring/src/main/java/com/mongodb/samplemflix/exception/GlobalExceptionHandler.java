package com.mongodb.samplemflix.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Global exception handler for the application.
 * 
 * This class uses @ControllerAdvice to handle exceptions thrown by controllers
 * and convert them into appropriate HTTP responses.
 * 
 * Exception types to handle:
 * - ResourceNotFoundException (404)
 * - ValidationException (400)
 * - DuplicateKeyException (409)
 * - MongoException (500)
 * - General exceptions (500)
 * 
 * TODO: Phase 7 - Implement exception handler methods
 * TODO: Phase 7 - Add @ExceptionHandler methods for each exception type
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // TODO: Phase 7 - Implement exception handlers
}


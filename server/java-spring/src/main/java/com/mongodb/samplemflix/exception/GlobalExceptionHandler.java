package com.mongodb.samplemflix.exception;

import com.mongodb.MongoWriteException;
import com.mongodb.samplemflix.model.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * Global exception handler for the application.
 *
 * This class uses @ControllerAdvice to handle exceptions thrown by controllers
 * and convert them into appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ErrorResponse.ErrorDetails.builder()
                        .message(ex.getMessage())
                        .code("RESOURCE_NOT_FOUND")
                        .build())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        logger.error("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .error(ErrorResponse.ErrorDetails.builder()
                        .message(ex.getMessage())
                        .code("VALIDATION_ERROR")
                        .build())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MongoWriteException.class)
    public ResponseEntity<ErrorResponse> handleMongoWriteException(
            MongoWriteException ex, WebRequest request) {
        logger.error("MongoDB write error: {}", ex.getMessage());

        String message = "Database error";
        String code = "DATABASE_ERROR";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex.getError().getCode() == 11000) {
            message = "Duplicate key error";
            code = "DUPLICATE_KEY";
            status = HttpStatus.CONFLICT;
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(message)
                .error(ErrorResponse.ErrorDetails.builder()
                        .message(message)
                        .code(code)
                        .details(ex.getError().getCode())
                        .build())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage() != null ? ex.getMessage() : "Internal server error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .message(ex.getMessage() != null ? ex.getMessage() : "Internal server error")
                        .code("INTERNAL_ERROR")
                        .build())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

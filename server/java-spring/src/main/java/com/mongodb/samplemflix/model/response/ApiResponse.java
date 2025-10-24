package com.mongodb.samplemflix.model.response;

/**
 * Generic API response interface.
 *
 * This interface is implemented by both SuccessResponse and ErrorResponse
 * to provide a consistent response structure across all API endpoints.
 *
 * All API responses include:
 * - success: boolean indicating if the request was successful
 * - timestamp: ISO 8601 timestamp of when the response was generated
 *
 * This matches the TypeScript ApiResponse type from the Express backend.
 */
public interface ApiResponse {

    /**
     * Indicates whether the request was successful.
     *
     * @return true for successful responses, false for error responses
     */
    boolean isSuccess();

    /**
     * Gets the timestamp when the response was generated.
     *
     * @return ISO 8601 formatted timestamp string
     */
    String getTimestamp();
}

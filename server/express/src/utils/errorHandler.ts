/**
 * Error Handling Utilities
 * 
 * This module provides centralized error handling for the Express application.
 * It includes middleware for catching and formatting errors in a consistent way.
 */

import { Request, Response, NextFunction } from 'express';
import { MongoError } from 'mongodb';

/**
 * Interface for standardized error responses
 */
interface ErrorResponse {
  success: false;
  error: {
    message: string;
    code?: string;
    details?: any;
  };
  timestamp: string;
}

/**
 * Global error handling middleware
 * 
 * This middleware catches all unhandled errors and returns a consistent
 * error response format. It should be the last middleware in the chain.
 * 
 * @param err - The error that was thrown
 * @param req - Express request object
 * @param res - Express response object
 * @param next - Express next function
 */
export function errorHandler(
  err: Error,
  req: Request,
  res: Response,
  next: NextFunction
): void {
  // Log the error for debugging purposes
  // In production, we recommend using a logging service
  console.error('Error occurred:', {
    message: err.message,
    stack: err.stack,
    url: req.url,
    method: req.method,
    timestamp: new Date().toISOString()
  });

  // Determine the appropriate HTTP status code and error message
  const errorResponse = createErrorResponse(err);
  
  // Send the error response
  res.status(errorResponse.statusCode).json({
    success: false,
    error: {
      message: errorResponse.message,
      code: errorResponse.code,
      details: errorResponse.details
    },
    timestamp: new Date().toISOString()
  } as ErrorResponse);
}

/**
 * Creates a standardized error response based on the error type
 * 
 * @param err - The error to process
 * @returns Object containing status code, message, and optional details
 */
function createErrorResponse(err: Error): {
  statusCode: number;
  message: string;
  code?: string;
  details?: any;
} {
  // Handle MongoDB-specific errors
  if (err instanceof MongoError) {
    return handleMongoError(err);
  }
  
  // Handle validation errors (you can extend this for custom validation)
  if (err.name === 'ValidationError') {
    return {
      statusCode: 400,
      message: 'Invalid input data',
      code: 'VALIDATION_ERROR',
      details: err.message
    };
  }
  
  // Handle JSON parsing errors
  if (err instanceof SyntaxError && 'body' in err) {
    return {
      statusCode: 400,
      message: 'Invalid JSON in request body',
      code: 'JSON_PARSE_ERROR'
    };
  }
  
  // Handle general application errors
  return {
    statusCode: 500,
    message: 'An unexpected error occurred',
    code: 'INTERNAL_SERVER_ERROR',
    details: process.env.NODE_ENV === 'development' ? err.message : undefined
  };
}

/**
 * Handles MongoDB-specific errors and returns appropriate responses
 * 
 * @param err - MongoDB error instance
 * @returns Object with status code and message
 */
function handleMongoError(err: MongoError): {
  statusCode: number;
  message: string;
  code: string;
  details?: any;
} {
  switch (err.code) {
    case 11000:
      // Duplicate key error
      return {
        statusCode: 409,
        message: 'Duplicate entry found',
        code: 'DUPLICATE_KEY_ERROR',
        details: 'A document with this data already exists'
      };
      
    case 121:
      // Document validation failed
      return {
        statusCode: 400,
        message: 'Document validation failed',
        code: 'DOCUMENT_VALIDATION_ERROR',
        details: err.message
      };
      
    default:
      // Generic MongoDB error
      return {
        statusCode: 500,
        message: 'Database operation failed',
        code: 'MONGODB_ERROR',
        details: process.env.NODE_ENV === 'development' ? err.message : undefined
      };
  }
}

/**
 * Async wrapper function for route handlers
 * 
 * This function wraps async route handlers to automatically catch
 * and forward any errors to the error handling middleware.
 * 
 * Usage:
 * app.get('/route', asyncHandler(async (req, res) => {
 *   // Your async code here
 * }));
 * 
 * @param fn - Async route handler function
 * @returns Express middleware function
 */
export function asyncHandler(
  fn: (req: Request, res: Response, next: NextFunction) => Promise<any>
) {
  return (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}

/**
 * Creates a standardized success response
 * 
 * @param data - The data to include in the response
 * @param message - Optional success message
 * @returns Standardized success response object
 */
export function createSuccessResponse(data: any, message?: string) {
  return {
    success: true,
    message: message || 'Operation completed successfully',
    data,
    timestamp: new Date().toISOString()
  };
}

/**
 * Validates that required fields are present in the request body
 * 
 * @param body - Request body object
 * @param requiredFields - Array of required field names
 * @throws Error if any required fields are missing
 */
export function validateRequiredFields(body: any, requiredFields: string[]): void {
  const missingFields = requiredFields.filter(field => 
    body[field] === undefined || body[field] === null || body[field] === ''
  );
  
  if (missingFields.length > 0) {
    throw new Error(`Missing required fields: ${missingFields.join(', ')}`);
  }
}
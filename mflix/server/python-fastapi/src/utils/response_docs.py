"""
OpenAPI Response Documentation Helpers

This module provides reusable response documentation for FastAPI endpoints
to maintain consistent OpenAPI documentation across all movie API endpoints.
Supports both FastAPI standard format and custom application error format.
"""

# FastAPI Standard Error Responses (HTTPException format)
FASTAPI_400_INVALID_OBJECTID = {
    "description": "Bad Request - Invalid ObjectId format",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "The provided ID 'invalid_id' is not a valid ObjectId"
                }
            }
        }
    }
}

FASTAPI_400_INVALID_SEARCH_OPERATOR = {
    "description": "Bad Request - Invalid search operator",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "Invalid search operator 'invalid'. The search operator must be one of {'must', 'should', 'mustNot', 'filter'}."
                }
            }
        }
    }
}

FASTAPI_400_VALIDATION_ERROR = {
    "description": "Bad Request - Request validation failed",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "Invalid request body format"
                }
            }
        }
    }
}

FASTAPI_422_VALIDATION_ERROR = {
    "description": "Unprocessable Entity - Validation error",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "loc": {"type": "array"},
                                "msg": {"type": "string"},
                                "type": {"type": "string"}
                            }
                        }
                    }
                },
                "example": {
                    "detail": [
                        {
                            "loc": ["body", "title"],
                            "msg": "field required",
                            "type": "value_error.missing"
                        }
                    ]
                }
            }
        }
    }
}

FASTAPI_404_MOVIE_NOT_FOUND = {
    "description": "Not Found - Movie not found",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "No movie found with ID: 507f1f77bcf86cd799439011"
                }
            }
        }
    }
}

FASTAPI_500_DATABASE_ERROR = {
    "description": "Internal Server Error - Database operation failed",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "Database error occurred: Connection timeout"
                }
            }
        }
    }
}

FASTAPI_500_SEARCH_ERROR = {
    "description": "Internal Server Error - Search operation failed", 
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "An error occurred while performing the search: Index not found"
                }
            }
        }
    }
}

FASTAPI_500_VECTOR_SEARCH_ERROR = {
    "description": "Internal Server Error - Vector search operation failed",
    "content": {
        "application/json": {
            "schema": {
                "type": "object", 
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "Error performing vector search: Embedding generation failed"
                }
            }
        }
    }
}

FASTAPI_400_MISSING_SEARCH_PARAMS = {
    "description": "Bad Request - Missing search parameters",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "detail": {"type": "string"}
                },
                "example": {
                    "detail": "At least one search parameter must be provided."
                }
            }
        }
    }
}

# Custom Application Error Responses (create_error_response format)
CUSTOM_400_VOYAGE_SERVICE_UNAVAILABLE = {
    "description": "Bad Request - Vector search service unavailable",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "success": {"type": "boolean"},
                    "message": {"type": "string"},
                    "error": {
                        "type": "object",
                        "properties": {
                            "message": {"type": "string"},
                            "code": {"type": "string"}, 
                            "details": {"type": "string"}
                        }
                    },
                    "timestamp": {"type": "string"}
                },
                "example": {
                    "success": False,
                    "message": "Vector search unavailable: VOYAGE_API_KEY not configured. Please add your API key to the .env file",
                    "error": {
                        "message": "Vector search unavailable: VOYAGE_API_KEY not configured. Please add your API key to the .env file",
                        "code": "SERVICE_UNAVAILABLE", 
                        "details": None
                    },
                    "timestamp": "2024-01-01T12:00:00.000Z"
                }
            }
        }
    }
}

CUSTOM_401_VOYAGE_AUTH_ERROR = {
    "description": "Unauthorized - Invalid Voyage AI API key",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "success": {"type": "boolean"},
                    "message": {"type": "string"},
                    "error": {
                        "type": "object",
                        "properties": {
                            "message": {"type": "string"},
                            "code": {"type": "string"},
                            "details": {"type": "string"}
                        }
                    },
                    "timestamp": {"type": "string"}
                },
                "example": {
                    "success": False,
                    "message": "Invalid Voyage AI API key. Please check your VOYAGE_API_KEY in the .env file",
                    "error": {
                        "message": "Invalid Voyage AI API key. Please check your VOYAGE_API_KEY in the .env file",
                        "code": "VOYAGE_AUTH_ERROR",
                        "details": "Please verify your VOYAGE_API_KEY is correct in the .env file"
                    },
                    "timestamp": "2024-01-01T12:00:00.000Z"
                }
            }
        }
    }
}

CUSTOM_503_VOYAGE_API_ERROR = {
    "description": "Service Unavailable - Voyage AI API error",
    "content": {
        "application/json": {
            "schema": {
                "type": "object",
                "properties": {
                    "success": {"type": "boolean"},
                    "message": {"type": "string"},
                    "error": {
                        "type": "object",
                        "properties": {
                            "message": {"type": "string"},
                            "code": {"type": "string"},
                            "details": {"type": "string"}
                        }
                    },
                    "timestamp": {"type": "string"}
                },
                "example": {
                    "success": False,
                    "message": "Vector search service unavailable",
                    "error": {
                        "message": "Voyage AI API returned status 503: Service temporarily unavailable",
                        "code": "VOYAGE_API_ERROR",
                        "details": "Voyage AI API returned status 503: Service temporarily unavailable"
                    },
                    "timestamp": "2024-01-01T12:00:00.000Z"
                }
            }
        }
    }
}

# Common response combinations for different endpoint types
OBJECTID_VALIDATION_RESPONSES = {
    400: FASTAPI_400_INVALID_OBJECTID,
    404: FASTAPI_404_MOVIE_NOT_FOUND,
    500: FASTAPI_500_DATABASE_ERROR
}

SEARCH_ENDPOINT_RESPONSES = {
    400: FASTAPI_400_INVALID_SEARCH_OPERATOR,
    500: FASTAPI_500_SEARCH_ERROR
}

VECTOR_SEARCH_RESPONSES = {
    400: CUSTOM_400_VOYAGE_SERVICE_UNAVAILABLE,
    401: CUSTOM_401_VOYAGE_AUTH_ERROR, 
    500: FASTAPI_500_VECTOR_SEARCH_ERROR,
    503: CUSTOM_503_VOYAGE_API_ERROR
}

DATABASE_OPERATION_RESPONSES = {
    500: FASTAPI_500_DATABASE_ERROR
}

CRUD_OPERATION_RESPONSES = {
    400: FASTAPI_400_VALIDATION_ERROR,
    422: FASTAPI_422_VALIDATION_ERROR,
    500: FASTAPI_500_DATABASE_ERROR
}

CRUD_WITH_OBJECTID_RESPONSES = {
    **OBJECTID_VALIDATION_RESPONSES,
    422: FASTAPI_422_VALIDATION_ERROR
}
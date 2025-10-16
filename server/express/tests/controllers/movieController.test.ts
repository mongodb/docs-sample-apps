/**
 * Unit Tests for Movie Controller
 *
 * These tests verify the business logic of movie controller functions
 * without requiring actual database connections.
 */

import { Request, Response } from "express";
import { ObjectId } from "mongodb";

// Test Data Constants
const TEST_MOVIE_ID = "507f1f77bcf86cd799439011";
const INVALID_MOVIE_ID = "invalid-id";

const SAMPLE_MOVIE = {
  _id: TEST_MOVIE_ID,
  title: "Test Movie",
  year: 2024,
  plot: "A test movie",
  genres: ["Action"],
};

const SAMPLE_MOVIES = [
  {
    _id: TEST_MOVIE_ID,
    title: "Test Movie 1",
    year: 2024,
    plot: "A test movie",
    genres: ["Action"],
  },
  {
    _id: TEST_MOVIE_ID + "-b",
    title: "Test Movie 2",
    year: 2024,
    plot: "Another test movie",
    genres: ["Comedy"],
  },
];

// Create mock collection methods
const mockFind = jest.fn();
const mockFindOne = jest.fn();
const mockInsertOne = jest.fn();
const mockInsertMany = jest.fn();
const mockUpdateOne = jest.fn();
const mockUpdateMany = jest.fn();
const mockDeleteOne = jest.fn();
const mockDeleteMany = jest.fn();
const mockFindOneAndDelete = jest.fn();
const mockToArray = jest.fn();

// Create mock database module
const mockGetCollection = jest.fn(() => ({
  find: mockFind.mockReturnValue({
    toArray: mockToArray,
    limit: jest.fn().mockReturnThis(),
    skip: jest.fn().mockReturnThis(),
    sort: jest.fn().mockReturnThis(),
  }),
  findOne: mockFindOne,
  insertOne: mockInsertOne,
  insertMany: mockInsertMany,
  updateOne: mockUpdateOne,
  updateMany: mockUpdateMany,
  deleteOne: mockDeleteOne,
  deleteMany: mockDeleteMany,
  findOneAndDelete: mockFindOneAndDelete,
}));

// Mock the database module
jest.mock("../../src/config/database", () => ({
  getCollection: mockGetCollection,
}));

// Mock the error handler utilities
const mockCreateSuccessResponse = jest.fn((data: any, message: string) => ({
  success: true,
  message,
  data,
  timestamp: "2024-01-01T00:00:00.000Z",
}));

const mockCreateErrorResponse = jest.fn(
  (message: string, code?: string, details?: any) => ({
    success: false,
    message,
    error: {
      message,
      code,
      details,
    },
    timestamp: "2024-01-01T00:00:00.000Z",
  })
);

const mockValidateRequiredFields = jest.fn();

jest.mock("../../src/utils/errorHandler", () => ({
  createSuccessResponse: mockCreateSuccessResponse,
  createErrorResponse: mockCreateErrorResponse,
  validateRequiredFields: mockValidateRequiredFields,
}));

// Import controller methods after mocks
import {
  getAllMovies,
  getMovieById,
  createMovie,
  createMoviesBatch,
  updateMovie,
  updateMoviesBatch,
  deleteMovie,
  deleteMoviesBatch,
  findAndDeleteMovie,
} from "../../src/controllers/movieController";

// Helper Functions
function createMockRequest(overrides: Partial<Request> = {}): Partial<Request> {
  return {
    query: {},
    params: {},
    body: {},
    ...overrides,
  };
}

function createMockResponse(): {
  mockJson: jest.Mock;
  mockStatus: jest.Mock;
  mockResponse: Partial<Response>;
} {
  const mockJson = jest.fn();
  const mockStatus = jest.fn().mockReturnThis();

  const mockResponse = {
    json: mockJson,
    status: mockStatus,
    setHeader: jest.fn(),
  };

  return { mockJson, mockStatus, mockResponse };
}

function expectSuccessResponse(
  mockCreateSuccessResponse: jest.Mock,
  data: any,
  message: string
) {
  expect(mockCreateSuccessResponse).toHaveBeenCalledWith(data, message);
}

function expectErrorResponse(
  mockStatus: jest.Mock,
  mockJson: jest.Mock,
  statusCode: number,
  errorMessage: string,
  errorCode: string
) {
  expect(mockStatus).toHaveBeenCalledWith(statusCode);
  expect(mockCreateErrorResponse).toHaveBeenCalledWith(errorMessage, errorCode);
  expect(mockJson).toHaveBeenCalledWith({
    success: false,
    message: errorMessage,
    error: {
      message: errorMessage,
      code: errorCode,
      details: undefined,
    },
    timestamp: "2024-01-01T00:00:00.000Z",
  });
}

describe("Movie Controller Tests", () => {
  let mockRequest: Partial<Request>;
  let mockResponse: Partial<Response>;
  let mockJson: jest.Mock;
  let mockStatus: jest.Mock;

  beforeEach(() => {
    // Reset all mocks
    jest.clearAllMocks();

    // Setup fresh response mock
    const responseMocks = createMockResponse();
    mockJson = responseMocks.mockJson;
    mockStatus = responseMocks.mockStatus;
    mockResponse = responseMocks.mockResponse;

    mockRequest = createMockRequest();
  });

  describe("getAllMovies", () => {
    it("should successfully retrieve movies", async () => {
      mockToArray.mockResolvedValue(SAMPLE_MOVIES);

      await getAllMovies(mockRequest as Request, mockResponse as Response);

      expect(mockGetCollection).toHaveBeenCalledWith("movies");
      expect(mockFind).toHaveBeenCalledWith({});
      expectSuccessResponse(
        mockCreateSuccessResponse,
        SAMPLE_MOVIES,
        "Found 2 movies"
      );
      expect(mockJson).toHaveBeenCalledWith({
        success: true,
        message: "Found 2 movies",
        data: SAMPLE_MOVIES,
        timestamp: "2024-01-01T00:00:00.000Z",
      });
    });

    it("should handle empty results", async () => {
      mockToArray.mockResolvedValue([]);

      await getAllMovies(mockRequest as Request, mockResponse as Response);

      expectSuccessResponse(mockCreateSuccessResponse, [], "Found 0 movies");
    });

    it("should handle database errors", async () => {
      const errorMessage = "Database connection failed";
      mockToArray.mockRejectedValue(new Error(errorMessage));

      await expect(
        getAllMovies(mockRequest as Request, mockResponse as Response)
      ).rejects.toThrow(errorMessage);
    });

    it("should handle query parameters for filtering", async () => {
      const testMovies = [{ _id: TEST_MOVIE_ID, title: "Action Movie" }];
      mockRequest.query = {
        genre: "Action",
        year: "2024",
        minRating: "7.0",
        limit: "10",
        sortBy: "year",
        sortOrder: "desc",
      };
      mockToArray.mockResolvedValue(testMovies);

      await getAllMovies(mockRequest as Request, mockResponse as Response);

      expect(mockFind).toHaveBeenCalledWith({
        genres: { $regex: new RegExp("Action", "i") },
        year: 2024,
        "imdb.rating": { $gte: 7.0 },
      });
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        testMovies,
        "Found 1 movies"
      );
    });
  });

  describe("getMovieById", () => {
    it("should successfully retrieve a movie by valid ID", async () => {
      mockRequest = createMockRequest({ params: { id: TEST_MOVIE_ID } });
      mockFindOne.mockResolvedValue(SAMPLE_MOVIE);

      await getMovieById(mockRequest as Request, mockResponse as Response);

      expect(mockGetCollection).toHaveBeenCalledWith("movies");
      expect(mockFindOne).toHaveBeenCalledWith({
        _id: new ObjectId(TEST_MOVIE_ID),
      });
      expectSuccessResponse(
        mockCreateSuccessResponse,
        SAMPLE_MOVIE,
        "Movie retrieved successfully"
      );
      expect(mockJson).toHaveBeenCalled();
    });

    it("should return 400 for invalid ObjectId format", async () => {
      mockRequest = createMockRequest({ params: { id: INVALID_MOVIE_ID } });

      await getMovieById(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        400,
        "Invalid movie ID format",
        "INVALID_OBJECT_ID"
      );
    });

    it("should return 404 when movie not found", async () => {
      mockRequest = createMockRequest({ params: { id: TEST_MOVIE_ID } });
      mockFindOne.mockResolvedValue(null);

      await getMovieById(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        404,
        "Movie not found",
        "MOVIE_NOT_FOUND"
      );
    });

    it("should handle database errors", async () => {
      mockRequest = createMockRequest({ params: { id: TEST_MOVIE_ID } });
      const errorMessage = "Database error";
      mockFindOne.mockRejectedValue(new Error(errorMessage));

      await expect(
        getMovieById(mockRequest as Request, mockResponse as Response)
      ).rejects.toThrow(errorMessage);
    });
  });

  describe("createMovie", () => {
    it("should successfully create a movie", async () => {
      const movieData = { title: "New Movie", year: 2024 };
      const insertResult = { acknowledged: true, insertedId: new ObjectId() };
      const createdMovie = { _id: insertResult.insertedId, ...movieData };

      mockRequest.body = movieData;
      mockInsertOne.mockResolvedValue(insertResult);
      mockFindOne.mockResolvedValue(createdMovie);

      await createMovie(mockRequest as Request, mockResponse as Response);

      expect(mockValidateRequiredFields).toHaveBeenCalledWith(movieData, [
        "title",
      ]);
      expect(mockInsertOne).toHaveBeenCalledWith(movieData);
      expect(mockFindOne).toHaveBeenCalledWith({
        _id: insertResult.insertedId,
      });
      expect(mockStatus).toHaveBeenCalledWith(201);
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        createdMovie,
        "Movie 'New Movie' created successfully"
      );
    });

    it("should handle validation errors", async () => {
      const movieData = {
        /* missing title */
      };
      mockRequest.body = movieData;

      const error = new Error("Missing required fields: title");
      mockValidateRequiredFields.mockImplementation(() => {
        throw error;
      });

      await expect(
        createMovie(mockRequest as Request, mockResponse as Response)
      ).rejects.toThrow("Missing required fields: title");
    });

    it("should handle insert acknowledgment failure", async () => {
      const movieData = { title: "Test Movie" };
      mockRequest.body = movieData;
      mockValidateRequiredFields.mockImplementation(() => {}); // Don't throw validation error
      mockInsertOne.mockResolvedValue({ acknowledged: false });

      await expect(
        createMovie(mockRequest as Request, mockResponse as Response)
      ).rejects.toThrow("Movie insertion was not acknowledged by the database");
    });
  });

  describe("createMoviesBatch", () => {
    it("should successfully create multiple movies", async () => {
      const moviesData = [{ title: "Movie 1" }, { title: "Movie 2" }];
      const insertResult = {
        acknowledged: true,
        insertedCount: 2,
        insertedIds: [new ObjectId(), new ObjectId()],
      };

      mockRequest.body = moviesData;
      mockValidateRequiredFields.mockImplementation(() => {}); // Don't throw validation error
      mockInsertMany.mockResolvedValue(insertResult);

      await createMoviesBatch(mockRequest as Request, mockResponse as Response);

      expect(mockInsertMany).toHaveBeenCalledWith(moviesData);
      expect(mockStatus).toHaveBeenCalledWith(201);
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        {
          insertedCount: 2,
          insertedIds: insertResult.insertedIds,
        },
        "Successfully created 2 movies"
      );
    });

    it("should return 400 for invalid input (not an array)", async () => {
      mockRequest.body = { title: "Single Movie" };

      await createMoviesBatch(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        400,
        "Request body must be a non-empty array of movie objects",
        "INVALID_INPUT"
      );
    });

    it("should return 400 for empty array", async () => {
      mockRequest.body = [];

      await createMoviesBatch(mockRequest as Request, mockResponse as Response);

      expect(mockStatus).toHaveBeenCalledWith(400);
    });
  });

  describe("updateMovie", () => {
    it("should successfully update a movie", async () => {
      const updateData = { title: "Updated Movie" };
      const updateResult = { matchedCount: 1, modifiedCount: 1 };
      const updatedMovie = { _id: TEST_MOVIE_ID, title: "Updated Movie" };

      mockRequest.params = { id: TEST_MOVIE_ID };
      mockRequest.body = updateData;
      mockUpdateOne.mockResolvedValue(updateResult);
      mockFindOne.mockResolvedValue(updatedMovie);

      await updateMovie(mockRequest as Request, mockResponse as Response);

      expect(mockUpdateOne).toHaveBeenCalledWith(
        { _id: new ObjectId(TEST_MOVIE_ID) },
        { $set: updateData }
      );
      expect(mockFindOne).toHaveBeenCalledWith({
        _id: new ObjectId(TEST_MOVIE_ID),
      });
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        updatedMovie,
        "Movie updated successfully. Modified 1 field(s)."
      );
    });

    it("should return 400 for invalid ObjectId", async () => {
      mockRequest.params = { id: INVALID_MOVIE_ID };
      mockRequest.body = { title: "Updated" };

      await updateMovie(mockRequest as Request, mockResponse as Response);

      expect(mockStatus).toHaveBeenCalledWith(400);
    });

    it("should return 400 for empty update data", async () => {
      mockRequest.params = { id: TEST_MOVIE_ID };
      mockRequest.body = {};

      await updateMovie(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        400,
        "No update data provided",
        "NO_UPDATE_DATA"
      );
    });

    it("should return 404 when movie not found", async () => {
      mockRequest.params = { id: TEST_MOVIE_ID };
      mockRequest.body = { title: "Updated" };
      mockUpdateOne.mockResolvedValue({ matchedCount: 0, modifiedCount: 0 });

      await updateMovie(mockRequest as Request, mockResponse as Response);

      expect(mockStatus).toHaveBeenCalledWith(404);
    });
  });

  describe("deleteMovie", () => {
    it("should successfully delete a movie", async () => {
      const deleteResult = { deletedCount: 1 };

      mockRequest.params = { id: TEST_MOVIE_ID };
      mockDeleteOne.mockResolvedValue(deleteResult);

      await deleteMovie(mockRequest as Request, mockResponse as Response);

      expect(mockDeleteOne).toHaveBeenCalledWith({
        _id: new ObjectId(TEST_MOVIE_ID),
      });
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        { deletedCount: 1 },
        "Movie deleted successfully"
      );
    });

    it("should return 400 for invalid ObjectId", async () => {
      mockRequest.params = { id: INVALID_MOVIE_ID };

      await deleteMovie(mockRequest as Request, mockResponse as Response);

      expect(mockStatus).toHaveBeenCalledWith(400);
    });

    it("should return 404 when movie not found", async () => {
      mockRequest.params = { id: TEST_MOVIE_ID };
      mockDeleteOne.mockResolvedValue({ deletedCount: 0 });

      await deleteMovie(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        404,
        "Movie not found",
        "MOVIE_NOT_FOUND"
      );
    });

    it("should handle database errors", async () => {
      mockRequest.params = { id: TEST_MOVIE_ID };
      const errorMessage = "Database error";
      mockDeleteOne.mockRejectedValue(new Error(errorMessage));

      await expect(
        deleteMovie(mockRequest as Request, mockResponse as Response)
      ).rejects.toThrow(errorMessage);
    });
  });

  describe("updateMoviesBatch", () => {
    it("should successfully update multiple movies", async () => {
      const filter = { year: 2023 };
      const update = { genre: "Updated Genre" };
      const updateResult = { matchedCount: 5, modifiedCount: 3 };

      mockRequest.body = { filter, update };
      mockUpdateMany.mockResolvedValue(updateResult);

      await updateMoviesBatch(mockRequest as Request, mockResponse as Response);

      expect(mockUpdateMany).toHaveBeenCalledWith(filter, { $set: update });
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        {
          matchedCount: 5,
          modifiedCount: 3,
        },
        "Update operation completed. Matched 5 documents, modified 3 documents."
      );
    });

    it("should return 400 when filter is missing", async () => {
      mockRequest.body = { update: { title: "Updated" } };

      await updateMoviesBatch(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        400,
        "Both filter and update objects are required",
        "MISSING_REQUIRED_FIELDS"
      );
    });

    it("should return 400 when update is empty", async () => {
      mockRequest.body = { filter: { year: 2023 }, update: {} };

      await updateMoviesBatch(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        400,
        "Update object cannot be empty",
        "EMPTY_UPDATE"
      );
    });
  });

  describe("deleteMoviesBatch", () => {
    it("should successfully delete multiple movies", async () => {
      const filter = { year: { $lt: 2000 } };
      const deleteResult = { deletedCount: 10 };

      mockRequest.body = { filter };
      mockDeleteMany.mockResolvedValue(deleteResult);

      await deleteMoviesBatch(mockRequest as Request, mockResponse as Response);

      expect(mockDeleteMany).toHaveBeenCalledWith(filter);
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        { deletedCount: 10 },
        "Delete operation completed. Removed 10 documents."
      );
    });

    it("should return 400 when filter is missing", async () => {
      mockRequest.body = {};

      await deleteMoviesBatch(mockRequest as Request, mockResponse as Response);

      expectErrorResponse(
        mockStatus,
        mockJson,
        400,
        "Filter object is required and cannot be empty. This prevents accidental deletion of all documents.",
        "MISSING_FILTER"
      );
    });

    it("should return 400 when filter is empty", async () => {
      mockRequest.body = { filter: {} };

      await deleteMoviesBatch(mockRequest as Request, mockResponse as Response);

      expect(mockStatus).toHaveBeenCalledWith(400);
    });
  });

  describe("findAndDeleteMovie", () => {
    it("should successfully find and delete a movie", async () => {
      const deletedMovie = { _id: TEST_MOVIE_ID, title: "Deleted Movie" };

      mockRequest.params = { id: TEST_MOVIE_ID };
      mockFindOneAndDelete.mockResolvedValue(deletedMovie);

      await findAndDeleteMovie(
        mockRequest as Request,
        mockResponse as Response
      );

      expect(mockFindOneAndDelete).toHaveBeenCalledWith({
        _id: new ObjectId(TEST_MOVIE_ID),
      });
      expect(mockCreateSuccessResponse).toHaveBeenCalledWith(
        deletedMovie,
        "Movie found and deleted successfully"
      );
    });

    it("should return 400 for invalid ObjectId", async () => {
      mockRequest.params = { id: INVALID_MOVIE_ID };

      await findAndDeleteMovie(
        mockRequest as Request,
        mockResponse as Response
      );

      expect(mockStatus).toHaveBeenCalledWith(400);
    });

    it("should return 404 when movie not found", async () => {
      mockRequest.params = { id: TEST_MOVIE_ID };
      mockFindOneAndDelete.mockResolvedValue(null);

      await findAndDeleteMovie(
        mockRequest as Request,
        mockResponse as Response
      );

      expectErrorResponse(
        mockStatus,
        mockJson,
        404,
        "Movie not found",
        "MOVIE_NOT_FOUND"
      );
    });

    it("should handle database errors", async () => {
      mockRequest.params = { id: TEST_MOVIE_ID };
      const errorMessage = "Database error";
      mockFindOneAndDelete.mockRejectedValue(new Error(errorMessage));

      await expect(
        findAndDeleteMovie(mockRequest as Request, mockResponse as Response)
      ).rejects.toThrow(errorMessage);
    });
  });
});

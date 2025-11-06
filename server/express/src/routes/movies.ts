/**
 * Movies API Routes
 *
 * This module defines the routing endpoints for movie operations.
 *
 * Implemented operations:
 * - insertOne() - Create a single movie
 * - insertMany() - Create multiple movies
 * - findOne() - Get a single movie by ID
 * - find() - Get multiple movies with filtering and pagination
 * - updateOne() - Update a single movie
 * - updateMany() - Update multiple movies
 * - deleteOne() - Delete a single movie
 * - deleteMany() - Delete multiple movies
 * - findOneAndDelete() - Find and delete a movie in one operation
 */

import express from "express";
import { asyncHandler } from "../utils/errorHandler";
import * as movieController from "../controllers/movieController";

const router = express.Router();

/**
 * GET /api/movies
 *
 * Retrieves multiple movies with optional filtering, sorting, and pagination.
 * Demonstrates the find() operation with various query options.
 */
router.get("/", asyncHandler(movieController.getAllMovies));

/**
 * GET /api/movies/search
 *
 * Search movies using MongoDB Search across multiple fields.
 * Demonstrates MongoDB Atlas Search with compound queries and fuzzy matching.
 */
router.get("/search", asyncHandler(movieController.searchMovies));

/**
 * GET /api/movies/vector-search
 *
 * Search movies using MongoDB Vector Search for semantic similarity.
 * Demonstrates vector search using embeddings to find similar plots.
 */
router.get("/vector-search", asyncHandler(movieController.vectorSearchMovies));

/**
 * GET /api/movies/find-similar-movies
 *
 * Find similar movies using vector search based on a movie ID.
 * Demonstrates finding semantically similar movies using plot embeddings.
 */
router.get("/find-similar-movies", asyncHandler(movieController.findSimilarMovies));

/**
 * GET /api/movies/aggregations/comments
 *
 * Aggregate movies with their most recent comments.
 * Demonstrates MongoDB $lookup aggregation to join collections.
 */
router.get("/aggregations/comments", asyncHandler(movieController.getMoviesWithMostRecentComments));

/**
 * GET /api/movies/aggregations/years
 *
 * Aggregate movies by year with statistics.
 * Demonstrates MongoDB $group aggregation for statistical calculations.
 */
router.get("/aggregations/years", asyncHandler(movieController.getMoviesByYearWithStats));

/**
 * GET /api/movies/aggregations/directors
 *
 * Aggregate directors with the most movies.
 * Demonstrates MongoDB $unwind and $group for array aggregation.
 */
router.get("/aggregations/directors", asyncHandler(movieController.getDirectorsWithMostMovies));

/**
 * GET /api/movies/:id
 *
 * Retrieves a single movie by its ObjectId.
 * Demonstrates the findOne() operation.
 */
router.get("/:id", asyncHandler(movieController.getMovieById));

/**
 * POST /api/movies
 *
 * Creates a single new movie document.
 * Demonstrates the insertOne() operation.
 */
router.post("/", asyncHandler(movieController.createMovie));

/**
 * POST /api/movies/batch
 *
 * Creates multiple movie documents in a single operation.
 * Demonstrates the insertMany() operation.
 */
router.post("/batch", asyncHandler(movieController.createMoviesBatch));

/**
 * PATCH /api/movies/:id
 *
 * Updates a single movie document.
 * Demonstrates the updateOne() operation.
 */
router.patch("/:id", asyncHandler(movieController.updateMovie));

/**
 * PATCH /api/movies
 *
 * Updates multiple movies based on a filter.
 * Demonstrates the updateMany() operation.
 */
router.patch("/", asyncHandler(movieController.updateMoviesBatch));

/**
 * DELETE /api/movies/:id/find-and-delete
 *
 * Finds and deletes a movie in a single atomic operation.
 * Demonstrates the findOneAndDelete() operation.
 */
router.delete(
  "/:id/find-and-delete",
  asyncHandler(movieController.findAndDeleteMovie)
);

/**
 * DELETE /api/movies/:id
 *
 * Deletes a single movie document.
 * Demonstrates the deleteOne() operation.
 */
router.delete("/:id", asyncHandler(movieController.deleteMovie));

/**
 * DELETE /api/movies
 *
 * Deletes multiple movies based on a filter.
 * Demonstrates the deleteMany() operation.
 */
router.delete("/", asyncHandler(movieController.deleteMoviesBatch));

export default router;

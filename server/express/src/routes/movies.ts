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
 * @swagger
 * /api/movies:
 *   get:
 *     summary: Get all movies
 *     description: Retrieves multiple movies with optional filtering, sorting, and pagination. Demonstrates the MongoDB find() operation.
 *     tags: [Movies]
 *     parameters:
 *       - in: query
 *         name: q
 *         schema:
 *           type: string
 *         description: Text search query (searches title, plot, fullplot)
 *         example: shawshank
 *       - in: query
 *         name: genre
 *         schema:
 *           type: string
 *         description: Filter by genre
 *         example: Drama
 *       - in: query
 *         name: year
 *         schema:
 *           type: integer
 *         description: Filter by year
 *         example: 1994
 *       - in: query
 *         name: minRating
 *         schema:
 *           type: number
 *         description: Minimum IMDB rating
 *         example: 8.0
 *       - in: query
 *         name: maxRating
 *         schema:
 *           type: number
 *         description: Maximum IMDB rating
 *         example: 10.0
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *           maximum: 100
 *         description: Number of results to return
 *       - in: query
 *         name: skip
 *         schema:
 *           type: integer
 *           default: 0
 *         description: Number of documents to skip for pagination
 *       - in: query
 *         name: sortBy
 *         schema:
 *           type: string
 *           default: title
 *         description: Field to sort by
 *         example: year
 *       - in: query
 *         name: sortOrder
 *         schema:
 *           type: string
 *           enum: [asc, desc]
 *           default: asc
 *         description: Sort direction
 *     responses:
 *       200:
 *         description: List of movies
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         movies:
 *                           type: array
 *                           items:
 *                             $ref: '#/components/schemas/Movie'
 *                         count:
 *                           type: integer
 *                         limit:
 *                           type: integer
 *                         skip:
 *                           type: integer
 *       400:
 *         description: Bad request
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
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
 * GET /api/movies/aggregations/reportingByComments
 *
 * Aggregate movies with their most recent comments.
 * Demonstrates MongoDB $lookup aggregation to join collections.
 */
router.get("/aggregations/reportingByComments", asyncHandler(movieController.getMoviesWithMostRecentComments));

/**
 * GET /api/movies/aggregations/reportingByYear
 *
 * Aggregate movies by year with statistics.
 * Demonstrates MongoDB $group aggregation for statistical calculations.
 */
router.get("/aggregations/reportingByYear", asyncHandler(movieController.getMoviesByYearWithStats));

/**
 * GET /api/movies/aggregations/reportingByDirectors
 *
 * Aggregate directors with the most movies.
 * Demonstrates MongoDB $unwind and $group for array aggregation.
 */
router.get("/aggregations/reportingByDirectors", asyncHandler(movieController.getDirectorsWithMostMovies));

/**
 * GET /api/movies/:id
 *
 * Retrieves a single movie by its ObjectId.
 * Demonstrates the findOne() operation.
 * @swagger
 * /api/movies/{id}:
 *   get:
 *     summary: Get a movie by ID
 *     description: Retrieves a single movie by its MongoDB ObjectId. Demonstrates the findOne() operation.
 *     tags: [Movies]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: MongoDB ObjectId of the movie
 *         example: 573a1390f29313caabcd4135
 *     responses:
 *       200:
 *         description: Movie found
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Movie'
 *       400:
 *         description: Invalid ObjectId format
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       404:
 *         description: Movie not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.get("/:id", asyncHandler(movieController.getMovieById));

/**
 * @swagger
 * /api/movies:
 *   post:
 *     summary: Create a new movie
 *     description: Creates a single new movie document. Demonstrates the MongoDB insertOne() operation.
 *     tags: [Movies]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/CreateMovieRequest'
 *     responses:
 *       201:
 *         description: Movie created successfully
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         insertedId:
 *                           type: string
 *                           description: MongoDB ObjectId of the created movie
 *       400:
 *         description: Validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post("/", asyncHandler(movieController.createMovie));

/**
 * @swagger
 * /api/movies/batch:
 *   post:
 *     summary: Create multiple movies
 *     description: Creates multiple movie documents in a single operation. Demonstrates the MongoDB insertMany() operation.
 *     tags: [Movies]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: array
 *             items:
 *               $ref: '#/components/schemas/CreateMovieRequest'
 *           example:
 *             - title: "Movie One"
 *               year: 2024
 *               genres: ["Action"]
 *             - title: "Movie Two"
 *               year: 2024
 *               genres: ["Drama"]
 *     responses:
 *       201:
 *         description: Movies created successfully
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         insertedCount:
 *                           type: integer
 *                         insertedIds:
 *                           type: object
 *       400:
 *         description: Validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post("/batch", asyncHandler(movieController.createMoviesBatch));

/**
 * @swagger
 * /api/movies/{id}:
 *   patch:
 *     summary: Update a movie
 *     description: Updates a single movie document by ID. Demonstrates the MongoDB updateOne() operation.
 *     tags: [Movies]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: MongoDB ObjectId of the movie
 *         example: 573a1390f29313caabcd4135
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/UpdateMovieRequest'
 *           example:
 *             title: "Updated Movie Title"
 *             year: 2024
 *     responses:
 *       200:
 *         description: Movie updated successfully
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         matchedCount:
 *                           type: integer
 *                         modifiedCount:
 *                           type: integer
 *       400:
 *         description: Invalid ObjectId or validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       404:
 *         description: Movie not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.patch("/:id", asyncHandler(movieController.updateMovie));

/**
 * @swagger
 * /api/movies:
 *   patch:
 *     summary: Update multiple movies
 *     description: Updates multiple movies based on a filter. Demonstrates the MongoDB updateMany() operation.
 *     tags: [Movies]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - filter
 *               - update
 *             properties:
 *               filter:
 *                 type: object
 *                 description: MongoDB filter criteria
 *                 example:
 *                   year: 1994
 *               update:
 *                 type: object
 *                 description: Fields to update
 *                 example:
 *                   rated: "PG-13"
 *     responses:
 *       200:
 *         description: Movies updated successfully
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         matchedCount:
 *                           type: integer
 *                         modifiedCount:
 *                           type: integer
 *       400:
 *         description: Validation error
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.patch("/", asyncHandler(movieController.updateMoviesBatch));

/**
 * @swagger
 * /api/movies/{id}/find-and-delete:
 *   delete:
 *     summary: Find and delete a movie
 *     description: Finds and deletes a movie in a single atomic operation. Demonstrates the MongoDB findOneAndDelete() operation.
 *     tags: [Movies]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: MongoDB ObjectId of the movie
 *         example: 573a1390f29313caabcd4135
 *     responses:
 *       200:
 *         description: Movie found and deleted
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Movie'
 *       400:
 *         description: Invalid ObjectId format
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       404:
 *         description: Movie not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.delete(
  "/:id/find-and-delete",
  asyncHandler(movieController.findAndDeleteMovie)
);

/**
 * @swagger
 * /api/movies/{id}:
 *   delete:
 *     summary: Delete a movie
 *     description: Deletes a single movie document by ID. Demonstrates the MongoDB deleteOne() operation.
 *     tags: [Movies]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *         description: MongoDB ObjectId of the movie
 *         example: 573a1390f29313caabcd4135
 *     responses:
 *       200:
 *         description: Movie deleted successfully
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         deletedCount:
 *                           type: integer
 *       400:
 *         description: Invalid ObjectId format
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 *       404:
 *         description: Movie not found
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.delete("/:id", asyncHandler(movieController.deleteMovie));

/**
 * @swagger
 * /api/movies:
 *   delete:
 *     summary: Delete multiple movies
 *     description: Deletes multiple movies based on a filter. Demonstrates the MongoDB deleteMany() operation.
 *     tags: [Movies]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - filter
 *             properties:
 *               filter:
 *                 type: object
 *                 description: MongoDB filter criteria (cannot be empty to prevent accidental deletion of all documents)
 *                 example:
 *                   year: 1990
 *     responses:
 *       200:
 *         description: Movies deleted successfully
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/SuccessResponse'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         deletedCount:
 *                           type: integer
 *       400:
 *         description: Validation error (missing or empty filter)
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.delete("/", asyncHandler(movieController.deleteMoviesBatch));

export default router;

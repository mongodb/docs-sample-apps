/**
 * Movies API Routes
 */

import express from 'express';
import { asyncHandler } from '../utils/errorHandler';
import * as movieController from '../controllers/movieController';

const router = express.Router();

/**
 * GET /api/movies
 * 
 * Retrieves multiple movies with optional filtering, sorting, and pagination.
 * Demonstrates the find() operation with various query options.
 */
router.get('/', asyncHandler(movieController.getAllMovies));

export default router;
/**
 * Movie Controller
 */

import { Request, Response } from 'express';
import { getCollection } from '../config/database';
import { createSuccessResponse } from '../utils/errorHandler';


/**
 * GET /api/movies
 */
export async function getAllMovies(req: Request, res: Response): Promise<void> {
  const moviesCollection = getCollection('movies');

  try {
    // Execute the find operation with all options
    const movies = await moviesCollection
      .find({})
      .limit(10) // TODO: Remove temp limit used for testing
      .toArray();

    // Return successful response
    res.json(createSuccessResponse(movies, `Found ${movies.length} movies`));
    
  } catch (error) {
    throw error;
  }
}
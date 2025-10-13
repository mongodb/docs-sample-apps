/**
 * Movie Controller
 * 
 * This file contains all the business logic for movie operations.
 * Each method demonstrates different MongoDB operations using the Node.js driver.
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

import { Request, Response } from 'express';
import { ObjectId } from 'mongodb';
import { getCollection } from '../config/database';
import { createSuccessResponse, validateRequiredFields } from '../utils/errorHandler';
import { 
  Movie, 
  CreateMovieRequest, 
  UpdateMovieRequest
} from '../types';

/**
 * GET /api/movies
 * 
 * Retrieves multiple movies with optional filtering, sorting, and pagination.
 * Demonstrates the find() operation with various query options.
 * 
 * Query parameters:
 * - q: Text search query (searches title, plot, fullplot)
 * - genre: Filter by genre
 * - year: Filter by year
 * - minRating: Minimum IMDB rating
 * - maxRating: Maximum IMDB rating
 * - limit: Number of results (default: 20, max: 100)
 * - skip: Number of documents to skip for pagination
 * - sortBy: Field to sort by (default: title)
 * - sortOrder: Sort direction - asc or desc (default: asc)
 */
export async function getAllMovies(req: Request, res: Response): Promise<void> {
  const moviesCollection = getCollection('movies');
  
  // Extract and validate query parameters
  const {
    q,
    genre,
    year,
    minRating,
    maxRating,
    limit = '20',
    skip = '0',
    sortBy = 'title',
    sortOrder = 'asc'
  } = req.query as { [key: string]: string };

  // Build MongoDB query filter
  // This demonstrates how to construct complex queries with multiple conditions
  const filter: any = {};

  // Text search by using MongoDB's text index
  // This requires the text index we created in the database verification
  if (q) {
    filter.$text = { $search: q };
  }

  // Genre filtering
  if (genre) {
    filter.genres = { $regex: new RegExp(genre, 'i') };
  }

  // Year filtering
  if (year) {
    filter.year = parseInt(year);
  }

  // Rating range filtering
  // Demonstrates nested field queries (imdb.rating)
  if (minRating || maxRating) {
    filter['imdb.rating'] = {};
    if (minRating) {
      filter['imdb.rating'].$gte = parseFloat(minRating);
    }
    if (maxRating) {
      filter['imdb.rating'].$lte = parseFloat(maxRating);
    }
  }

  // Parse pagination parameters
  const limitNum = Math.min(parseInt(limit), 100); // Cap at 100 for performance
  const skipNum = parseInt(skip);

  // Build sort object
  // Demonstrates dynamic sorting based on user input
  const sort: any = {};
  sort[sortBy] = sortOrder === 'desc' ? -1 : 1;

  try {
    // Execute the find operation with all options
    const movies = await moviesCollection
      .find(filter)
      .sort(sort)
      .limit(limitNum)
      .skip(skipNum)
      .toArray();

    // Return successful response
    res.json(createSuccessResponse(movies, `Found ${movies.length} movies`));

  } catch (error) {
    throw error;
  }
}

/**
 * GET /api/movies/:id
 * 
 * Retrieves a single movie by its ObjectId.
 * Demonstrates the findOne() operation.
 */
export async function getMovieById(req: Request, res: Response): Promise<void> {
  const { id } = req.params;
  
  // Validate ObjectId format
  if (!ObjectId.isValid(id)) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Invalid movie ID format',
        code: 'INVALID_OBJECT_ID'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  const moviesCollection = getCollection('movies');

  try {
    // Use findOne() to get a single document by _id
    const movie = await moviesCollection.findOne({ _id: new ObjectId(id) });

    if (!movie) {
      res.status(404).json({
        success: false,
        error: {
          message: 'Movie not found',
          code: 'MOVIE_NOT_FOUND'
        },
        timestamp: new Date().toISOString()
      });
      return;
    }

    res.json(createSuccessResponse(movie, 'Movie retrieved successfully'));

  } catch (error) {
    throw new Error(`Failed to retrieve movie: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * POST /api/movies
 * 
 * Creates a single new movie document.
 * Demonstrates the insertOne() operation.
 */
export async function createMovie(req: Request, res: Response): Promise<void> {
  const movieData: CreateMovieRequest = req.body;

  // Validate required fields
  // The title field is the minimum requirement for a movie
  validateRequiredFields(movieData, ['title']);

  const moviesCollection = getCollection('movies');

  try {
    // Prepare the document for insertion
    // Here you can add metadata or other fields that might be necessary
    const movieDocument: Partial<Movie> = {
      ...movieData
    };

    // Use insertOne() to create a single document
    // This operation returns information about the insertion including the new _id
    const result = await moviesCollection.insertOne(movieDocument);

    if (!result.acknowledged) {
      throw new Error('Movie insertion was not acknowledged by the database');
    }

    // Retrieve the created document to return complete data
    const createdMovie = await moviesCollection.findOne({ _id: result.insertedId });

    res.status(201).json(
      createSuccessResponse(
        createdMovie, 
        `Movie '${movieData.title}' created successfully`
      )
    );

  } catch (error) {
    throw new Error(`Failed to create movie: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * POST /api/movies/batch
 * 
 * Creates multiple movie documents in a single operation.
 * Demonstrates the insertMany() operation.
 */
export async function createMoviesBatch(req: Request, res: Response): Promise<void> {
  const moviesData: CreateMovieRequest[] = req.body;

  // Validate that we have an array of movies
  if (!Array.isArray(moviesData) || moviesData.length === 0) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Request body must be a non-empty array of movie objects',
        code: 'INVALID_INPUT'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  // Validate each movie has required fields
  moviesData.forEach((movie, index) => {
    try {
      validateRequiredFields(movie, ['title']);
    } catch (error) {
      throw new Error(`Movie at index ${index}: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });

  const moviesCollection = getCollection('movies');

  try {
    // Use insertMany() to create multiple documents
    const result = await moviesCollection.insertMany(moviesData);

    if (!result.acknowledged) {
      throw new Error('Batch movie insertion was not acknowledged by the database');
    }

    res.status(201).json(
      createSuccessResponse(
        {
          insertedCount: result.insertedCount,
          insertedIds: result.insertedIds
        },
        `Successfully created ${result.insertedCount} movies`
      )
    );

  } catch (error) {
    throw new Error(`Failed to create movies: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * PUT /api/movies/:id
 * 
 * Updates a single movie document.
 * Demonstrates the updateOne() operation.
 */
export async function updateMovie(req: Request, res: Response): Promise<void> {
  const { id } = req.params;
  const updateData: UpdateMovieRequest = req.body;

  // Validate ObjectId format
  if (!ObjectId.isValid(id)) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Invalid movie ID format',
        code: 'INVALID_OBJECT_ID'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  // Ensure we have something to update
  if (Object.keys(updateData).length === 0) {
    res.status(400).json({
      success: false,
      error: {
        message: 'No update data provided',
        code: 'NO_UPDATE_DATA'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  const moviesCollection = getCollection('movies');

  try {
    // Use updateOne() to update a single document
    // $set operator replaces the value of fields with specified values
    const result = await moviesCollection.updateOne(
      { _id: new ObjectId(id) },
      { $set: updateData }
    );

    if (result.matchedCount === 0) {
      res.status(404).json({
        success: false,
        error: {
          message: 'Movie not found',
          code: 'MOVIE_NOT_FOUND'
        },
        timestamp: new Date().toISOString()
      });
      return;
    }

    // Retrieve the updated document to return complete data
    const updatedMovie = await moviesCollection.findOne({ _id: new ObjectId(id) });

    res.json(
      createSuccessResponse(
        updatedMovie,
        `Movie updated successfully. Modified ${result.modifiedCount} field(s).`
      )
    );

  } catch (error) {
    throw new Error(`Failed to update movie: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * PATCH /api/movies
 * 
 * Updates multiple movies based on a filter.
 * Demonstrates the updateMany() operation.
 */
export async function updateMoviesBatch(req: Request, res: Response): Promise<void> {
  const { filter, update } = req.body;

  // Validate input
  if (!filter || !update) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Both filter and update objects are required',
        code: 'MISSING_REQUIRED_FIELDS'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  if (Object.keys(update).length === 0) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Update object cannot be empty',
        code: 'EMPTY_UPDATE'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  const moviesCollection = getCollection('movies');

  try {
    // Use updateMany() to update multiple documents
    // This is useful for bulk operations like updating all movies from a certain year
    const result = await moviesCollection.updateMany(
      filter,
      { $set: update }
    );

    res.json(
      createSuccessResponse(
        {
          matchedCount: result.matchedCount,
          modifiedCount: result.modifiedCount
        },
        `Update operation completed. Matched ${result.matchedCount} documents, modified ${result.modifiedCount} documents.`
      )
    );

  } catch (error) {
    throw new Error(`Failed to update movies: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * DELETE /api/movies/:id
 * 
 * Deletes a single movie document.
 * Demonstrates the deleteOne() operation.
 */
export async function deleteMovie(req: Request, res: Response): Promise<void> {
  const { id } = req.params;

  // Validate ObjectId format
  if (!ObjectId.isValid(id)) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Invalid movie ID format',
        code: 'INVALID_OBJECT_ID'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  const moviesCollection = getCollection('movies');

  try {
    // Use deleteOne() to remove a single document
    const result = await moviesCollection.deleteOne({ _id: new ObjectId(id) });

    if (result.deletedCount === 0) {
      res.status(404).json({
        success: false,
        error: {
          message: 'Movie not found',
          code: 'MOVIE_NOT_FOUND'
        },
        timestamp: new Date().toISOString()
      });
      return;
    }

    res.json(
      createSuccessResponse(
        { deletedCount: result.deletedCount },
        'Movie deleted successfully'
      )
    );

  } catch (error) {
    throw new Error(`Failed to delete movie: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * DELETE /api/movies
 * 
 * Deletes multiple movies based on a filter.
 * Demonstrates the deleteMany() operation.
 */
export async function deleteMoviesBatch(req: Request, res: Response): Promise<void> {
  const { filter } = req.body;

  // Validate input
  if (!filter || Object.keys(filter).length === 0) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Filter object is required and cannot be empty. This prevents accidental deletion of all documents.',
        code: 'MISSING_FILTER'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  const moviesCollection = getCollection('movies');

  try {
    // Use deleteMany() to remove multiple documents
    // This operation is useful for cleanup tasks like removing all movies from a certain year
    const result = await moviesCollection.deleteMany(filter);

    res.json(
      createSuccessResponse(
        { deletedCount: result.deletedCount },
        `Delete operation completed. Removed ${result.deletedCount} documents.`
      )
    );

  } catch (error) {
    throw new Error(`Failed to delete movies: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}

/**
 * DELETE /api/movies/:id/find-and-delete
 * 
 * Finds and deletes a movie in a single atomic operation.
 * Demonstrates the findOneAndDelete() operation.
 */
export async function findAndDeleteMovie(req: Request, res: Response): Promise<void> {
  const { id } = req.params;

  // Validate ObjectId format
  if (!ObjectId.isValid(id)) {
    res.status(400).json({
      success: false,
      error: {
        message: 'Invalid movie ID format',
        code: 'INVALID_OBJECT_ID'
      },
      timestamp: new Date().toISOString()
    });
    return;
  }

  const moviesCollection = getCollection('movies');

  try {
    // Use findOneAndDelete() to find and delete in a single atomic operation
    // This is useful when you need to return the deleted document
    // or ensure the document exists before deletion
    const deletedMovie = await moviesCollection.findOneAndDelete(
      { _id: new ObjectId(id) }
    );

    if (!deletedMovie) {
      res.status(404).json({
        success: false,
        error: {
          message: 'Movie not found',
          code: 'MOVIE_NOT_FOUND'
        },
        timestamp: new Date().toISOString()
      });
      return;
    }

    res.json(
      createSuccessResponse(
        deletedMovie,
        'Movie found and deleted successfully'
      )
    );

  } catch (error) {
    throw new Error(`Failed to find and delete movie: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
}
/**
 * TypeScript Type Definitions for MongoDB Documents
 * 
 * These interfaces define the structure of documents in the sample_mflix database.
 * They help ensure type safety when working with MongoDB operations.
 */

import { ObjectId } from 'mongodb';

/**
 * Interface for Movie documents in the movies collection
 * 
 * This represents the structure of movie documents in the sample_mflix.movies collection.
 */
export interface Movie {
  _id?: ObjectId;
  title: string;
  year?: number;
  plot?: string;
  fullplot?: string;
  released?: Date;
  runtime?: number;
  poster?: string;
  genres?: string[];
  directors?: string[];
  writers?: string[];
  cast?: string[];
  countries?: string[];
  languages?: string[];
  rated?: string;
  awards?: {
    wins?: number;
    nominations?: number;
    text?: string;
  };

  imdb?: {
    rating?: number;
    votes?: number;
    id?: number;
  };

  tomatoes?: {
    viewer?: {
      rating?: number;
      numReviews?: number;
      meter?: number;
    };
    critic?: {
      rating?: number;
      numReviews?: number;
      meter?: number;
    };
    fresh?: number;
    rotten?: number;
    production?: string;
    lastUpdated?: Date;
  };

  metacritic?: number;
  type?: string;
}

/**
 * Interface for Theater documents in the theaters collection
 */
export interface Theater {
  _id?: ObjectId;
  theaterId: number;
  location: {
    address: {
      street1: string;
      city: string;
      state: string;
      zipcode: string;
    };
    geo: {
      type: 'Point';
      coordinates: [number, number]; // [longitude, latitude]
    };
  };
}

/**
 * Interface for Comment documents in the comments collection
 */
export interface Comment {
  _id?: ObjectId;
  name: string;
  email: string;
  movie_id: ObjectId;
  text: string;
  date: Date;
}

/**
 * Interface for API request bodies when creating/updating movies
 */
export interface CreateMovieRequest {
  title: string;
  year?: number;
  plot?: string;
  fullplot?: string;
  genres?: string[];
  directors?: string[];
  writers?: string[];
  cast?: string[];
  countries?: string[];
  languages?: string[];
  rated?: string;
  runtime?: number;
  poster?: string;
}

/**
 * Interface for API request bodies when updating movies
 * All fields are optional for partial updates
 */
export interface UpdateMovieRequest {
  title?: string;
  year?: number;
  plot?: string;
  fullplot?: string;
  genres?: string[];
  directors?: string[];
  writers?: string[];
  cast?: string[];
  countries?: string[];
  languages?: string[];
  rated?: string;
  runtime?: number;
  poster?: string;
}

/**
 * Interface for search query parameters
 */
export interface SearchQuery {
  q?: string;
  genre?: string;
  year?: number;
  minRating?: number;
  maxRating?: number;
  limit?: number;
  skip?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

/**
 * Standard API response wrapper
 */
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  error?: {
    message: string;
    code?: string;
    details?: any;
  };
  timestamp: string;
  pagination?: {
    page: number;
    limit: number;
    total: number;
    pages: number;
  };
}

/**
 * Type for MongoDB operation results
 */
export interface OperationResult {
  acknowledged: boolean;
  insertedId?: ObjectId;
  insertedIds?: ObjectId[];
  modifiedCount?: number;
  deletedCount?: number;
  matchedCount?: number;
  upsertedCount?: number;
  upsertedId?: ObjectId;
}
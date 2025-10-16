/**
 * Shared type definitions for the Movie application
 * These types match the backend API response structure
 */

/**
 * Movie interface for type safety
 * Matches the Movie type from the Express backend
 */
export interface Movie {
  _id: string;
  title: string;
  year?: number;
  plot?: string;
  poster?: string;
  genres?: string[];
  imdb?: {
    rating?: number;
  };
}

/**
 * API Response interface for the movies endpoint
 * Matches the SuccessResponse type from the Express backend
 */
export interface MoviesApiResponse {
  success: boolean;
  data: Movie[];
  message?: string;
}

/**
 * Props interface for MovieCard component
 */
export interface MovieCardProps {
  movie: Movie;
}
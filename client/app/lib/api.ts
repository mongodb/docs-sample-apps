import { Movie, MoviesApiResponse } from '../types/movie';

/**
 * API configuration and helper functions
 */

const API_BASE_URL = process.env.API_URL || 'http://localhost:3001';

/**
 * Fetches movies from the Express API with pagination support
 * This function runs on the server during SSR
 */
export async function fetchMovies(
  limit: number = 20, 
  skip: number = 0
): Promise<{ movies: Movie[]; hasNextPage: boolean; hasPrevPage: boolean }> {
  try {
    // Request one extra movie to check if there's a next page
    const requestLimit = Math.min(limit + 1, 100);
    const response = await fetch(`${API_BASE_URL}/api/movies?limit=${requestLimit}&skip=${skip}`, {
      next: { revalidate: 300 }, // Revalidate every 5 minutes
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch movies: ${response.status}`);
    }

    const result: MoviesApiResponse = await response.json();
    
    if (!result.success) {
      throw new Error('API returned error response');
    }

    const hasNextPage = result.data.length > limit;
    const movies = hasNextPage ? result.data.slice(0, limit) : result.data;
    const hasPrevPage = skip > 0;

    return {
      movies,
      hasNextPage,
      hasPrevPage
    };
  } catch (error) {
    console.error('Error fetching movies:', error);
    
    // In development, throw the error to help with debugging
    if (process.env.NODE_ENV === 'development') {
      throw error;
    }
    
    // In production, return empty result with logged error to prevent page crash
    return {
      movies: [],
      hasNextPage: false,
      hasPrevPage: false
    };
  }
}

/**
 * Fetch a single movie by ID
 */
export async function fetchMovieById(id: string): Promise<Movie | null> {
  try {
    // Validate the ID format (basic validation)
    if (!id || id.length !== 24) {
      console.warn('Invalid movie ID format:', id);
      return null;
    }

    const response = await fetch(`${API_BASE_URL}/api/movies/${id}`, {
      next: { revalidate: 300 },
    });

    if (!response.ok) {
      console.warn(`Failed to fetch movie ${id}: ${response.status}`);
      return null;
    }

    const result = await response.json();
    
    if (!result.success) {
      console.warn('API returned error response for movie:', id);
      return null;
    }

    return result.data;
  } catch (error) {
    console.error('Error fetching movie:', error);
    return null;
  }
}
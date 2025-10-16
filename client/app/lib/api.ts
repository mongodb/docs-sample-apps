import { Movie, MoviesApiResponse } from '../types/movie';

/**
 * API configuration and helper functions
 */

const API_BASE_URL = process.env.API_URL || 'http://localhost:3001';

/**
 * Fetches all movies from the Express API
 * This function runs on the server during SSR
 */
export async function fetchMovies(limit: number = 50): Promise<Movie[]> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/movies?limit=${limit}`, {
      next: { revalidate: 300 }, // Revalidate every 5 minutes
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch movies: ${response.status}`);
    }

    const result: MoviesApiResponse = await response.json();
    
    if (!result.success) {
      throw new Error('API returned error response');
    }

    return result.data;
  } catch (error) {
    console.error('Error fetching movies:', error);
    // Return empty array instead of throwing to prevent page crash
    return [];
  }
}

/**
 * Fetch a single movie by ID
 */
export async function fetchMovieById(id: string): Promise<Movie | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/movies/${id}`, {
      next: { revalidate: 300 },
    });

    if (!response.ok) {
      return null;
    }

    const result = await response.json();
    return result.success ? result.data : null;
  } catch (error) {
    console.error('Error fetching movie:', error);
    return null;
  }
}
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

/**
 * Update a movie by ID
 */
export async function updateMovie(id: string, updateData: Partial<Movie>): Promise<{ success: boolean; error?: string }> {
  try {
    // Validate the ID format
    if (!id || id.length !== 24) {
      return { success: false, error: 'Invalid movie ID format' };
    }

    // Remove the _id field from update data if present
    const { _id, ...dataToUpdate } = updateData;

    const response = await fetch(`${API_BASE_URL}/api/movies/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(dataToUpdate),
    });

    const result = await response.json();

    if (!response.ok) {
      return { 
        success: false, 
        error: result.error || `Failed to update movie: ${response.status}` 
      };
    }

    if (!result.success) {
      return { 
        success: false, 
        error: result.error || 'API returned error response' 
      };
    }

    return { success: true };
  } catch (error) {
    console.error('Error updating movie:', error);
    return { 
      success: false, 
      error: 'Network error occurred while updating movie' 
    };
  }
}

/**
 * Delete a movie by ID
 */
export async function deleteMovie(id: string): Promise<{ success: boolean; error?: string }> {
  try {
    // Validate the ID format
    if (!id || id.length !== 24) {
      return { success: false, error: 'Invalid movie ID format' };
    }

    const response = await fetch(`${API_BASE_URL}/api/movies/${id}`, {
      method: 'DELETE',
    });

    const result = await response.json();

    if (!response.ok) {
      return { 
        success: false, 
        error: result.error || `Failed to delete movie: ${response.status}` 
      };
    }

    if (!result.success) {
      return { 
        success: false, 
        error: result.error || 'API returned error response' 
      };
    }

    return { success: true };
  } catch (error) {
    console.error('Error deleting movie:', error);
    return { 
      success: false, 
      error: 'Network error occurred while deleting movie' 
    };
  }
}

/**
 * Create a new movie
 */
export async function createMovie(movieData: Omit<Movie, '_id'>): Promise<{ success: boolean; error?: string; movieId?: string }> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/movies`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(movieData),
    });

    const result = await response.json();

    if (!response.ok) {
      return { 
        success: false, 
        error: result.error || `Failed to create movie: ${response.status}` 
      };
    }

    if (!result.success) {
      return { 
        success: false, 
        error: result.error || 'API returned error response' 
      };
    }

    return { 
      success: true, 
      movieId: result.data._id || result.data.insertedId 
    };
  } catch (error) {
    console.error('Error creating movie:', error);
    return { 
      success: false, 
      error: 'Network error occurred while creating movie' 
    };
  }
}

/**
 * Create multiple movies in a batch operation
 */
export async function createMoviesBatch(moviesData: Omit<Movie, '_id'>[]): Promise<{ success: boolean; error?: string; insertedCount?: number; insertedIds?: string[] }> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/movies/batch`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(moviesData),
    });

    const result = await response.json();

    if (!response.ok) {
      return { 
        success: false, 
        error: result.error || `Failed to create movies: ${response.status}` 
      };
    }

    if (!result.success) {
      return { 
        success: false, 
        error: result.error || 'API returned error response' 
      };
    }

    return { 
      success: true, 
      insertedCount: result.data.insertedCount,
      insertedIds: result.data.insertedIds ? Object.values(result.data.insertedIds) : []
    };
  } catch (error) {
    console.error('Error creating movies batch:', error);
    return { 
      success: false, 
      error: 'Network error occurred while creating movies' 
    };
  }
}
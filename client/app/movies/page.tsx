'use client';

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import pageStyles from "./page.module.css";
import movieStyles from "./movies.module.css";
import { MovieCard, Pagination, PageSizeSelector, AddMovieForm } from "../components";
import { ErrorDisplay, LoadingSpinner } from "../components/ui";
import { fetchMovies, createMovie, createMoviesBatch } from "../lib/api";
import { Movie } from "../types/movie";
import { APP_CONFIG, ROUTES } from "../lib/constants";

export default function Movies() {
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const [movies, setMovies] = useState<Movie[]>([]);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [hasPrevPage, setHasPrevPage] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  
  const page = parseInt(searchParams.get('page') || '1');
  const limit = Math.min(
    parseInt(searchParams.get('limit') || APP_CONFIG.defaultMovieLimit.toString()), 
    APP_CONFIG.maxMovieLimit
  );
  const skip = (page - 1) * limit;

  const loadMovies = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const result = await fetchMovies(limit, skip);
      setMovies(result.movies);
      setHasNextPage(result.hasNextPage);
      setHasPrevPage(result.hasPrevPage);
    } catch (err) {
      setError('Failed to load movies. Make sure the Express server is running on port 3001.');
      setMovies([]);
    }
    
    setIsLoading(false);
  };

  useEffect(() => {
    loadMovies();
  }, [page, limit]);

  const handleAddMovie = () => {
    setShowAddForm(true);
    setError(null);
    setSuccessMessage(null);
  };

  const handleCancelAdd = () => {
    setShowAddForm(false);
    setError(null);
    setSuccessMessage(null);
  };

  const handleSaveMovie = async (moviesData: Omit<Movie, '_id'>[]) => {
    setIsCreating(true);
    setError(null);
    setSuccessMessage(null);

    // Choose between single and batch creation based on number of movies
    if (moviesData.length === 1) {
      // Single movie creation
      const result = await createMovie(moviesData[0]);

      if (result.success) {
        setSuccessMessage('Movie created successfully!');
        setShowAddForm(false);
        
        // If we have a movieId, redirect to the new movie's page after a brief delay
        if (result.movieId) {
          setTimeout(() => {
            router.push(ROUTES.movie(result.movieId!));
          }, 2000);
        } else {
          // Otherwise, refresh the movies list
          loadMovies();
        }
      } else {
        setError(result.error || 'Failed to create movie');
      }
    } else {
      // Batch movie creation
      const result = await createMoviesBatch(moviesData);

      if (result.success) {
        setSuccessMessage(`Successfully created ${result.insertedCount} movies!`);
        setShowAddForm(false);
        
        // Refresh the movies list after batch creation
        setTimeout(() => {
          loadMovies();
        }, 2000);
      } else {
        setError(result.error || 'Failed to create movies');
      }
    }

    setIsCreating(false);
  };

  if (isLoading && !showAddForm) {
    return (
      <div className={pageStyles.page}>
        <main className={pageStyles.main}>
          <div style={{ textAlign: 'center', padding: '2rem' }}>
            <LoadingSpinner />
            <p>Loading movies...</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className={pageStyles.page}>
      <main className={pageStyles.main}>
        <div className={movieStyles.pageHeader}>
          <h1 className={movieStyles.pageTitle}>Movies</h1>
          
          <button
            onClick={handleAddMovie}
            disabled={showAddForm || isCreating}
            className={movieStyles.addButton}
            type="button"
          >
            {isCreating ? 'Creating...' : '+ Add Movie'}
          </button>
        </div>

        {/* Success/Error Messages */}
        {successMessage && (
          <div className={movieStyles.successMessage}>
            {successMessage}
          </div>
        )}
        
        {error && (
          <div className={movieStyles.errorMessage}>
            {error}
          </div>
        )}

        {/* Add Movie Form */}
        {showAddForm && (
          <AddMovieForm
            onSave={handleSaveMovie}
            onCancel={handleCancelAdd}
            isLoading={isCreating}
          />
        )}

        {/* Page Size Selector */}
        {!showAddForm && <PageSizeSelector currentLimit={limit} />}
        
        {/* Movies Content */}
        {!showAddForm && (
          <>
            {error && movies.length === 0 ? (
              <ErrorDisplay 
                message={error} 
                onRetry={loadMovies}
              />
            ) : movies.length === 0 ? (
              <div className={movieStyles.noMovies}>
                <p>No movies found. Make sure the Express server is running on port 3001.</p>
              </div>
            ) : (
              <>
                <div className={movieStyles.moviesGrid}>
                  {movies.map((movie) => (
                    <MovieCard key={movie._id} movie={movie} />
                  ))}
                </div>
                
                <Pagination
                  currentPage={page}
                  hasNextPage={hasNextPage}
                  hasPrevPage={hasPrevPage}
                  limit={limit}
                />
              </>
            )}
          </>
        )}
      </main>
    </div>
  );
}

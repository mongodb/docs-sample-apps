'use client';

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import pageStyles from "./page.module.css";
import movieStyles from "./movies.module.css";
import { MovieCard, Pagination, PageSizeSelector, AddMovieForm, BatchEditMovieForm } from "../components";
import { ErrorDisplay, LoadingSpinner } from "../components/ui";
import { fetchMovies, createMovie, createMoviesBatch, deleteMoviesBatch, updateMoviesBatch } from "../lib/api";
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
  const [isDeleting, setIsDeleting] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [showBatchEditForm, setShowBatchEditForm] = useState(false);
  const [selectedMovies, setSelectedMovies] = useState<Set<string>>(new Set());
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);
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

  const handleMovieSelection = (movieId: string, isSelected: boolean) => {
    setSelectedMovies(prev => {
      const newSelection = new Set(prev);
      if (isSelected) {
        newSelection.add(movieId);
      } else {
        newSelection.delete(movieId);
      }
      return newSelection;
    });
  };

  const handleBatchDelete = () => {
    if (selectedMovies.size > 0) {
      setShowDeleteConfirmation(true);
    }
  };

  const handleBatchUpdate = () => {
    if (selectedMovies.size > 0) {
      setShowBatchEditForm(true);
      setError(null);
      setSuccessMessage(null);
    }
  };

  const handleCancelBatchEdit = () => {
    setShowBatchEditForm(false);
    setError(null);
    setSuccessMessage(null);
  };

  const handleSaveBatchEdit = async (updateData: Partial<Movie>) => {
    setIsUpdating(true);
    setError(null);
    setSuccessMessage(null);

    const movieIds = Array.from(selectedMovies);
    const result = await updateMoviesBatch(movieIds, updateData);

    if (result.success) {
      setSuccessMessage(`Successfully updated ${result.modifiedCount} out of ${result.matchedCount} movies!`);
      setShowBatchEditForm(false);
      setSelectedMovies(new Set()); // Clear selection
      
      // Refresh the movies list
      setTimeout(() => {
        loadMovies();
      }, 1500);
    } else {
      setError(result.error || 'Failed to update movies');
    }

    setIsUpdating(false);
  };

  const confirmBatchDelete = async () => {
    setIsDeleting(true);
    setError(null);
    setSuccessMessage(null);
    setShowDeleteConfirmation(false);

    const movieIds = Array.from(selectedMovies);
    const result = await deleteMoviesBatch(movieIds);

    if (result.success) {
      setSuccessMessage(`Successfully deleted ${result.deletedCount} movies!`);
      setSelectedMovies(new Set()); // Clear selection
      
      // Refresh the movies list
      setTimeout(() => {
        loadMovies();
      }, 1500);
    } else {
      setError(result.error || 'Failed to delete movies');
    }

    setIsDeleting(false);
  };

  const cancelBatchDelete = () => {
    setShowDeleteConfirmation(false);
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
          
          <div className={movieStyles.headerActions}>
            {/* Batch Selection Controls */}
            {!showAddForm && !showBatchEditForm && movies.length > 0 && (
              <div className={movieStyles.selectionControls}>
                {selectedMovies.size > 0 && (
                  <>
                    <button
                      onClick={handleBatchUpdate}
                      disabled={isUpdating}
                      className={movieStyles.batchUpdateButton}
                      type="button"
                    >
                      {isUpdating ? 'Updating...' : `Update ${selectedMovies.size} Selected`}
                    </button>
                    
                    <button
                      onClick={handleBatchDelete}
                      disabled={isDeleting}
                      className={movieStyles.batchDeleteButton}
                      type="button"
                    >
                      {isDeleting ? 'Deleting...' : `Delete ${selectedMovies.size} Selected`}
                    </button>
                  </>
                )}
              </div>
            )}

            <button
              onClick={handleAddMovie}
              disabled={showAddForm || showBatchEditForm || isCreating}
              className={movieStyles.addButton}
              type="button"
            >
              {isCreating ? 'Creating...' : '+ Add Movie'}
            </button>
          </div>
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

        {/* Batch Edit Movie Form */}
        {showBatchEditForm && (
          <BatchEditMovieForm
            selectedCount={selectedMovies.size}
            onSave={handleSaveBatchEdit}
            onCancel={handleCancelBatchEdit}
            isLoading={isUpdating}
          />
        )}

        {/* Page Size Selector */}
        {!showAddForm && !showBatchEditForm && <PageSizeSelector currentLimit={limit} />}
        
        {/* Movies Content */}
        {!showAddForm && !showBatchEditForm && (
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
                    <MovieCard 
                      key={movie._id} 
                      movie={movie} 
                      isSelected={selectedMovies.has(movie._id)}
                      onSelectionChange={handleMovieSelection}
                      showCheckbox={!showAddForm && !showBatchEditForm}
                    />
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

        {/* Batch Delete Confirmation Dialog */}
        {showDeleteConfirmation && (
          <div className={movieStyles.confirmationOverlay}>
            <div className={movieStyles.confirmationDialog}>
              <h3 className={movieStyles.confirmationTitle}>Confirm Batch Delete</h3>
              <p className={movieStyles.confirmationMessage}>
                Are you sure you want to delete {selectedMovies.size} selected movie{selectedMovies.size !== 1 ? 's' : ''}? 
                This action cannot be undone.
              </p>
              <div className={movieStyles.confirmationActions}>
                <button
                  onClick={cancelBatchDelete}
                  className={movieStyles.cancelButton}
                  type="button"
                >
                  Cancel
                </button>
                <button
                  onClick={confirmBatchDelete}
                  className={movieStyles.confirmDeleteButton}
                  type="button"
                  disabled={isDeleting}
                >
                  {isDeleting ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

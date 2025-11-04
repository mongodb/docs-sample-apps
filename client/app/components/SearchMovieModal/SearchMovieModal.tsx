'use client';

/**
 * Search Movie Modal Component
 * 
 * Modal for searching movies across multiple fields using MongoDB Search.
 * Supports plot, fullplot, directors, writers, cast fields with search operator options.
 */

import { useState } from 'react';
import styles from './SearchMovieModal.module.css';

interface SearchMovieModalProps {
  onSearch: (searchParams: SearchParams) => void;
  onCancel: () => void;
  isLoading?: boolean;
}

export interface SearchParams {
  plot?: string;
  fullplot?: string;
  directors?: string;
  writers?: string;
  cast?: string;
  limit?: number;
  skip?: number;
  search_operator?: 'must' | 'should' | 'mustNot' | 'filter';
}

interface SearchFormData {
  plot: string;
  fullplot: string;
  directors: string;
  writers: string;
  cast: string;
  limit: string;
  search_operator: 'must' | 'should' | 'mustNot' | 'filter';
}

const getInitialFormData = (): SearchFormData => ({
  plot: '',
  fullplot: '',
  directors: '',
  writers: '',
  cast: '',
  limit: '20',
  search_operator: 'must',
});

export default function SearchMovieModal({ 
  onSearch, 
  onCancel, 
  isLoading = false
}: SearchMovieModalProps) {
  const [formData, setFormData] = useState<SearchFormData>(getInitialFormData());
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    // Check if at least one search field has a value
    const hasSearchInput = formData.plot.trim() || 
                          formData.fullplot.trim() || 
                          formData.directors.trim() || 
                          formData.writers.trim() || 
                          formData.cast.trim();

    if (!hasSearchInput) {
      newErrors.general = 'Please enter search terms in at least one field';
    }

    // Validate limit
    const limitNum = parseInt(formData.limit);
    if (!limitNum || limitNum < 1 || limitNum > 100) {
      newErrors.limit = 'Limit must be between 1 and 100';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    // Build search parameters, only including non-empty fields
    const searchParams: SearchParams = {
      search_operator: formData.search_operator,
      limit: parseInt(formData.limit),
      skip: 0, // Always start from beginning for new search
    };

    if (formData.plot.trim()) {
      searchParams.plot = formData.plot.trim();
    }
    if (formData.fullplot.trim()) {
      searchParams.fullplot = formData.fullplot.trim();
    }
    if (formData.directors.trim()) {
      searchParams.directors = formData.directors.trim();
    }
    if (formData.writers.trim()) {
      searchParams.writers = formData.writers.trim();
    }
    if (formData.cast.trim()) {
      searchParams.cast = formData.cast.trim();
    }

    onSearch(searchParams);
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Clear errors when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
    if (errors.general) {
      setErrors(prev => ({ ...prev, general: '' }));
    }
  };

  const handleClear = () => {
    setFormData(getInitialFormData());
    setErrors({});
  };

  const searchOperatorOptions = [
    { value: 'must', label: 'Must match all fields (AND)', description: 'All specified fields must match' },
    { value: 'should', label: 'Should match any field (OR)', description: 'At least one field should match' },
    { value: 'mustNot', label: 'Must not match', description: 'Results must NOT contain these terms' },
    { value: 'filter', label: 'Filter results', description: 'Filter results by these criteria' },
  ];

  return (
    <div className={styles.formContainer}>
      <h2 className={styles.formTitle}>Search Movies</h2>
      <p className={styles.batchDescription}>
        Search across movie plots, directors, writers, and cast.
      </p>
      
      {errors.general && (
        <div className={styles.generalError}>
          {errors.general}
        </div>
      )}

      <form onSubmit={handleSubmit} className={styles.form}>
        {/* Search Fields */}
        <div className={styles.formGrid}>
          {/* Plot Search */}
          <div className={styles.formGroup}>
            <label htmlFor="plot" className={styles.label}>
              Plot Keywords
            </label>
            <input
              type="text"
              id="plot"
              value={formData.plot}
              onChange={(e) => handleInputChange('plot', e.target.value)}
              className={`${styles.input} ${errors.plot ? styles.inputError : ''}`}
              disabled={isLoading}
                            placeholder="Exact phrase search in plot summaries"
            />
            {errors.plot && <span className={styles.error}>{errors.plot}</span>}
          </div>

          {/* Full Plot Search */}
          <div className={styles.formGroup}>
            <label htmlFor="fullplot" className={styles.label}>
              Full Plot Keywords
            </label>
            <input
              type="text"
              id="fullplot"
              value={formData.fullplot}
              onChange={(e) => handleInputChange('fullplot', e.target.value)}
              className={`${styles.input} ${errors.fullplot ? styles.inputError : ''}`}
              disabled={isLoading}
              placeholder="Search in full plot descriptions"
            />
            {errors.fullplot && <span className={styles.error}>{errors.fullplot}</span>}
          </div>

          {/* Directors Search */}
          <div className={styles.formGroup}>
            <label htmlFor="directors" className={styles.label}>
              Directors
            </label>
            <input
              type="text"
              id="directors"
              value={formData.directors}
              onChange={(e) => handleInputChange('directors', e.target.value)}
              className={`${styles.input} ${errors.directors ? styles.inputError : ''}`}
              disabled={isLoading}
              placeholder="Director names"
            />
            {errors.directors && <span className={styles.error}>{errors.directors}</span>}
          </div>

          {/* Writers Search */}
          <div className={styles.formGroup}>
            <label htmlFor="writers" className={styles.label}>
              Writers
            </label>
            <input
              type="text"
              id="writers"
              value={formData.writers}
              onChange={(e) => handleInputChange('writers', e.target.value)}
              className={`${styles.input} ${errors.writers ? styles.inputError : ''}`}
              disabled={isLoading}
              placeholder="Writer names"
            />
            {errors.writers && <span className={styles.error}>{errors.writers}</span>}
          </div>

          {/* Cast Search */}
          <div className={styles.formGroup}>
            <label htmlFor="cast" className={styles.label}>
              Cast
            </label>
            <input
              type="text"
              id="cast"
              value={formData.cast}
              onChange={(e) => handleInputChange('cast', e.target.value)}
              className={`${styles.input} ${errors.cast ? styles.inputError : ''}`}
              disabled={isLoading}
              placeholder="Actor names"
            />
            {errors.cast && <span className={styles.error}>{errors.cast}</span>}
          </div>

          {/* Limit */}
          <div className={styles.formGroup}>
            <label htmlFor="limit" className={styles.label}>
              Max Results
            </label>
            <input
              type="number"
              id="limit"
              value={formData.limit}
              onChange={(e) => handleInputChange('limit', e.target.value)}
              className={`${styles.input} ${errors.limit ? styles.inputError : ''}`}
              disabled={isLoading}
              min="1"
              max="100"
            />
            {errors.limit && <span className={styles.error}>{errors.limit}</span>}
          </div>
        </div>

        {/* Search Operator */}
        <div className={styles.formGroup}>
          <label htmlFor="search_operator" className={styles.label}>
            Search Logic
          </label>
          <select
            id="search_operator"
            value={formData.search_operator}
            onChange={(e) => handleInputChange('search_operator', e.target.value)}
            className={styles.input}
            disabled={isLoading}
          >
            {searchOperatorOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <small className={styles.searchOperatorDescription}>
            {searchOperatorOptions.find(opt => opt.value === formData.search_operator)?.description}
          </small>
        </div>

        {/* Form Actions */}
        <div className={styles.formActions}>
          <button
            type="button"
            onClick={handleClear}
            className={`${styles.button} ${styles.clearButton}`}
            disabled={isLoading}
          >
            Clear
          </button>
          <button
            type="button"
            onClick={onCancel}
            className={`${styles.button} ${styles.cancelButton}`}
            disabled={isLoading}
          >
            Close
          </button>
          <button
            type="submit"
            className={`${styles.button} ${styles.saveButton}`}
            disabled={isLoading}
          >
            {isLoading ? 'Searching...' : 'Search Movies'}
          </button>
        </div>
      </form>
    </div>
  );
}
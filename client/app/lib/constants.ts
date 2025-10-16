/**
 * Application constants
 */

export const APP_CONFIG = {
  name: 'MFlix',
  description: 'Browse movies from the sample MFlix database',
  defaultMovieLimit: 50,
  imageFormats: ['image/avif', 'image/webp'],
} as const;

export const ROUTES = {
  home: '/',
  movies: '/movies',
  movie: (id: string) => `/movie/${id}`,
} as const;

export const API_ENDPOINTS = {
  movies: '/api/movies',
  movie: (id: string) => `/api/movies/${id}`,
} as const;
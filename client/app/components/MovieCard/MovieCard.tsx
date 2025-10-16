'use client';

import Image from 'next/image';
import movieStyles from "./MovieCard.module.css";
import { MovieCardProps } from "../../types/movie";

/**
 * Movie Card Client Component
 * 
 * This component handles the interactive parts of the movie card,
 * such as image error handling, while the parent remains a Server Component.
 */
export default function MovieCard({ movie }: MovieCardProps) {
  const handleImageError = () => {
    // This will be handled by the Image component's onError prop
    console.warn(`Failed to load poster for: ${movie.title}`);
  };

  return (
    <div className={movieStyles.movieCard}>
      <div className={movieStyles.moviePoster}>
        {movie.poster ? (
          <Image
            src={movie.poster}
            alt={`${movie.title} poster`}
            fill
            sizes="(max-width: 480px) 100vw, (max-width: 768px) 50vw, 280px"
            style={{ objectFit: 'cover' }}
            onError={handleImageError}
            placeholder="blur"
            blurDataURL="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAhEAACAQMDBQAAAAAAAAAAAAABAgMABAUGIWGRkqGx0f/EABUBAQEAAAAAAAAAAAAAAAAAAAMF/8QAGhEAAgIDAAAAAAAAAAAAAAAAAAECEgMRkf/aAAwDAQACEQMRAD8AltJagyeH0AthI5xdrLcNM91BF5pX2HaH9bcfaSXWGaRmknyJckliyjqTzSlT54b6bk+h0R7Dh5zq6esmOk2cWkgaWKJZoSGEa5qKUlPP45++P//Z"
          />
        ) : (
          <div className={movieStyles.posterPlaceholder}>
            No Poster Available
          </div>
        )}
      </div>
      
      <div className={movieStyles.movieInfo}>
        <h3 className={movieStyles.movieTitle}>{movie.title}</h3>
        {movie.year && (
          <p className={movieStyles.movieYear}>({movie.year})</p>
        )}
        {movie.imdb?.rating && (
          <p className={movieStyles.movieRating}>⭐ {movie.imdb.rating}/10</p>
        )}
        {movie.genres && movie.genres.length > 0 && (
          <p className={movieStyles.movieGenres}>
            {movie.genres.slice(0, 3).join(', ')}
          </p>
        )}
      </div>
      
      <button className={movieStyles.detailsButton} type="button">
        Get Details
      </button>
    </div>
  );
}
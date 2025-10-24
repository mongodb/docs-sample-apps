import pageStyles from "./page.module.css";
import movieStyles from "./movies.module.css";
import MovieCard from "../components/MovieCard";
import { fetchMovies } from "../lib/api";
import { APP_CONFIG } from "../lib/constants";

export default async function Movies() {
  const movies = await fetchMovies(APP_CONFIG.defaultMovieLimit);

  return (
    <div className={pageStyles.page}>
      <main className={pageStyles.main}>
        <h1 className={movieStyles.pageTitle}>Movies</h1>
        <p className={movieStyles.movieCount}>Displaying {movies.length} movies from the sample_mflix database</p>
        
        {movies.length === 0 ? (
          <div className={movieStyles.noMovies}>
            <p>No movies found. Make sure the Express server is running on port 3001.</p>
          </div>
        ) : (
          <div className={movieStyles.moviesGrid}>
            {movies.map((movie) => (
              <MovieCard key={movie._id} movie={movie} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

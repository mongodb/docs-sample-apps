import pageStyles from "./page.module.css";
import movieStyles from "./movies.module.css";
import MovieCard from "../components/MovieCard";
import Pagination from "../components/Pagination";
import PageSizeSelector from "../components/PageSizeSelector";
import { fetchMovies } from "../lib/api";
import { APP_CONFIG } from "../lib/constants";

interface MoviesPageProps {
  searchParams: Promise<{
    page?: string;
    limit?: string;
  }>;
}

export default async function Movies({ searchParams }: MoviesPageProps) {
  const params = await searchParams;
  const page = parseInt(params.page || '1');
  const limit = Math.min(
    parseInt(params.limit || APP_CONFIG.defaultMovieLimit.toString()), 
    APP_CONFIG.maxMovieLimit
  );
  const skip = (page - 1) * limit;

  const { movies, hasNextPage, hasPrevPage } = await fetchMovies(limit, skip);

  return (
    <div className={pageStyles.page}>
      <main className={pageStyles.main}>
        <h1 className={movieStyles.pageTitle}>Movies</h1>

        <PageSizeSelector currentLimit={limit} />
        
        {movies.length === 0 ? (
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
      </main>
    </div>
  );
}


import loadingStyles from "./loading.module.css";

/**
 * Loading Component for Movies Page
 * 
 * This component is automatically displayed by Next.js while the movies page
 * is loading during Server Side Rendering or when navigating to the page.
 */
export default function Loading() {
  return (
    <div>
      <main>
        <h1 className={loadingStyles.pageTitle}>Movies</h1>
        <div className={loadingStyles.loadingContainer}>
          <div className={loadingStyles.loadingSpinner}></div>
          <p className={loadingStyles.loadingMessage}>Loading movies from the database...</p>
        </div>
      </main>
    </div>
  );
}
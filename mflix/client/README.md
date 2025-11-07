# MongoDB Sample MFlix Frontend

This is a [Next.js](https://nextjs.org) frontend application that demonstrates MongoDB operations by connecting to an Express.js backend. The application showcases Server Side Rendering (SSR) and displays movie data from the sample_mflix database.

## Features

- **Movies Page** (`/movies`): Displays a paginated grid of movies with Server Side Rendering
- **Pagination**: Navigate through movies with previous/next controls and adjustable page sizes
- **Movie Details Page** (`/movie/[id]`): Shows comprehensive movie information including plot, cast, ratings, and more
- **Movie Cards**: Shows movie title, poster, year, rating, and genres with navigation to details
- **Navigation**: Sticky navigation bar with links to home and movies pages
- **Responsive Design**: Works on desktop and mobile devices
- **Error Handling**: Graceful fallbacks for missing posters, API errors, and not-found movies
- **Loading States**: Professional loading indicators and skeleton screens during data fetching
- **SEO Optimized**: Dynamic metadata generation for movie pages and pagination

## Prerequisites

1. **Express Backend**: The Express server must be running on `http://localhost:3001`
2. **MongoDB Database**: The backend must be connected to a MongoDB instance with the sample_mflix dataset
3. **Node.js**: Version 18 or higher

## Getting Started

### 1. Environment Configuration

Copy the environment example file and configure your API URL:

```bash
cp .env.example .env.local
```

Edit `.env.local` and update the API_URL if needed:
```bash
API_URL=http://localhost:3001
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Start the Development Server

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the application.

### 4. View the Movies

Navigate to [http://localhost:3000/movies](http://localhost:3000/movies) to see the movies grid with data from the MongoDB sample_mflix database. 

**Pagination Features:**
- Use the **Previous/Next** buttons to navigate between pages
- Change the **page size** (10, 20, or 50 movies per page) using the dropdown selector
- URL parameters support direct linking: `?page=2&limit=10`
- Click the "Get Details" button on any movie card to view comprehensive movie information

## API Integration

The application connects to the Express backend using Server Side Rendering:

- **Movies API**: `GET /api/movies` - Fetches movie data with pagination support (`limit` and `skip` parameters)
- **Movie Details API**: `GET /api/movies/:id` - Fetches detailed information for a specific movie
- **Pagination**: Smart pagination that detects available pages by requesting `limit + 1` movies
- **Error Handling**: Graceful fallbacks when the API is unavailable
- **Type Safety**: TypeScript interfaces matching the backend data structure

## Available Scripts

- `npm run dev` - Starts the development server with Turbopack
- `npm run build` - Builds the application for production
- `npm run start` - Starts the production server
- `npm run lint` - Runs ESLint for code quality

## Project Structure

```
client/
├── app/
│   ├── movie/
│   │   └── [id]/
│   │       ├── page.tsx      # Movie details page with SSR
│   │       ├── loading.tsx   # Loading skeleton for details
│   │       ├── error.tsx     # Error boundary for details
│   │       └── not-found.tsx # 404 page for invalid movies
│   ├── movies/
│   │   ├── page.tsx      # Movies page with SSR
│   │   └── loading.tsx   # Loading component
│   ├── components/
│   │   └── MovieCard/    # Reusable movie card component
│   ├── lib/
│   │   ├── api.ts        # API functions
│   │   └── constants.ts  # App constants and routes
│   ├── types/
│   │   └── movie.ts      # TypeScript type definitions
│   ├── layout.tsx        # Root layout with navigation
│   └── page.tsx          # Home page
├── .env.example          # Environment variables template
└── .env.local           # Local environment configuration
```

## Development Notes

### Server Side Rendering

The movies page uses Next.js Server Side Rendering (SSR) to fetch data on the server before sending the page to the client. This provides:

- **Better SEO**: Search engines can index the movie content
- **Faster Initial Load**: Users see content immediately
- **Fresh Data**: Movie data is fetched on each page request

### Type Safety

The application uses TypeScript interfaces that match the backend MongoDB document structure:

```typescript
interface Movie {
  _id: string;
  title: string;
  year?: number;
  poster?: string;
  genres?: string[];
  imdb?: {
    rating?: number;
  };
}
```

### Error Handling

- **API Errors**: Shows fallback message when the backend is unavailable
- **Missing Posters**: Displays placeholder for movies without poster images
- **Network Issues**: Graceful degradation with empty state

## Learn More

To learn more about the technologies used:

- [Next.js Documentation](https://nextjs.org/docs) - Learn about Next.js features and API
- [MongoDB Node.js Driver](https://mongodb.github.io/node-mongodb-native/) - Backend database integration
- [TypeScript](https://www.typescriptlang.org/) - Type safety and development experience

## Deployment

### Development
The application is configured to work with a local Express backend on port 3001.

### Production
For production deployment:

1. Update `API_URL` in your environment configuration
2. Build the application: `npm run build`
3. Start the production server: `npm start`

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

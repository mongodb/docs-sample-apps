# Next.js MFlix Frontend Application

This is a Next.js frontend application that demonstrates a modern movie browsing experience by connecting to a backend API. The application showcases Server Side Rendering (SSR), responsive design, and interactive features while displaying movie data from the MongoDB sample_mflix dataset.

## Project Structure

```
client/
├── README.md
├── package.json
├── next.config.ts
├── tsconfig.json
├── .env.example
└── app/
    ├── layout.tsx          # Root layout with navigation
    ├── page.tsx            # Home page
    ├── globals.css         # Global styles
    ├── movies/             # Movies listing page
    ├── movie/[id]/         # Individual movie details
    ├── aggregations/       # Data aggregations page
    ├── components/         # Reusable UI components
    ├── lib/                # API utilities and constants
    └── types/              # TypeScript type definitions
```

## Prerequisites

- **Backend API Server**: The backend server must be running on `http://localhost:3001`
- **MongoDB Database**: The backend must be connected to a MongoDB instance with the `sample_mflix` dataset loaded
  - [Load sample data](https://www.mongodb.com/docs/atlas/sample-data/)
- **Node.js 18** or higher
- **npm** (included with Node.js)

## Getting Started

### 1. Configure the Environment

Copy the environment example file:

```bash
cp .env.example .env.local
```

Edit `.env.local` and update the API URL if needed:

```env
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

The Next.js application will start on `http://localhost:3000`.

### 4. Access the Application

Open your browser and navigate to:
- **Frontend:** http://localhost:3000
- **Movies Page:** http://localhost:3000/movies
- **Aggregations:** http://localhost:3000/aggregations

## Features

- **Browse Movies:** View a paginated list of movies from the sample_mflix dataset with server-side rendering
- **Movie Details:** Access comprehensive movie information including plot, cast, ratings, and metadata
- **CRUD Operations:** Create, read, update, and delete movies through an intuitive interface
- **Search & Filters:** Search movies with various filters and sorting options
- **Vector Search:** Find movies with similar plot descriptions using AI-powered search
- **Data Aggregations:** View analytics and insights built with MongoDB aggregation pipelines
- **Responsive Design:** Optimized experience across desktop and mobile devices
- **Real-time Updates:** Dynamic content updates without page refreshes

## Development

### Frontend Development

The Next.js frontend uses:
- **React 19** with TypeScript for modern component development
- **Next.js 15** with App Router for server-side rendering and routing
- **Turbopack** for fast development builds and hot reloading
- **CSS Modules** for component-scoped styling
- **ESLint** for code quality and consistency

#### Development Mode

For active development with hot reloading and fast refresh:

```bash
npm run dev
```

This starts the development server on `http://localhost:3000` with Turbopack for fast rebuilds.

#### Production Build

To create an optimized production build:

```bash
npm run build  # Creates optimized production build
npm start      # Starts production server
```

The production build:
- Minifies and optimizes JavaScript and CSS
- Optimizes images and assets  
- Generates static pages where possible
- Provides better performance for end users

#### Code Quality

To check code quality and formatting:

```bash
npm run lint
```

### API Integration

The application connects to the backend API using:
- **Server-Side Rendering**: Data fetched on the server for better SEO and performance
- **TypeScript**: Type-safe API calls with interfaces matching backend data structures
- **Error Handling**: Graceful fallbacks when the API is unavailable
- **Loading States**: Professional loading indicators and skeleton screens

### Key Technologies

- **Next.js SSR**: Server-side rendering for better performance and SEO
- **TypeScript**: Full type safety across the application
- **CSS Modules**: Scoped styling to prevent style conflicts
- **Responsive Design**: Mobile-first approach with flexible layouts

## Issues

If you have problems running the sample app, please check the following:

- [ ] Verify that you have started the backend server on port 3001.
- [ ] Verify that the backend is connected to MongoDB with the sample_mflix dataset.
- [ ] Verify that you have set the correct API_URL in your `.env.local` file.
- [ ] Verify that you have no firewalls blocking access to ports 3000 or 3001.

If you have verified the above and still have issues, please [open an issue](https://github.com/mongodb/docs-sample-apps/issues/new/choose) on the source repository `mongodb/docs-sample-apps`.

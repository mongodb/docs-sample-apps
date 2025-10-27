# MongoDB Sample MFlix Express.js Backend

This Express.js backend demonstrates MongoDB operations using the Node.js driver with TypeScript. It provides a comprehensive API for working with the sample_mflix dataset, showcasing all basic CRUD operations and following best practices for educational purposes.

## Features

### ✅ Implemented CRUD Operations

- **insertOne()** - Create a single movie document
- **insertMany()** - Create multiple movie documents in batch
- **findOne()** - Retrieve a single movie by ID
- **find()** - Retrieve multiple movies with filtering, sorting, and pagination
- **updateOne()** - Update a single movie document
- **updateMany()** - Update multiple movies based on filter criteria
- **deleteOne()** - Delete a single movie document
- **deleteMany()** - Delete multiple movies based on filter criteria
- **findOneAndDelete()** - Find and delete a movie in one atomic operation

### 🔮 Planned Features (Post-MVP)

- **Aggregations** - Reporting by comments, year, and director
- **Full Text Search** - Search index based on movie plots
- **Vector Search** - Find similar movies based on plot content
- **Geospatial Queries** - Find theaters near specific coordinates

## Prerequisites

- Node.js 18+ installed
- MongoDB Atlas cluster with sample_mflix dataset loaded

## Quick Start

### 1. Environment Setup

Copy the environment example file and configure your MongoDB connection:

```bash
cp .env.example .env
```

Edit `.env` file with your MongoDB Atlas connection string:

### 2. Install Dependencies

```bash
npm install
```

### 3. Build and Run

For development with hot reloading:
```bash
npm run dev
```

For production:
```bash
npm run build
npm start
```

## API Documentation

### Base URL

```
http://localhost:3001/api
```

### Movies Endpoints

#### Get All Movies
```http
GET /api/movies
```

**Query Parameters:**
- `q` - Text search query
- `genre` - Filter by genre
- `year` - Filter by year
- `minRating` - Minimum IMDB rating
- `maxRating` - Maximum IMDB rating
- `limit` - Number of results (default: 20, max: 100)
- `skip` - Number of documents to skip (pagination)
- `sortBy` - Field to sort by (default: title)
- `sortOrder` - Sort direction: asc or desc (default: asc)

**Example:**
```bash
curl "http://localhost:3001/api/movies?genre=Action&year=2010&minRating=7&limit=10"
```

#### Get Single Movie
```http
GET /api/movies/:id
```

**Example:**
```bash
curl "http://localhost:3001/api/movies/573a1390f29313caabcd42e8"
```

#### Create Single Movie
```http
POST /api/movies
Content-Type: application/json

{
  "title": "My New Movie",
  "year": 2024,
  "plot": "An amazing story about...",
  "genres": ["Action", "Drama"],
  "directors": ["John Doe"]
}
```

#### Create Multiple Movies
```http
POST /api/movies/batch
Content-Type: application/json

[
  {
    "title": "Movie 1",
    "year": 2024,
    "plot": "First movie plot..."
  },
  {
    "title": "Movie 2", 
    "year": 2024,
    "plot": "Second movie plot..."
  }
]
```

#### Update Single Movie
```http
PUT /api/movies/:id
Content-Type: application/json

{
  "year": 2025,
  "plot": "Updated plot description..."
}
```

#### Update Multiple Movies
```http
PATCH /api/movies
Content-Type: application/json

{
  "filter": { "year": 2020 },
  "update": { "genres": ["Updated Genre"] }
}
```

#### Delete Single Movie
```http
DELETE /api/movies/:id
```

#### Delete Multiple Movies
```http
DELETE /api/movies
Content-Type: application/json

{
  "filter": { "year": { "$lt": 1950 } }
}
```

#### Find and Delete Movie
```http
DELETE /api/movies/:id/find-and-delete
```

Returns the deleted movie document.

## Project Structure

```
src/
├── config/
│   └── database.ts          # MongoDB connection and setup
├── routes/
│   └── movies.ts            # Movie CRUD endpoints
├── types/
│   └── index.ts             # TypeScript type definitions
├── utils/
│   └── errorHandler.ts      # Error handling utilities
└── index.ts                 # Application entry point
```

## Database Verification

The application automatically verifies on startup:

- ✅ MongoDB connection
- ✅ Sample_mflix database exists
- ✅ Required collections (movies, theaters, comments) exist
- ✅ Necessary indexes are created
- ✅ Sample data is present

If any requirements are missing, the application will attempt to create them or provide helpful error messages.

## Error Handling

The API returns consistent error responses:

```json
{
  "success": false,
  "error": {
    "message": "Error description",
    "code": "ERROR_CODE",
    "details": "Additional details (development only)"
  },
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## Success Responses

Successful operations return:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* result data */ },
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

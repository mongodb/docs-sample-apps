# Java Spring Boot MongoDB Sample MFlix Application

This is a full-stack movie browsing application built with Java Spring Boot and Next.js, demonstrating MongoDB operations using the `sample_mflix` dataset. The application showcases CRUD operations, aggregations, and MongoDB Search using Spring Data MongoDB.

## Project Structure

```
├── README-JAVA-SPRING.md
├── client/                 # Next.js frontend (TypeScript)
└── server/java-spring/     # Java Spring Boot backend
    ├── src/
    ├── pom.xml
    ├── .env.example
    └── mvnw
```

## Prerequisites

- **Java 21** or higher
- **Node.js 20** or higher
- **MongoDB Atlas account** with the `sample_mflix` dataset loaded
  - [Load sample data](https://www.mongodb.com/docs/atlas/sample-data/) in your Atlas cluster
- **Maven** (included via Maven Wrapper)

## Getting Started

### 1. Configure the Backend

Navigate to the Java Spring server directory:

```bash
cd server/java-spring
```

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit the `.env` file and set your MongoDB connection string:

```env
# MongoDB Connection
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/?retryWrites=true&w=majority

# Server Configuration
PORT=3001

# CORS Configuration
CORS_ORIGIN=http://localhost:3000
```

**Note:** Replace `username`, `password`, and `cluster` with your actual MongoDB Atlas credentials.

### 2. Start the Backend Server

From the `server/java-spring` directory, run:

```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

The server will start on `http://localhost:3001`. You can verify it's running by visiting:
- API root: http://localhost:3001/
- API documentation (Swagger UI): http://localhost:3001/swagger-ui.html

### 3. Configure and Start the Frontend

Open a new terminal and navigate to the client directory:

```bash
cd client
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The Next.js application will start on `http://localhost:3000`.

### 4. Access the Application

Open your browser and navigate to:
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:3001
- **API Documentation:** http://localhost:3001/swagger-ui.html

## Features

- **Browse Movies:** View a paginated list of movies from the sample_mflix dataset
- **Search:** Full-text search using MongoDB Search
- **Filter:** Filter movies by genre, year, rating, and more
- **Movie Details:** View detailed information about each movie
- **Aggregations:** Complex data aggregations and analytics

## Development

### Backend Development

The Java Spring Boot backend uses:
- **Spring Data MongoDB** for database operations
- **Spring Boot Web** for REST API
- **SpringDoc OpenAPI** for API documentation
- **Maven** for dependency management

To run tests:

```bash
cd server/java-spring
./mvnw test
```

### Frontend Development

The Next.js frontend uses:
- **React 19** with TypeScript
- **Next.js 16** with App Router
- **Turbopack** for fast development builds

To build for production:

```bash
cd client
npm run build
npm start
```

## Issues

If you have problems running the sample app, please check the following:

- [ ] Verify that you have set your MongoDB connection string in the `.env` file.
- [ ] Verify that you have started the Java Spring server.
- [ ] Verify that you have started the Next.js client.
- [ ] Verify that you have no firewalls blocking access to the server or client ports.

If you have verified the above and still have issues, please
[open an issue](https://github.com/mongodb/docs-sample-apps/issues/new/choose)
on the source repository `mongodb/docs-sample-apps`.

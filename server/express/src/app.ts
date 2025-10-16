/**
 * Express.js Backend for MongoDB Sample MFlix Application
 *
 * This application demonstrates MongoDB operations using the Node.js driver
 * with TypeScript. The code prioritizes readability and educational value
 * over performance optimization.
 */

import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import {
  closeDatabaseConnection,
  connectToDatabase,
  verifyRequirements,
} from "./config/database";
import { errorHandler } from "./utils/errorHandler";
import moviesRouter from "./routes/movies";

// Load environment variables from .env file
// This must be called before any other imports that use environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

/**
 * CORS Configuration
 * Allows the frontend to communicate with this Express backend
 * In production, this should be configured to only allow specific origins
 */
app.use(
  cors({
    origin: process.env.CORS_ORIGIN || "http://localhost:3000",
    credentials: true,
  })
);

/**
 * Middleware Configuration
 * Express.json() parses incoming JSON requests and puts the parsed data in req.body
 * The limit is set to handle potentially large movie documents
 */
app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ extended: true, limit: "10mb" }));

/**
 * API Routes
 * All movie-related CRUD operations are handled by the movies router
 */
app.use("/api/movies", moviesRouter);

/**
 * Root Endpoint
 * Provides basic information about the API
 */
app.get("/", (req, res) => {
  res.json({
    name: "MongoDB Sample MFlix API",
    version: "1.0.0",
    description:
      "Express.js backend demonstrating MongoDB operations with the sample_mflix dataset",
    endpoints: {
      movies: "/api/movies",
    },
  });
});

/**
 * Global Error Handler
 * This middleware catches any unhandled errors and returns a consistent error response
 * It should be the last middleware in the chain
 */
app.use(errorHandler);

/**
 * Application Startup Function
 * Handles database connection, requirement verification, and server startup
 */
async function startServer() {
  try {
    console.log("Starting MongoDB Sample MFlix API...");

    // Connect to MongoDB database
    console.log("Connecting to MongoDB...");
    await connectToDatabase();
    console.log("Connected to MongoDB successfully");

    // Verify that all required indexes and sample data exist
    console.log("Verifying requirements (indexes and sample data)...");
    await verifyRequirements();
    console.log("All requirements verified successfully");

    // Start the Express server
    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
      console.log(`API documentation available at http://localhost:${PORT}`);
    });
  } catch (error) {
    console.error("Failed to start server:", error);

    // Exit the process if we can't start properly
    // This ensures the application doesn't run in a broken state
    process.exit(1);
  }
}

/**
 * Graceful Shutdown Handler
 * Ensures the application shuts down cleanly when terminated
 */
process.on("SIGINT", () => {
  console.log("\nReceived SIGINT. Shutting down...");
  closeDatabaseConnection();
  process.exit(0);
});

process.on("SIGTERM", () => {
  console.log("\nReceived SIGTERM. Shutting down...");
  closeDatabaseConnection();
  process.exit(0);
});

// Start the server
startServer();

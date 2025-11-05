/**
 * Movie CRUD Integration Tests
 *
 * These tests verify the full database operations for movie CRUD functionality.
 * Unlike unit tests, these tests connect to a real MongoDB instance.
 *
 * Requirements:
 * - MONGODB_URI environment variable must be set
 * - MongoDB instance must be accessible
 */

import { ObjectId } from "mongodb";
import { getCollection } from "../../src/config/database";
import { describeIntegration } from "./setup";

describeIntegration("Movie CRUD Integration Tests", () => {
  let testMovieIds: ObjectId[] = [];

  beforeAll(async () => {
    // Clean up any orphaned test data from previous failed runs
    // This ensures tests are idempotent
    const moviesCollection = getCollection("movies");
    await moviesCollection.deleteMany({
      $or: [
        { title: { $regex: /^Integration Test Movie/ } },
        { title: { $regex: /^Find By ID Test Movie/ } },
        { title: { $regex: /^Action Movie 202[0-9]/ } },
        { title: { $regex: /^Drama Movie 202[0-9]/ } },
        { title: { $regex: /^Pagination Test Movie/ } },
        { title: { $regex: /^Original Title/ } },
        { title: { $regex: /^Updated Title/ } },
        { title: { $regex: /^Movie [0-9]/ } },
        { title: { $regex: /^Movie to Delete/ } },
        { title: { $regex: /^Delete Test/ } },
        { title: { $regex: /^Find and Delete Test/ } },
      ],
    });
  });

  afterEach(async () => {
    // Clean up test movies after each test
    if (testMovieIds.length > 0) {
      const moviesCollection = getCollection("movies");
      await moviesCollection.deleteMany({
        _id: { $in: testMovieIds },
      });
      testMovieIds = [];
    }
  });

  describe("Create Operations", () => {
    test("should create a single movie with insertOne", async () => {
      const moviesCollection = getCollection("movies");

      const newMovie = {
        title: "Integration Test Movie",
        year: 2024,
        plot: "A movie created during integration testing",
        genres: ["Test", "Drama"],
      };

      const result = await moviesCollection.insertOne(newMovie);

      expect(result.acknowledged).toBe(true);
      expect(result.insertedId).toBeDefined();
      expect(result.insertedId).toBeInstanceOf(ObjectId);

      testMovieIds.push(result.insertedId);

      // Verify the movie was actually inserted
      const insertedMovie = await moviesCollection.findOne({
        _id: result.insertedId,
      });

      expect(insertedMovie).toBeDefined();
      expect(insertedMovie?.title).toBe(newMovie.title);
      expect(insertedMovie?.year).toBe(newMovie.year);
      expect(insertedMovie?.plot).toBe(newMovie.plot);
    });

    test("should create multiple movies with insertMany", async () => {
      const moviesCollection = getCollection("movies");

      const newMovies = [
        {
          title: "Integration Test Movie 1",
          year: 2024,
          plot: "First test movie",
          genres: ["Test"],
        },
        {
          title: "Integration Test Movie 2",
          year: 2024,
          plot: "Second test movie",
          genres: ["Test"],
        },
        {
          title: "Integration Test Movie 3",
          year: 2024,
          plot: "Third test movie",
          genres: ["Test"],
        },
      ];

      const result = await moviesCollection.insertMany(newMovies);

      expect(result.acknowledged).toBe(true);
      expect(result.insertedCount).toBe(3);
      expect(Object.keys(result.insertedIds).length).toBe(3);

      testMovieIds.push(...Object.values(result.insertedIds));

      // Verify all movies were inserted
      const insertedMovies = await moviesCollection
        .find({ _id: { $in: Object.values(result.insertedIds) } })
        .toArray();

      expect(insertedMovies.length).toBe(3);
    });
  });

  describe("Read Operations", () => {
    test("should find a movie by ObjectId", async () => {
      const moviesCollection = getCollection("movies");

      // Create a test movie
      const newMovie = {
        title: "Find By ID Test Movie",
        year: 2024,
        plot: "Testing findOne by ID",
      };

      const insertResult = await moviesCollection.insertOne(newMovie);
      testMovieIds.push(insertResult.insertedId);

      // Find the movie by ID
      const foundMovie = await moviesCollection.findOne({
        _id: insertResult.insertedId,
      });

      expect(foundMovie).toBeDefined();
      expect(foundMovie?._id.toString()).toBe(insertResult.insertedId.toString());
      expect(foundMovie?.title).toBe(newMovie.title);
    });

    test("should find movies with filters", async () => {
      const moviesCollection = getCollection("movies");

      // Create test movies
      const testMovies = [
        {
          title: "Action Movie 2024",
          year: 2024,
          genres: ["Action"],
        },
        {
          title: "Drama Movie 2024",
          year: 2024,
          genres: ["Drama"],
        },
        {
          title: "Action Movie 2023",
          year: 2023,
          genres: ["Action"],
        },
      ];

      const insertResult = await moviesCollection.insertMany(testMovies);
      testMovieIds.push(...Object.values(insertResult.insertedIds));

      // Find movies by year
      const movies2024 = await moviesCollection
        .find({ year: 2024 })
        .toArray();

      const testMovies2024 = movies2024.filter((m) =>
        testMovieIds.some((id) => id.toString() === m._id.toString())
      );

      expect(testMovies2024.length).toBe(2);

      // Find movies by genre
      const actionMovies = await moviesCollection
        .find({ genres: { $regex: /Action/i } })
        .toArray();

      const testActionMovies = actionMovies.filter((m) =>
        testMovieIds.some((id) => id.toString() === m._id.toString())
      );

      expect(testActionMovies.length).toBe(2);
    });

    test("should support pagination with limit and skip", async () => {
      const moviesCollection = getCollection("movies");

      // Create 5 test movies
      const testMovies = Array.from({ length: 5 }, (_, i) => ({
        title: `Pagination Test Movie ${i + 1}`,
        year: 2024,
      }));

      const insertResult = await moviesCollection.insertMany(testMovies);
      testMovieIds.push(...Object.values(insertResult.insertedIds));

      // Get first page (2 movies)
      const firstPage = await moviesCollection
        .find({ _id: { $in: testMovieIds } })
        .limit(2)
        .toArray();

      expect(firstPage.length).toBe(2);

      // Get second page (2 movies, skip first 2)
      const secondPage = await moviesCollection
        .find({ _id: { $in: testMovieIds } })
        .skip(2)
        .limit(2)
        .toArray();

      expect(secondPage.length).toBe(2);

      // Verify different results
      const firstPageIds = firstPage.map((m) => m._id.toString());
      const secondPageIds = secondPage.map((m) => m._id.toString());
      const hasOverlap = firstPageIds.some((id) => secondPageIds.includes(id));

      expect(hasOverlap).toBe(false);
    });
  });

  describe("Update Operations", () => {
    test("should update a single movie with updateOne", async () => {
      const moviesCollection = getCollection("movies");

      // Create a test movie
      const newMovie = {
        title: "Original Title",
        year: 2024,
        plot: "Original plot",
      };

      const insertResult = await moviesCollection.insertOne(newMovie);
      testMovieIds.push(insertResult.insertedId);

      // Update the movie
      const updateResult = await moviesCollection.updateOne(
        { _id: insertResult.insertedId },
        { $set: { title: "Updated Title", plot: "Updated plot" } }
      );

      expect(updateResult.acknowledged).toBe(true);
      expect(updateResult.matchedCount).toBe(1);
      expect(updateResult.modifiedCount).toBe(1);

      // Verify the update
      const updatedMovie = await moviesCollection.findOne({
        _id: insertResult.insertedId,
      });

      expect(updatedMovie?.title).toBe("Updated Title");
      expect(updatedMovie?.plot).toBe("Updated plot");
      expect(updatedMovie?.year).toBe(2024); // Unchanged field
    });

    test("should update multiple movies with updateMany", async () => {
      const moviesCollection = getCollection("movies");

      // Create test movies
      const testMovies = [
        { title: "Movie 1", year: 2024, rated: "PG" },
        { title: "Movie 2", year: 2024, rated: "PG" },
        { title: "Movie 3", year: 2024, rated: "R" },
      ];

      const insertResult = await moviesCollection.insertMany(testMovies);
      testMovieIds.push(...Object.values(insertResult.insertedIds));

      // Update all PG movies
      const updateResult = await moviesCollection.updateMany(
        { _id: { $in: testMovieIds }, rated: "PG" },
        { $set: { rated: "PG-13" } }
      );

      expect(updateResult.acknowledged).toBe(true);
      expect(updateResult.matchedCount).toBe(2);
      expect(updateResult.modifiedCount).toBe(2);

      // Verify the updates
      const updatedMovies = await moviesCollection
        .find({ _id: { $in: testMovieIds }, rated: "PG-13" })
        .toArray();

      expect(updatedMovies.length).toBe(2);
    });
  });

  describe("Delete Operations", () => {
    test("should delete a single movie with deleteOne", async () => {
      const moviesCollection = getCollection("movies");

      // Create a test movie
      const newMovie = {
        title: "Movie to Delete",
        year: 2024,
      };

      const insertResult = await moviesCollection.insertOne(newMovie);
      const movieId = insertResult.insertedId;

      // Track the ID in case the test fails before deletion
      testMovieIds.push(movieId);

      // Delete the movie
      const deleteResult = await moviesCollection.deleteOne({
        _id: movieId,
      });

      expect(deleteResult.acknowledged).toBe(true);
      expect(deleteResult.deletedCount).toBe(1);

      // Verify deletion
      const deletedMovie = await moviesCollection.findOne({ _id: movieId });
      expect(deletedMovie).toBeNull();

      // Remove from tracking since it's successfully deleted
      testMovieIds = testMovieIds.filter(id => id.toString() !== movieId.toString());
    });

    test("should delete multiple movies with deleteMany", async () => {
      const moviesCollection = getCollection("movies");

      // Create test movies
      const testMovies = [
        { title: "Delete Test 1", year: 2024 },
        { title: "Delete Test 2", year: 2024 },
        { title: "Delete Test 3", year: 2024 },
      ];

      const insertResult = await moviesCollection.insertMany(testMovies);
      const movieIds = Object.values(insertResult.insertedIds);

      // Track the IDs in case the test fails before deletion
      testMovieIds.push(...movieIds);

      // Delete all test movies
      const deleteResult = await moviesCollection.deleteMany({
        _id: { $in: movieIds },
      });

      expect(deleteResult.acknowledged).toBe(true);
      expect(deleteResult.deletedCount).toBe(3);

      // Verify deletion
      const remainingMovies = await moviesCollection
        .find({ _id: { $in: movieIds } })
        .toArray();

      expect(remainingMovies.length).toBe(0);

      // Remove from tracking since they're successfully deleted
      testMovieIds = testMovieIds.filter(
        id => !movieIds.some(deletedId => deletedId.toString() === id.toString())
      );
    });

    test("should find and delete a movie with findOneAndDelete", async () => {
      const moviesCollection = getCollection("movies");

      // Create a test movie
      const newMovie = {
        title: "Find and Delete Test",
        year: 2024,
        plot: "This movie will be found and deleted",
      };

      const insertResult = await moviesCollection.insertOne(newMovie);
      const movieId = insertResult.insertedId;

      // Track the ID in case the test fails before deletion
      testMovieIds.push(movieId);

      // Find and delete the movie
      const result = await moviesCollection.findOneAndDelete({
        _id: movieId,
      });

      expect(result).toBeDefined();
      expect(result?._id.toString()).toBe(movieId.toString());
      expect(result?.title).toBe(newMovie.title);

      // Verify deletion
      const deletedMovie = await moviesCollection.findOne({ _id: movieId });
      expect(deletedMovie).toBeNull();

      // Remove from tracking since it's successfully deleted
      testMovieIds = testMovieIds.filter(id => id.toString() !== movieId.toString());
    });
  });
});


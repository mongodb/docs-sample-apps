/**
 * MongoDB Search Integration Tests
 *
 * These tests verify MongoDB Search functionality with a real MongoDB instance.
 * The tests require:
 * - A MongoDB instance with Search enabled (local MongoDB or Atlas)
 * - MONGODB_URI environment variable
 * - ENABLE_SEARCH_TESTS=true environment variable to enable tests
 *
 * Note: These tests are disabled by default and should only be run against a test MongoDB instance.
 */

import { ObjectId } from "mongodb";
import { connectToDatabase, getCollection } from "../../src/config/database";
import { describeSearch } from "./setup";

const SEARCH_INDEX_NAME = "movieSearchIndex";
const MAX_INDEX_WAIT_SECONDS = 120;
const POLL_INTERVAL_SECONDS = 5;

describeSearch("MongoDB Search Integration Tests", () => {
  let testMovieIds: ObjectId[] = [];

  beforeAll(async () => {
    try {
      // Clean up any leftover test data from previous failed runs
      await cleanupTestMovies();

      // Create test data
      await createTestMovies();

      // Create Search index (or verify it exists)
      await createSearchIndex();

      // Wait for index to be ready
      await waitForSearchIndexReady();

      // Wait for documents to be indexed
      await new Promise((resolve) => setTimeout(resolve, 10000)); // 10 seconds
    } catch (error) {
      console.error("❌ Error during setup:", error);
      // Clean up on setup failure
      await cleanupTestMovies();
      throw error;
    }
  });

  afterAll(async () => {
    await cleanupTestMovies();
  });

  describe("Search by plot", () => {
    test("should find movies with 'space adventure' in plot", async () => {
      const moviesCollection = getCollection("movies");

      // Perform Search query
      const results = await moviesCollection
        .aggregate([
          {
            $search: {
              index: SEARCH_INDEX_NAME,
              text: {
                query: "space adventure",
                path: ["plot", "fullplot"],
              },
            },
          },
          { $limit: 10 },
        ])
        .toArray();

      expect(results).toBeDefined();
      expect(results.length).toBeGreaterThan(0);

      // Verify at least one result contains our test movie
      const foundTestMovie = results.some(
        (movie: any) =>
          movie.plot && movie.plot.toLowerCase().includes("space adventure")
      );
      expect(foundTestMovie).toBe(true);
    });

    test("should return empty array when no movies match search query", async () => {
      const moviesCollection = getCollection("movies");

      // Search for something that definitely doesn't exist
      const results = await moviesCollection
        .aggregate([
          {
            $search: {
              index: SEARCH_INDEX_NAME,
              text: {
                query: "xyzabc123nonexistent",
                path: ["plot", "fullplot"],
              },
            },
          },
          { $limit: 10 },
        ])
        .toArray();

      expect(results).toBeDefined();
      expect(results.length).toBe(0);
    });
  });

  describe("Search with pagination", () => {
    test("should respect limit parameter", async () => {
      const moviesCollection = getCollection("movies");
      const limit = 2;

      const results = await moviesCollection
        .aggregate([
          {
            $search: {
              index: SEARCH_INDEX_NAME,
              text: {
                query: "adventure",
                path: ["plot", "fullplot"],
              },
            },
          },
          { $limit: limit },
        ])
        .toArray();

      expect(results).toBeDefined();
      expect(results.length).toBeLessThanOrEqual(limit);
    });

    test("should support pagination with skip", async () => {
      const moviesCollection = getCollection("movies");
      const limit = 2;

      // Get first page
      const firstPage = await moviesCollection
        .aggregate([
          {
            $search: {
              index: SEARCH_INDEX_NAME,
              text: {
                query: "adventure",
                path: ["plot", "fullplot"],
              },
            },
          },
          { $limit: limit },
        ])
        .toArray();

      // Get second page
      const secondPage = await moviesCollection
        .aggregate([
          {
            $search: {
              index: SEARCH_INDEX_NAME,
              text: {
                query: "adventure",
                path: ["plot", "fullplot"],
              },
            },
          },
          { $skip: limit },
          { $limit: limit },
        ])
        .toArray();

      expect(firstPage).toBeDefined();
      expect(secondPage).toBeDefined();

      // If we have enough results, verify different pages have different results
      if (firstPage.length === limit && secondPage.length > 0) {
        const firstPageIds = firstPage.map((m: any) => m._id.toString());
        const secondPageIds = secondPage.map((m: any) => m._id.toString());

        // Verify no overlap between pages
        const hasOverlap = firstPageIds.some((id) => secondPageIds.includes(id));
        expect(hasOverlap).toBe(false);
      }
    });
  });

  // ==================== HELPER FUNCTIONS ====================

  /**
   * Clean up test movies from the database.
   * This function is idempotent and safe to call multiple times.
   */
  async function cleanupTestMovies(): Promise<void> {
    const moviesCollection = getCollection("movies");

    // Clean up by IDs if we have them
    if (testMovieIds.length > 0) {
      await moviesCollection.deleteMany({
        _id: { $in: testMovieIds },
      });
      testMovieIds = [];
    }

    // Also clean up by title pattern to catch any orphaned test data
    // This ensures idempotency even if previous test runs failed
    await moviesCollection.deleteMany({
      title: { $regex: /^Test (Space Adventure|Mystery Movie|Adventure Quest)$/ },
    });
  }

  async function createTestMovies(): Promise<void> {
    const moviesCollection = getCollection("movies");

    const testMovies = [
      {
        title: "Test Space Adventure",
        year: 2024,
        plot: "An epic space adventure across the galaxy",
        genres: ["Sci-Fi", "Adventure"],
      },
      {
        title: "Test Mystery Movie",
        year: 2024,
        plot: "A detective solves a mysterious crime",
        genres: ["Mystery", "Thriller"],
      },
      {
        title: "Test Adventure Quest",
        year: 2024,
        plot: "Heroes embark on a dangerous adventure",
        genres: ["Adventure", "Fantasy"],
      },
    ];

    const result = await moviesCollection.insertMany(testMovies);
    testMovieIds = Object.values(result.insertedIds) as ObjectId[];
  }

  async function createSearchIndex(): Promise<void> {
    const db = await connectToDatabase();
    const moviesCollection = getCollection("movies");

    // Check if index already exists
    const existingIndexes = await moviesCollection.listSearchIndexes().toArray();
    const indexExists = existingIndexes.some(
      (idx: any) => idx.name === SEARCH_INDEX_NAME
    );

    if (indexExists) {
      return;
    }

    // Create the search index definition
    const indexDefinition = {
      mappings: {
        dynamic: false,
        fields: {
          plot: {
            type: "string",
            analyzer: "lucene.standard",
          },
          fullplot: {
            type: "string",
            analyzer: "lucene.standard",
          },
          directors: {
            type: "string",
            analyzer: "lucene.standard",
          },
          writers: {
            type: "string",
            analyzer: "lucene.standard",
          },
          cast: {
            type: "string",
            analyzer: "lucene.standard",
          },
        },
      },
    };

    // Create the index using the createSearchIndexes command
    try {
      await db.command({
        createSearchIndexes: "movies",
        indexes: [
          {
            name: SEARCH_INDEX_NAME,
            definition: indexDefinition,
          },
        ],
      });
    } catch (error) {
      console.error("❌ Error creating search index:", error);
      throw error;
    }
  }

  async function waitForSearchIndexReady(): Promise<void> {
    const moviesCollection = getCollection("movies");
    const startTime = Date.now();
    const maxWaitMillis = MAX_INDEX_WAIT_SECONDS * 1000;

    while (Date.now() - startTime < maxWaitMillis) {
      const indexes = await moviesCollection.listSearchIndexes().toArray();

      const searchIndex = indexes.find(
        (idx: any) => idx.name === SEARCH_INDEX_NAME
      );

      if (searchIndex) {
        const status = (searchIndex as any).status;
        if (status === "READY") {
          return;
        }
      }

      // Wait before polling again
      await new Promise((resolve) =>
        setTimeout(resolve, POLL_INTERVAL_SECONDS * 1000)
      );
    }

    throw new Error(
      `Search index did not become ready within ${MAX_INDEX_WAIT_SECONDS} seconds`
    );
  }
});


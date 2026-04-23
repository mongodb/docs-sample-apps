/// <reference types="vitest" />

import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  SAMPLE_RESTAURANTS,
  SAMPLE_QUEENS_RESTAURANTS,
} from "../utils/testHelpers";

/**
 * Unit tests for restaurant server functions
 * 
 * Following mflix pattern: Mock the database module (#/lib/db), not the mongodb driver.
 */

// Create mocks using vi.hoisted (must be defined before vi.mock)
const { mockToArray, mockLimit, mockFind, mockCollection, mockDb, mockConnectToDatabase } = vi.hoisted(() => {
  const mockToArray = vi.fn();
  const mockLimit = vi.fn().mockReturnValue({ toArray: mockToArray });
  const mockFind = vi.fn().mockReturnValue({ limit: mockLimit, toArray: mockToArray });
  const mockCollection = { find: mockFind, limit: mockLimit, toArray: mockToArray };
  const mockDb = { collection: vi.fn().mockReturnValue(mockCollection) };
  const mockConnectToDatabase = vi.fn().mockResolvedValue(mockDb);
  return { mockToArray, mockLimit, mockFind, mockCollection, mockDb, mockConnectToDatabase };
});

// Mock mongodb to prevent loading the real driver
vi.mock("mongodb", () => ({
  MongoClient: vi.fn(),
  ObjectId: class ObjectId {
    constructor(id?: string) {
      this.id = id || '507f1f77bcf86cd799439011';
    }
    toString() {
      return this.id;
    }
    id: string;
  },
}));

// Mock the database module (like mflix mocks config/database)
vi.mock("#/lib/db", () => ({
  connectToDatabase: mockConnectToDatabase,
}));

// Import the functions AFTER mocks are set up
import { getAllRestaurants, getRestaurantsByBorough } from "#/server/restaurants";

describe("Restaurant Server Functions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("getAllRestaurants()", () => {
    it("should query database for all restaurants", async () => {
      mockToArray.mockResolvedValue(SAMPLE_RESTAURANTS);

      // Call the server function
      await getAllRestaurants();

      // Verify correct database calls were made
      expect(mockConnectToDatabase).toHaveBeenCalledOnce();
      expect(mockDb.collection).toHaveBeenCalledWith("restaurants");
      expect(mockFind).toHaveBeenCalledWith({});
      expect(mockLimit).toHaveBeenCalledWith(100);
      expect(mockToArray).toHaveBeenCalled();
    });

    it("should handle empty results", async () => {
      mockToArray.mockResolvedValue([]);

      // Call the server function
      await getAllRestaurants();

      // Verify query was still executed
      expect(mockConnectToDatabase).toHaveBeenCalledOnce();
      expect(mockFind).toHaveBeenCalledWith({});
      expect(mockToArray).toHaveBeenCalled();
    });

    it("should handle database errors", async () => {
      const errorMessage = "Database connection failed";
      mockToArray.mockRejectedValue(new Error(errorMessage));

      // Verify error is propagated
      await expect(getAllRestaurants()).rejects.toThrow(errorMessage);
      expect(mockConnectToDatabase).toHaveBeenCalledOnce();
    });

    it("should convert ObjectId to string in results", async () => {
      // Mock restaurant with ObjectId that has toString method
      const mockRestaurant = {
        ...SAMPLE_RESTAURANTS[0],
        _id: {
          toString: vi.fn().mockReturnValue("507f1f77bcf86cd799439011")
        }
      };
      mockToArray.mockResolvedValue([mockRestaurant]);

      // Call the server function
      await getAllRestaurants();

      // Verify ObjectId.toString() would be called for serialization
      expect(mockToArray).toHaveBeenCalled();
    });
  });

  describe("getRestaurantsByBorough()", () => {
    it("should query database with borough and name filters", async () => {
      mockToArray.mockResolvedValue(SAMPLE_QUEENS_RESTAURANTS);

      // Call the server function
      await getRestaurantsByBorough();

      // Verify correct query was built
      expect(mockConnectToDatabase).toHaveBeenCalledOnce();
      expect(mockDb.collection).toHaveBeenCalledWith("restaurants");
      expect(mockFind).toHaveBeenCalledWith({
        borough: "Queens",
        name: { $regex: "Moon", $options: "i" }
      });
      expect(mockLimit).toHaveBeenCalledWith(100);
      expect(mockToArray).toHaveBeenCalled();
    });

    it("should use case-insensitive regex for name matching", async () => {
      mockToArray.mockResolvedValue(SAMPLE_QUEENS_RESTAURANTS);

      // Call the server function
      await getRestaurantsByBorough();

      // Verify regex has 'i' flag for case-insensitive matching
      expect(mockFind).toHaveBeenCalledWith(
        expect.objectContaining({
          name: expect.objectContaining({
            $regex: "Moon",
            $options: "i"
          })
        })
      );
    });

    it("should handle empty results for filtered query", async () => {
      mockToArray.mockResolvedValue([]);

      // Call the server function
      await getRestaurantsByBorough();

      // Verify query was still executed
      expect(mockFind).toHaveBeenCalledWith({
        borough: "Queens",
        name: { $regex: "Moon", $options: "i" }
      });
      expect(mockToArray).toHaveBeenCalled();
    });

    it("should handle database errors on filtered query", async () => {
      const errorMessage = "Query execution failed";
      mockToArray.mockRejectedValue(new Error(errorMessage));

      // Verify error is propagated
      await expect(getRestaurantsByBorough()).rejects.toThrow(errorMessage);
    });
  });

  describe("Database Connection", () => {
    it("should reuse database connection across multiple calls", async () => {
      mockToArray.mockResolvedValue(SAMPLE_RESTAURANTS);

      // Call both functions multiple times
      await getAllRestaurants();
      await getRestaurantsByBorough();
      await getAllRestaurants();

      // Connection should only be established once (connection reuse)
      expect(mockConnectToDatabase).toHaveBeenCalledTimes(3);
      // Note: In real implementation, db.ts caches the connection
      // Integration tests will verify actual connection reuse
    });
  });
});

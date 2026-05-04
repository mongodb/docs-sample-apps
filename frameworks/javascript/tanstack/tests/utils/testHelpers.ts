/**
 * Test Utilities and Helpers
 * 
 * This file contains common utilities, test data, and helper functions
 * used across unit and integration tests for the TanStack restaurants app.
 */

import { ObjectId } from "mongodb";
import type { Restaurant } from "../../app/src/server/restaurants";

// ==================== TEST CONSTANTS ====================

export const TEST_OBJECT_IDS = {
  VALID: "507f1f77bcf86cd799439011",
  VALID_2: "507f1f77bcf86cd799439012",
  VALID_3: "507f1f77bcf86cd799439013",
  INVALID: "invalid-id",
};

export const TEST_RESTAURANT_PREFIX = "Integration Test Restaurant";

// ==================== SAMPLE RESTAURANT DATA ====================

export const SAMPLE_RESTAURANT: Restaurant = {
  _id: TEST_OBJECT_IDS.VALID,
  address: {
    building: "123",
    coord: [-73.9654, 40.7829],
    street: "Main Street",
    zipcode: "11101",
  },
  borough: "Queens",
  cuisine: "American",
  name: "Test Moon Restaurant",
  restaurant_id: "40356151",
};

export const SAMPLE_RESTAURANTS: Restaurant[] = [
  {
    _id: TEST_OBJECT_IDS.VALID,
    address: {
      building: "123",
      coord: [-73.9654, 40.7829],
      street: "Main Street",
      zipcode: "11101",
    },
    borough: "Queens",
    cuisine: "American",
    name: "Test Moon Restaurant",
    restaurant_id: "40356151",
  },
  {
    _id: TEST_OBJECT_IDS.VALID_2,
    address: {
      building: "456",
      coord: [-73.9876, 40.7543],
      street: "Broadway",
      zipcode: "10001",
    },
    borough: "Manhattan",
    cuisine: "Italian",
    name: "Test Pizza Place",
    restaurant_id: "40356152",
  },
  {
    _id: TEST_OBJECT_IDS.VALID_3,
    address: {
      building: "789",
      coord: [-73.9512, 40.7912],
      street: "Astoria Blvd",
      zipcode: "11102",
    },
    borough: "Queens",
    cuisine: "Chinese",
    name: "Blue Moon Diner",
    restaurant_id: "40356153",
  },
];

export const SAMPLE_QUEENS_RESTAURANTS: Restaurant[] = SAMPLE_RESTAURANTS.filter(
  (r) => r.borough === "Queens"
);

export const SAMPLE_MOON_RESTAURANTS: Restaurant[] = SAMPLE_RESTAURANTS.filter(
  (r) => r.name.toLowerCase().includes("moon")
);

// ==================== MOCK DATABASE RESPONSES ====================

export const SAMPLE_DB_RESPONSES = {
  INSERT_ONE: {
    acknowledged: true,
    insertedId: new ObjectId(TEST_OBJECT_IDS.VALID),
  },
  INSERT_MANY: {
    acknowledged: true,
    insertedCount: 3,
    insertedIds: {
      0: new ObjectId(TEST_OBJECT_IDS.VALID),
      1: new ObjectId(TEST_OBJECT_IDS.VALID_2),
      2: new ObjectId(TEST_OBJECT_IDS.VALID_3),
    },
  },
  DELETE_ONE: {
    acknowledged: true,
    deletedCount: 1,
  },
  DELETE_MANY: {
    acknowledged: true,
    deletedCount: 3,
  },
};

// ==================== MOCK HELPER FUNCTIONS (for unit tests) ====================

/**
 * Creates a mock MongoDB collection with chainable methods
 * Used for unit tests with vi.mock()
 */
export function createMockCollection(returnData: any[] = SAMPLE_RESTAURANTS) {
  const mockToArray = vi.fn().mockResolvedValue(returnData);
  const mockLimit = vi.fn().mockReturnValue({ toArray: mockToArray });
  const mockFind = vi.fn().mockReturnValue({ limit: mockLimit, toArray: mockToArray });

  return {
    find: mockFind,
    limit: mockLimit,
    toArray: mockToArray,
    insertOne: vi.fn().mockResolvedValue(SAMPLE_DB_RESPONSES.INSERT_ONE),
    insertMany: vi.fn().mockResolvedValue(SAMPLE_DB_RESPONSES.INSERT_MANY),
    deleteOne: vi.fn().mockResolvedValue(SAMPLE_DB_RESPONSES.DELETE_ONE),
    deleteMany: vi.fn().mockResolvedValue(SAMPLE_DB_RESPONSES.DELETE_MANY),
  };
}

/**
 * Creates a mock database instance
 * Used for unit tests with vi.mock()
 */
export function createMockDatabase(collectionMock = createMockCollection()) {
  return {
    collection: vi.fn().mockReturnValue(collectionMock),
  };
}

// ==================== INTEGRATION TEST HELPERS ====================

/**
 * Creates a test restaurant with unique identifiers
 * Used for integration tests to create temporary test data
 *
 * @param overrides - Optional fields to override default values
 * @returns Restaurant object ready for insertion (without _id)
 */
export function createTestRestaurant(
  overrides: Partial<Omit<Restaurant, "_id">> = {}
): Omit<Restaurant, "_id"> {
  const timestamp = Date.now();
  const randomSuffix = Math.random().toString(36).substring(7);

  return {
    address: {
      building: "999",
      coord: [-73.9999, 40.9999],
      street: "Test Street",
      zipcode: "99999",
    },
    borough: "Manhattan",
    cuisine: "Test Cuisine",
    name: `${TEST_RESTAURANT_PREFIX} ${timestamp}-${randomSuffix}`,
    restaurant_id: `test-${timestamp}-${randomSuffix}`,
    ...overrides,
  };
}

/**
 * Creates multiple test restaurants
 *
 * @param count - Number of restaurants to create
 * @param baseOverrides - Base overrides applied to all restaurants
 * @returns Array of restaurant objects
 */
export function createTestRestaurants(
  count: number,
  baseOverrides: Partial<Omit<Restaurant, "_id">> = {}
): Omit<Restaurant, "_id">[] {
  return Array.from({ length: count }, (_, i) =>
    createTestRestaurant({
      ...baseOverrides,
      name: `${TEST_RESTAURANT_PREFIX} ${i + 1} - ${Date.now()}`,
    })
  );
}

/**
 * Gets a regex pattern to match all integration test restaurants
 * Used for cleanup operations
 */
export function getTestRestaurantPattern() {
  return new RegExp(`^${TEST_RESTAURANT_PREFIX}`);
}

// ==================== TEST ASSERTION HELPERS ====================

/**
 * Asserts that a restaurant object matches expected structure
 *
 * @param restaurant - Restaurant object to validate
 */
export function expectRestaurantMatch(restaurant: any) {
  expect(restaurant).toBeDefined();
  expect(restaurant._id).toBeDefined();
  expect(restaurant.name).toBeDefined();
  expect(restaurant.borough).toBeDefined();
  expect(restaurant.cuisine).toBeDefined();
  expect(restaurant.address).toBeDefined();
  expect(restaurant.address.building).toBeDefined();
  expect(restaurant.address.street).toBeDefined();
  expect(restaurant.address.zipcode).toBeDefined();
  expect(restaurant.address.coord).toBeDefined();
  expect(Array.isArray(restaurant.address.coord)).toBe(true);
  expect(restaurant.address.coord).toHaveLength(2);
  expect(restaurant.restaurant_id).toBeDefined();
}

/**
 * Asserts that an array contains only Queens restaurants
 *
 * @param restaurants - Array of restaurants to check
 */
export function expectAllQueensRestaurants(restaurants: Restaurant[]) {
  expect(Array.isArray(restaurants)).toBe(true);
  expect(restaurants.length).toBeGreaterThan(0);

  const allFromQueens = restaurants.every((r) => r.borough === "Queens");
  expect(allFromQueens).toBe(true);
}

/**
 * Asserts that an array contains only restaurants with "Moon" in the name
 *
 * @param restaurants - Array of restaurants to check
 */
export function expectAllMoonRestaurants(restaurants: Restaurant[]) {
  expect(Array.isArray(restaurants)).toBe(true);
  expect(restaurants.length).toBeGreaterThan(0);

  const allHaveMoon = restaurants.every((r) =>
    r.name.toLowerCase().includes("moon")
  );
  expect(allHaveMoon).toBe(true);
}

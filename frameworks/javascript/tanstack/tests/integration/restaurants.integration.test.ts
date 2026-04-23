/// <reference types="vitest" />

import { describe, it, expect, beforeAll, afterEach } from 'vitest';
import { connectToDatabase } from '#/lib/db';
import { describeIntegration } from './setup';

/**
 * Restaurant API Integration Tests
 *
 * These tests verify the full functionality of restaurant database queries
 * with a real MongoDB connection (no mocks).
 *
 * Following mflix pattern: test database operations directly, not through HTTP endpoints.
 *
 * Requirements:
 * - MONGODB_URI environment variable must be set
 * - MongoDB instance must be accessible
 * - sample_restaurants database with restaurants collection
 */

describeIntegration('Restaurant Database Operations Integration Tests', () => {
  let testRestaurantIds: string[] = [];

  beforeAll(async () => {
    // Clean up any orphaned test data from previous failed runs
    // This ensures tests are idempotent
    const db = await connectToDatabase();
    const restaurantsCollection = db.collection('restaurants');
    
    await restaurantsCollection.deleteMany({
      $or: [
        { name: { $regex: /^Integration Test Restaurant/ } },
        { name: { $regex: /^Test Restaurant/ } },
      ],
    });
  });

  afterEach(async () => {
    // Clean up test data after each test
    if (testRestaurantIds.length > 0) {
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');
      
      await restaurantsCollection.deleteMany({
        restaurant_id: { $in: testRestaurantIds },
      });
      
      testRestaurantIds = [];
    }
  });

  describe('Get All Restaurants Query', () => {
    it('should retrieve restaurants from database', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act - Execute the same query as getAllRestaurants()
      const result = await restaurantsCollection
        .find({})
        .limit(100)
        .toArray();

      // Assert - verify we got real data
      expect(result).toBeDefined();
      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBeGreaterThan(0);
      expect(result.length).toBeLessThanOrEqual(100);

      // Verify structure
      const restaurant = result[0];
      expect(restaurant).toHaveProperty('_id');
      expect(restaurant).toHaveProperty('name');
      expect(restaurant).toHaveProperty('borough');
      expect(restaurant).toHaveProperty('cuisine');
      expect(restaurant).toHaveProperty('address');
    });

    it('should respect limit of 100 restaurants', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act
      const result = await restaurantsCollection
        .find({})
        .limit(100)
        .toArray();

      // Assert
      expect(result.length).toBeLessThanOrEqual(100);
    });

    it('should return valid restaurant objects', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act
      const result = await restaurantsCollection
        .find({})
        .limit(1)
        .toArray();

      // Assert
      expect(result.length).toBeGreaterThan(0);
      const restaurant = result[0];

      // Check required fields exist
      expect(restaurant._id).toBeDefined();
      expect(restaurant.name).toBeDefined();
      expect(restaurant.borough).toBeDefined();
      expect(restaurant.cuisine).toBeDefined();
      expect(restaurant.address).toBeDefined();

      // Check address structure
      expect(restaurant.address).toHaveProperty('building');
      expect(restaurant.address).toHaveProperty('street');
      expect(restaurant.address).toHaveProperty('zipcode');
      expect(restaurant.address).toHaveProperty('coord');
      expect(Array.isArray(restaurant.address.coord)).toBe(true);
    });
  });

  describe('Filtered Query - Queens + Moon', () => {
    it('should retrieve restaurants from Queens with "Moon" in name', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act - Execute the same query as getRestaurantsByBorough()
      const result = await restaurantsCollection
        .find({
          borough: 'Queens',
          name: { $regex: 'Moon', $options: 'i' }
        })
        .limit(100)
        .toArray();

      // Assert
      expect(result).toBeDefined();
      expect(Array.isArray(result)).toBe(true);

      // Verify all results match our filters
      result.forEach((restaurant) => {
        expect(restaurant.borough).toBe('Queens');
        expect(restaurant.name.toLowerCase()).toContain('moon');
      });
    });

    it('should apply case-insensitive name filter', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act
      const result = await restaurantsCollection
        .find({
          borough: 'Queens',
          name: { $regex: 'Moon', $options: 'i' }
        })
        .limit(100)
        .toArray();

      // Assert - Should match "Moon", "moon", "MOON", etc.
      result.forEach((restaurant) => {
        expect(restaurant.name.toLowerCase()).toContain('moon');
      });
    });

    it('should only return Queens borough restaurants', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act
      const result = await restaurantsCollection
        .find({
          borough: 'Queens',
          name: { $regex: 'Moon', $options: 'i' }
        })
        .limit(100)
        .toArray();

      // Assert
      result.forEach((restaurant) => {
        expect(restaurant.borough).toBe('Queens');
      });
    });

    it('should respect limit of 100 restaurants', async () => {
      // Arrange
      const db = await connectToDatabase();
      const restaurantsCollection = db.collection('restaurants');

      // Act
      const result = await restaurantsCollection
        .find({
          borough: 'Queens',
          name: { $regex: 'Moon', $options: 'i' }
        })
        .limit(100)
        .toArray();

      // Assert
      expect(result.length).toBeLessThanOrEqual(100);
    });
  });
});

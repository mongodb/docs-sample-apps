/// <reference types="vitest" />

import { describe, it, expect } from 'vitest';
import { getAllRestaurants, getRestaurantsByBorough } from '#/server/restaurants';
import { describeIntegration } from './setup';

/**
 * Restaurant API Integration Tests
 *
 * These tests verify the full functionality of the restaurant server functions
 * with a real MongoDB connection (no mocks).
 *
 * The vitest integration config aliases @tanstack/react-start to a mock that
 * makes createServerFn().handler(fn) return fn directly, so server functions
 * can be called in a plain Node/Vitest environment without a TanStack Start
 * server context.
 *
 * Requirements:
 * - MONGODB_URI environment variable must be set
 * - MongoDB instance must be accessible
 * - sample_restaurants database with restaurants collection
 */

describeIntegration('Restaurant Server Functions Integration Tests', () => {
  describe('getAllRestaurants()', () => {
    it('should retrieve restaurants from database', async () => {
      const result = await getAllRestaurants();

      expect(result).toBeDefined();
      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBeGreaterThan(0);
      expect(result.length).toBeLessThanOrEqual(100);
    });

    it('should respect limit of 100 restaurants', async () => {
      const result = await getAllRestaurants();

      expect(result.length).toBeLessThanOrEqual(100);
    });

    it('should return valid restaurant objects with required fields', async () => {
      const result = await getAllRestaurants();

      expect(result.length).toBeGreaterThan(0);
      const restaurant = result[0];

      // _id should be serialized to a string
      expect(restaurant._id).toBeDefined();
      expect(typeof restaurant._id).toBe('string');
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

  describe('getRestaurantsByBorough()', () => {
    it('should retrieve restaurants from Queens with "Moon" in name', async () => {
      const result = await getRestaurantsByBorough();

      expect(result).toBeDefined();
      expect(Array.isArray(result)).toBe(true);

      // Verify all results match the filters
      result.forEach((restaurant) => {
        expect(restaurant.borough).toBe('Queens');
        expect(restaurant.name.toLowerCase()).toContain('moon');
      });
    });

    it('should apply case-insensitive name filter', async () => {
      const result = await getRestaurantsByBorough();

      result.forEach((restaurant) => {
        expect(restaurant.name.toLowerCase()).toContain('moon');
      });
    });

    it('should only return Queens borough restaurants', async () => {
      const result = await getRestaurantsByBorough();

      result.forEach((restaurant) => {
        expect(restaurant.borough).toBe('Queens');
      });
    });

    it('should respect limit of 100 restaurants', async () => {
      const result = await getRestaurantsByBorough();

      expect(result.length).toBeLessThanOrEqual(100);
    });
  });
});

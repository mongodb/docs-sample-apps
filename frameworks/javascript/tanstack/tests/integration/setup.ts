/// <reference types="vitest" />

import { beforeAll, afterAll, describe } from 'vitest';
import { connectToDatabase } from '#/lib/db';

/**
 * Integration Test Setup
 *
 * Provides setup/teardown hooks and helper functions for integration tests.
 * Following the mflix pattern for idempotent, conditionally-skipped integration tests.
 */

let hasShownIntegrationSkipMessage = false;

/**
 * Check if integration tests should be enabled
 */
export function isIntegrationTestEnabled(): boolean {
  return !!process.env.MONGODB_URI;
}

/**
 * Get the appropriate describe function based on whether integration tests are enabled
 * Usage: describeIntegration("My Test Suite", () => { ... })
 * This will skip the entire suite if MONGODB_URI is not set
 */
export const describeIntegration: typeof describe = isIntegrationTestEnabled()
  ? describe
  : ((...args: Parameters<typeof describe>) => {
      if (!hasShownIntegrationSkipMessage) {
        console.log(`
⚠️  Integration tests skipped: MONGODB_URI environment variable is not set
   To run integration tests, set MONGODB_URI to your MongoDB connection string
   Example: MONGODB_URI=mongodb://localhost:27017/sample_restaurants npm run test:integration
`);
        hasShownIntegrationSkipMessage = true;
      }
      return describe.skip(...args);
    }) as typeof describe;

// Global setup - runs once before all tests
beforeAll(async () => {
  if (!isIntegrationTestEnabled()) {
    return;
  }

  try {
    await connectToDatabase();
    console.log('✅ Connected to MongoDB for integration tests');
  } catch (error) {
    console.error('❌ Failed to connect to MongoDB:', error);
    throw error;
  }
});

// Global teardown - runs once after all tests
afterAll(async () => {
  if (!isIntegrationTestEnabled()) {
    return;
  }

  try {
    // Note: db.ts doesn't export a close function, so connection stays open
    // This is fine for test runs as the process will exit
    console.log('✅ Integration tests completed');
  } catch (error) {
    console.error('❌ Error during teardown:', error);
  }
});

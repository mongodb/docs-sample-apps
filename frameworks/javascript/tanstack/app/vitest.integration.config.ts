import { defineConfig } from 'vitest/config';
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';
import dotenv from 'dotenv';

// Load environment variables from .env file
dotenv.config();

// ESM equivalent of __dirname
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/**
 * Vitest configuration for integration tests
 *
 * Integration tests connect to a real MongoDB instance to test actual database operations.
 * Requires MONGODB_URI environment variable to be set (in .env file or CLI).
 *
 * Run with: npm run test:integration
 * Or: MONGODB_URI=mongodb://localhost:27017/sample_restaurants npm run test:integration
 */
export default defineConfig({
  test: {
    // Make Vitest globals available without imports (describe, it, expect, vi)
    globals: true,
    
    // Use Node.js environment for server-side code
    environment: 'node',
    
    // Only run integration tests (exclude unit tests)
    include: ['../tests/integration/**/*.integration.test.ts'],
    exclude: [
      '../tests/unit/**',
      '**/node_modules/**',
      '**/dist/**',
      '**/.tanstack/**',
      '**/.vinxi/**',
    ],
    
    // Longer timeout for integration tests (real DB operations)
    testTimeout: 60000, // 60 seconds
    
    // Run tests sequentially to avoid DB conflicts
    // Change to `false` if your tests are properly isolated
    poolOptions: {
      threads: {
        singleThread: true,
      },
    },
    
    // Coverage configuration
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.config.ts',
        '**/*.test.ts',
      ],
    },
  },
  
  // Resolve paths relative to the app directory
  resolve: {
    alias: {
      '#': resolve(__dirname, './src'),
      // Replace TanStack's createServerFn with a passthrough so server
      // functions can be called directly in integration tests.
      '@tanstack/react-start': resolve(__dirname, '../tests/integration/tanstack-react-start.mock.ts'),
    },
  },
});

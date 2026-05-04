import { defineConfig } from 'vitest/config';
import { fileURLToPath } from 'url';
import { dirname, resolve } from 'path';

// ESM equivalent of __dirname
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/**
 * Vitest configuration for unit tests
 *
 * Unit tests use mocked dependencies (including MongoDB) for fast, isolated testing.
 * These tests don't require a real database connection.
 *
 * Run with: npm run test:unit
 */
export default defineConfig({
  test: {
    // Make Vitest globals available without imports (describe, it, expect, vi)
    globals: true,
    
    // Use Node.js environment for server-side code
    environment: 'node',
    
    // Only run unit tests (exclude integration tests)
    include: ['../tests/unit/**/*.test.ts'],
    exclude: [
      '../tests/integration/**',
      '**/node_modules/**',
      '**/dist/**',
      '**/.tanstack/**',
      '**/.vinxi/**',
    ],
    
    // Test timeout (unit tests should be fast)
    testTimeout: 10000, // 10 seconds
    
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
    },
  },
});

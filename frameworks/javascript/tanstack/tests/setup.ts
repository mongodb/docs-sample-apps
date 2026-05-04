/**
 * Global test setup
 * 
 * This file runs before all tests (both unit and integration).
 * Use it for global configuration that applies to all test types.
 */

// Set NODE_ENV to test mode
process.env.NODE_ENV = 'test';

// Suppress console logs during tests (optional - uncomment if desired)
// global.console = {
//   ...console,
//   log: vi.fn(),
//   debug: vi.fn(),
//   info: vi.fn(),
//   warn: vi.fn(),
// };

export {};

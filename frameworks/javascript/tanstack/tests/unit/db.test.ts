/// <reference types="vitest" />

import { describe, it, expect } from "vitest";

/**
 * Unit tests for database connection module
 *
 * Note: db.ts is a simple utility. For unit tests, we mock connectToDatabase
 * in the modules that use it (like restaurants.test.ts does).
 * This is just a smoke test to ensure the module loads.
 */

// Set MONGODB_URI so module can load
process.env.MONGODB_URI = "mongodb://localhost:27017/test";

describe("Database Module", () => {
  it("should export connectToDatabase function", async () => {
    const db = await import("#/lib/db");
    expect(db.connectToDatabase).toBeDefined();
    expect(typeof db.connectToDatabase).toBe("function");
  });
});

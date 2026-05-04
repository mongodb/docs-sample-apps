/**
 * Global type declarations for Vitest
 * 
 * This file provides TypeScript support for Vitest globals (vi, describe, it, expect, etc.)
 * when using `globals: true` in vitest config.
 */

/// <reference types="vitest/globals" />

// Extend ImportMeta to support import.meta.url in ESM
interface ImportMeta {
  readonly url: string;
}

/**
 * Mock for @tanstack/react-start used by integration tests.
 *
 * Makes createServerFn().handler(fn) return fn directly so server
 * functions can be called in a plain Node/Vitest environment without
 * a TanStack Start server context.
 */
export const createServerFn = () => ({
  handler: (fn: (...args: any[]) => any) => fn,
});

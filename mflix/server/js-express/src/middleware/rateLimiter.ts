import rateLimit from "express-rate-limit";

/**
 * Rate limiter for movie API routes that access the database.
 * Uses a higher limit in test environments so integration tests are unaffected.
 */
export const moviesRateLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: process.env.NODE_ENV === "test" ? 10_000 : 300,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    message: "Too many requests. Please try again later.",
    error: {
      message: "Too many requests. Please try again later.",
      code: "RATE_LIMIT_EXCEEDED",
    },
  },
});

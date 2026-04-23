# Testing Documentation

Comprehensive test suite for the TanStack Start + MongoDB sample application, following the mflix testing patterns.

## 📊 Test Overview

| Type | Files | Tests | Duration | Database |
|------|-------|-------|----------|----------|
| **Unit** | 2 | 10 | ~400ms | ❌ Mocked |
| **Integration** | 1 | 7 | ~2s | ✅ Real MongoDB |
| **Total** | 3 | **17** | ~2.4s | - |

## 🚀 Running Tests

```bash
# Run all tests
npm test

# Run unit tests only (no MongoDB required)
npm run test:unit

# Run integration tests only (requires MongoDB)
npm run test:integration

# Watch mode for development
npm run test:watch
```

## 📁 Test Structure

```
tests/
├── unit/                          # Unit tests (mocked dependencies)
│   ├── db.test.ts                # Database module tests (1 test)
│   └── restaurants.test.ts       # Server functions tests (9 tests)
├── integration/                   # Integration tests (real database)
│   ├── setup.ts                  # Test setup/teardown
│   └── restaurants.integration.test.ts  # DB operations (7 tests)
├── utils/                        # Shared test utilities
│   ├── testHelpers.ts           # Sample data, mocks, helpers
│   └── README.md                # Utilities documentation
├── tsconfig.json                # TypeScript config for tests
├── vitest.d.ts                  # Vitest type definitions
└── README.md                    # This file
```

## 🧪 Unit Tests

Unit tests mock all external dependencies (database, MongoDB driver) to test business logic in isolation.

### What We Test

#### `db.test.ts` (1 test)
- ✅ Module exports `connectToDatabase` function

#### `restaurants.test.ts` (9 tests)
- ✅ `getAllRestaurants()` - 4 tests
  - Query construction
  - Empty results handling
  - Database error handling
  - ObjectId conversion
  
- ✅ `getRestaurantsByBorough()` - 4 tests
  - Filtered query (borough + name)
  - Case-insensitive regex
  - Empty filtered results
  - Database errors

- ✅ Database Connection - 1 test
  - Connection reuse verification

### Mocking Strategy

Following the **mflix pattern**, we mock at the **application boundary** (not the MongoDB driver):

```typescript
// ✅ Mock the database module
const mockConnectToDatabase = vi.fn().mockResolvedValue(mockDb);
vi.mock('#/lib/db', () => ({
  connectToDatabase: mockConnectToDatabase
}));

// ✅ Also prevent mongodb from loading
vi.mock('mongodb', () => ({
  MongoClient: vi.fn(),
  ObjectId: class ObjectId { ... }
}));
```

### Example Unit Test

```typescript
it('should query database for all restaurants', async () => {
  // Arrange
  mockToArray.mockResolvedValue(SAMPLE_RESTAURANTS);

  // Act
  await getAllRestaurants();

  // Assert
  expect(mockConnectToDatabase).toHaveBeenCalledOnce();
  expect(mockDb.collection).toHaveBeenCalledWith('restaurants');
  expect(mockFind).toHaveBeenCalledWith({});
  expect(mockLimit).toHaveBeenCalledWith(100);
});
```

## 🔗 Integration Tests

Integration tests use a real MongoDB connection to verify end-to-end database operations.

### What We Test

#### `restaurants.integration.test.ts` (7 tests)

**Get All Restaurants Query** - 3 tests
- ✅ Retrieve restaurants from database
- ✅ Respect limit of 100
- ✅ Return valid restaurant objects

**Filtered Query (Queens + Moon)** - 4 tests
- ✅ Filter by borough and name regex
- ✅ Case-insensitive name matching
- ✅ Only return matching borough
- ✅ Respect query limit

### Prerequisites

Integration tests require:
1. `MONGODB_URI` environment variable set
2. Accessible MongoDB instance
3. `sample_restaurants` database with `restaurants` collection

### Conditional Execution

Tests automatically skip if `MONGODB_URI` is not set:

```typescript
import { describeIntegration } from './setup';

describeIntegration('My Integration Tests', () => {
  // These tests only run if MONGODB_URI is set
  it('should query real database', async () => {
    // ...
  });
});
```

### Example Integration Test

```typescript
it('should retrieve restaurants from database', async () => {
  // Arrange
  const db = await connectToDatabase();
  const restaurantsCollection = db.collection('restaurants');
  
  // Act - Execute the same query as getAllRestaurants()
  const result = await restaurantsCollection
    .find({})
    .limit(100)
    .toArray();

  // Assert
  expect(result).toBeDefined();
  expect(result.length).toBeGreaterThan(0);
  expect(result[0]).toHaveProperty('name');
});
```

## 🛠️ Test Utilities

See [`utils/README.md`](./utils/README.md) for detailed documentation on:
- Sample data constants
- Mock creation helpers
- Integration test helpers
- Assertion utilities

### Quick Reference

```typescript
// Sample data
import { SAMPLE_RESTAURANTS, SAMPLE_QUEENS_RESTAURANTS } from '../utils/testHelpers';

// Unit test mocks
const mockCollection = createMockCollection(SAMPLE_RESTAURANTS);
const mockDb = createMockDatabase(mockCollection);
```

## ⚙️ Configuration

### Vitest Configs

**`vitest.unit.config.ts`** - Unit test configuration
- Runs tests from `tests/unit/`
- No environment setup required
- Fast execution

**`vitest.integration.config.ts`** - Integration test configuration
- Runs tests from `tests/integration/`
- Loads `.env` file automatically (via `dotenv`)
- Requires MongoDB connection

### TypeScript Config

**`tests/tsconfig.json`** - TypeScript configuration for tests
- Extends from `../app/tsconfig.json`
- Points `typeRoots` to `../app/node_modules`
- Supports path aliases (`#/*` → `../app/src/*`)

## 🎯 Testing Principles

Following **mflix testing patterns**:

- ✅ **Idempotent Tests** - Can run multiple times without side effects
- ✅ **Isolated Tests** - Each test is independent
- ✅ **Conditional Integration** - Skip if MongoDB unavailable
- ✅ **Fast Unit Tests** - No database calls (~400ms)
- ✅ **Comprehensive Coverage** - Happy path, errors, edge cases

## 🐛 Troubleshooting

### Integration Tests Skipped

```
⚠️  Integration tests skipped: MONGODB_URI environment variable is not set
```

**Solution:** Set `MONGODB_URI` in `.env` file or environment.

### TypeScript Errors in Tests

**Solution:** Restart TypeScript server (`Cmd+Shift+P` → "TypeScript: Restart TS Server")

### Unit Tests Hang

**Solution:** Ensure you're mocking `mongodb` to prevent loading the real driver.

## 📚 Additional Resources

- [Vitest Documentation](https://vitest.dev/)
- [MongoDB Node.js Driver Testing](https://www.mongodb.com/docs/drivers/node/current/fundamentals/testing/)
- [mflix Sample App](../../mflix/) - Reference implementation

## 🎊 Summary

This test suite provides:
- ✅ 17 comprehensive tests (10 unit + 7 integration)
- ✅ mflix-quality patterns
- ✅ Fast execution (~2.4s total)
- ✅ Environment-aware (skips gracefully without DB)
- ✅ Ready for CI/CD integration

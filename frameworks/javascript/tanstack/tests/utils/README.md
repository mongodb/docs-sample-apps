# Test Utilities and Helpers

This directory contains shared utilities and test data for both unit and integration tests.

## 📁 Files

- **`testHelpers.ts`** - Main test utilities file containing sample data, mocks, and assertion helpers

---

## 🎯 Usage Examples

### **Unit Tests (with mocks)**

```typescript
import { vi } from 'vitest';
import { 
  SAMPLE_RESTAURANTS, 
  createMockCollection, 
  createMockDatabase 
} from '../utils/testHelpers';
import { getAllRestaurants } from '#/server/restaurants';

describe('getAllRestaurants', () => {
  test('should return all restaurants', async () => {
    // Create mock collection
    const mockCollection = createMockCollection(SAMPLE_RESTAURANTS);
    const mockDb = createMockDatabase(mockCollection);
    
    // Mock the database connection
    vi.mock('#/lib/db', () => ({
      connectToDatabase: vi.fn().mockResolvedValue(mockDb)
    }));
    
    // Call the function
    const result = await getAllRestaurants();
    
    // Assertions
    expect(result).toHaveLength(SAMPLE_RESTAURANTS.length);
    expect(mockCollection.find).toHaveBeenCalled();
    expect(mockCollection.limit).toHaveBeenCalledWith(100);
  });
});
```

### **Integration Tests (with real database)**

```typescript
import { 
  createTestRestaurant, 
  getTestRestaurantPattern,
  expectRestaurantMatch 
} from '../utils/testHelpers';
import { describeIntegration } from '../integration/setup';
import { connectToDatabase } from '#/lib/db';

describeIntegration('Restaurant Integration Tests', () => {
  let testRestaurantIds: string[] = [];

  beforeAll(async () => {
    // Clean up orphaned test data
    const db = await connectToDatabase();
    await db.collection('restaurants').deleteMany({
      name: { $regex: getTestRestaurantPattern() }
    });
  });

  afterEach(async () => {
    // Clean up test data after each test
    if (testRestaurantIds.length > 0) {
      const db = await connectToDatabase();
      await db.collection('restaurants').deleteMany({
        _id: { $in: testRestaurantIds.map(id => new ObjectId(id)) }
      });
      testRestaurantIds = [];
    }
  });

  test('should insert a restaurant', async () => {
    const testRestaurant = createTestRestaurant({
      borough: 'Queens',
      cuisine: 'Italian'
    });
    
    const db = await connectToDatabase();
    const result = await db.collection('restaurants').insertOne(testRestaurant);
    
    testRestaurantIds.push(result.insertedId.toString());
    
    expect(result.insertedId).toBeDefined();
  });
});
```

---

## 📚 Available Exports

### **Constants**
- `TEST_OBJECT_IDS` - Valid and invalid ObjectId strings for testing
- `TEST_RESTAURANT_PREFIX` - Prefix for integration test restaurant names

### **Sample Data**
- `SAMPLE_RESTAURANT` - Single restaurant object
- `SAMPLE_RESTAURANTS` - Array of 3 restaurants
- `SAMPLE_QUEENS_RESTAURANTS` - Restaurants from Queens borough
- `SAMPLE_MOON_RESTAURANTS` - Restaurants with "Moon" in name

### **Mock Helpers (Unit Tests)**
- `createMockCollection()` - Creates mock MongoDB collection
- `createMockDatabase()` - Creates mock database instance

### **Integration Test Helpers**
- `createTestRestaurant()` - Creates a single test restaurant with unique identifiers
- `createTestRestaurants(count)` - Creates multiple test restaurants
- `getTestRestaurantPattern()` - Returns regex for cleanup operations

### **Assertion Helpers**
- `expectRestaurantMatch(restaurant)` - Validates restaurant structure
- `expectAllQueensRestaurants(restaurants)` - Asserts all are from Queens
- `expectAllMoonRestaurants(restaurants)` - Asserts all have "Moon" in name

---

## 🔑 Key Patterns

### **Idempotent Test Data**
Test restaurants are created with unique timestamps and random suffixes to avoid conflicts:
```typescript
const restaurant = createTestRestaurant();
// name: "Integration Test Restaurant 1714588800000-a3b5c7"
```

### **Cleanup Pattern**
Use the test prefix to clean up all test data:
```typescript
await collection.deleteMany({
  name: { $regex: getTestRestaurantPattern() }
});
```

### **Type Safety**
All helpers are fully typed using the `Restaurant` interface from your app code.

---

## 🎨 Following mflix Pattern

These test helpers follow the same patterns used in `mflix/server/js-express/tests/utils/testHelpers.ts`:

- ✅ Sample data matching real schema
- ✅ Mock utilities for unit tests
- ✅ Integration helpers for real database tests
- ✅ Assertion helpers for common validations
- ✅ Idempotent test data patterns

# TanStack Start + MongoDB Sample Application

A full-stack restaurant directory application built with TanStack Start and MongoDB, demonstrating modern React Server Components with database integration.

## 📋 Overview

This sample application showcases:
- **TanStack Start** for server-side rendering and server functions
- **MongoDB** for data persistence (sample_restaurants database)
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **Vitest** for comprehensive testing (unit + integration)

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- MongoDB Atlas account or local MongoDB instance
- MongoDB sample_restaurants database loaded

### Installation

```bash
# Install dependencies
npm install

# Configure environment variables
cp .env.example .env
# Edit .env and add your MONGODB_URI
```

### Development

```bash
# Start development server
npm run dev
```

Visit [http://localhost:3000](http://localhost:3000) to see the application.

### Production Build

```bash
# Build for production
npm run build

# Preview production build
npm run start
```

## 🧪 Testing

This project uses [Vitest](https://vitest.dev/) with comprehensive test coverage.

```bash
# Run all tests (unit + integration, requires MongoDB)
npm run test:all

# Run unit tests only (no MongoDB required, default)
npm test

# Run integration tests only (requires MongoDB)
npm run test:integration

# Watch mode for development
npm run test:watch
```

**Test Coverage:**
- **Unit Tests** (10 tests) - Mock database, test business logic
- **Integration Tests** (7 tests) - Real MongoDB, test queries

See [`../tests/README.md`](../tests/README.md) for detailed testing documentation.

## 🎨 Styling

This project uses [Tailwind CSS](https://tailwindcss.com/) for styling.

### Removing Tailwind CSS

If you prefer not to use Tailwind CSS:

1. Remove the demo pages in `src/routes/demo/`
2. Replace the Tailwind import in `src/styles.css` with your own styles
3. Remove `tailwindcss()` from the plugins array in `vite.config.ts`
4. Uninstall the packages: `npm uninstall @tailwindcss/vite tailwindcss`

## 📂 Project Structure

```
app/
├── src/
│   ├── routes/              # File-based routing
│   │   ├── index.tsx       # Home page (all restaurants)
│   │   ├── queens.tsx      # Queens restaurants page
│   │   └── __root.tsx      # Root layout
│   ├── server/             # Server functions
│   │   └── restaurants.ts  # Restaurant server functions
│   ├── lib/                # Utilities
│   │   └── db.ts          # MongoDB connection
│   └── types/             # TypeScript types
│       └── restaurant.ts  # Restaurant type definitions
├── tests/                 # Test files (separate directory)
│   ├── unit/             # Unit tests (mocked)
│   ├── integration/      # Integration tests (real DB)
│   └── utils/            # Test helpers
└── .env                  # Environment variables

```

## 🗄️ Database Schema

This app uses the MongoDB `sample_restaurants` database with the following structure:

```typescript
interface Restaurant {
  _id: ObjectId | string;
  name: string;
  borough: string;
  cuisine: string;
  restaurant_id: string;
  address: {
    building: string;
    street: string;
    zipcode: string;
    coord: [number, number];
  };
  grades: Array<{
    date: Date;
    grade: string;
    score: number;
  }>;
}
```

## 🔧 Environment Variables

Create a `.env` file in the `app/` directory:

```bash
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/sample_restaurants
```

## 📡 API Endpoints / Server Functions

### `getAllRestaurants()`
- Returns all restaurants (limit 100)
- Used on homepage

### `getRestaurantsByBorough()`
- Filters restaurants by Queens borough with "Moon" in name
- Case-insensitive regex search
- Used on `/queens` page

## 🎯 Routing

This project uses [TanStack Router](https://tanstack.com/router) with file-based routing. Routes are managed as files in `src/routes`.

### Available Routes

- `/` - Home page showing all restaurants
- `/queens` - Filtered view of Queens restaurants with "Moon" in name

### Adding A Route

To add a new route, just add a new file in the `./src/routes` directory.

TanStack will automatically generate the content of the route file for you.

### Adding Links

To use SPA (Single Page Application) navigation:

```tsx
import { Link } from "@tanstack/react-router";

<Link to="/about">About</Link>
```

## 💡 Key Concepts

### Server Functions

This app demonstrates TanStack Start server functions for database queries:

```tsx
import { createServerFn } from '@tanstack/react-start';
import { connectToDatabase } from '#/lib/db';

export const getAllRestaurants = createServerFn({
  method: 'GET',
}).handler(async () => {
  const db = await connectToDatabase();
  const restaurants = await db
    .collection('restaurants')
    .find({})
    .limit(100)
    .toArray();

  return restaurants.map(r => ({ ...r, _id: r._id.toString() }));
});
```

### Using Server Functions in Components

```tsx
import { getAllRestaurants } from '#/server/restaurants';

function RestaurantList() {
  const restaurants = getAllRestaurants.useQuery()({
    queryKey: ['restaurants'],
  });

  return (
    <ul>
      {restaurants.data?.map((r) => (
        <li key={r._id}>{r.name}</li>
      ))}
    </ul>
  );
}
```
## 🧹 Development Notes

### MongoDB Connection Pooling

The `db.ts` module implements connection reuse to optimize performance:

```typescript
let connected = false;
let client: MongoClient;

export async function connectToDatabase() {
  if (!connected) {
    client = new MongoClient(uri);
    await client.connect();
    connected = true;
  }
  return client.db("sample_restaurants");
}
```

This ensures a single connection is reused across all requests.

## 📚 Learn More

- [TanStack Start Documentation](https://tanstack.com/start)
- [TanStack Router Documentation](https://tanstack.com/router)
- [MongoDB Node.js Driver](https://www.mongodb.com/docs/drivers/node/)
- [Vitest Documentation](https://vitest.dev/)

## 🤝 Related Sample Apps

This app follows the same patterns as other MongoDB sample apps:
- **mflix** (Python FastAPI + JavaScript Express) - Movie database with full CRUD

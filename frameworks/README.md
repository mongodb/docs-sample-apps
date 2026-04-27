# MongoDB Framework Examples

Full-stack, runnable applications demonstrating MongoDB integration with modern JavaScript frameworks. These apps serve as the complete, tested source code for MongoDB framework tutorials.

## 📚 Available Examples

### **TanStack Start** - `javascript/tanstack/`

A restaurant browsing application built with TanStack Start, MongoDB, and TanStack Query.

**📖 Tutorial:** [MongoDB with TanStack Start](https://www.mongodb.com/docs/drivers/node-frameworks/tanstack/)

**Features:**
- Server-side data fetching with MongoDB
- TanStack Query for client-side caching
- Full-stack TypeScript
- Comprehensive test suite (17 tests)

**Quick Start:**
```bash
cd frameworks/javascript/tanstack/app
npm install
cp .env.example .env
# Add your MongoDB connection string to .env
npm run dev
```

Visit `http://localhost:3000` to see the app!

---

## 🎯 What You'll Learn

Each example demonstrates:
- ✅ **MongoDB Connection** - Connecting to MongoDB Atlas or local instance
- ✅ **CRUD Operations** - Creating, reading, updating, and deleting data
- ✅ **Query Patterns** - Filtering, sorting, and pagination
- ✅ **Best Practices** - Error handling, connection management, type safety
- ✅ **Testing** - Unit and integration tests for database operations

---

## 📂 Project Structure

Each framework example follows this pattern:

```
javascript/{framework-name}/
├── app/                 # Complete working application
│   ├── src/            # Source code
│   ├── package.json    # App dependencies
│   └── .env.example    # Environment template
├── tests/              # Test suite (unit + integration)
└── testedSnippets/     # Auto-generated code snippets for docs
```

---

## 🧪 Running Tests

All examples include comprehensive test suites. Tests run from the `app/` directory:

```bash
cd frameworks/javascript/tanstack/app

# Run all tests (unit + integration, requires MongoDB)
npm run test:all

# Run only unit tests (no MongoDB required, default)
npm test

# Run only integration tests (requires MongoDB)
npm run test:integration
```

**Requirements for integration tests:**
- MongoDB instance (local or Atlas)
- `sample_restaurants` database loaded (see tutorial)
- Valid `MONGODB_URI` in `.env` file

---

## 🎨 Code Annotations (Can Be Ignored)

You may notice special comments in the code:

```typescript
// :snippet-start: connect
const client = new MongoClient(uri)
// :snippet-end:
```

These are **Bluehawk annotations** used to extract code snippets for documentation. They:
- ✅ Are standard TypeScript/JavaScript comments
- ✅ Don't affect code execution or compilation
- ✅ Can be safely ignored when learning from the code
- ✅ Ensure tutorial snippets stay in sync with working code

---

## 🔧 For MongoDB Docs Maintainers

### Extracting Code Snippets

To generate documentation snippets from the source code:

```bash
cd frameworks/javascript/tanstack
npm install  # Install Bluehawk + Prettier
npm run snip # Extract and format snippets
```

Snippets are written to `testedSnippets/` with this naming pattern:
```
{filename}.snippet.{tag-name}.{ext}
```

Example: `db.snippet.db-connection.ts`

### Directory Structure for Docs Team

```
javascript/{framework-name}/
├── app/                    # Source code (with Bluehawk annotations)
├── testedSnippets/         # Generated snippets (committed to repo)
├── package.json            # Bluehawk + Prettier dependencies
├── snip.config.json        # Snippet extraction config
└── .prettierrc             # Code formatting rules
```

### Adding New Snippets

1. Add Bluehawk annotations to source code:
   ```typescript
   // :snippet-start: my-snippet
   // Your code here
   // :snippet-end:
   ```

2. Run extraction:
   ```bash
   npm run snip
   ```

3. Verify output in `testedSnippets/`

Learn more: [Bluehawk Documentation](https://mongodb-university.github.io/Bluehawk/)

---

## 🤝 Contributing

When adding or updating framework examples:

1. ✅ Ensure the app runs independently and matches the tutorial
2. ✅ Add comprehensive tests 
3. ✅ Add Bluehawk annotations to code sections used in tutorials
4. ✅ Extract snippets and verify formatting
5. ✅ Update this README with the new framework

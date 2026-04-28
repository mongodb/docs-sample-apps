# MongoDB Sample Applications & Framework Examples

Runnable, tested code examples for learning MongoDB with various languages and frameworks.

## Repository Contents

This repository contains two types of learning resources:

### 1. **Sample Applications** (`mflix/`)

Full-stack movie browsing applications demonstrating MongoDB with different backend languages.

**MFlix Sample App** - A Next.js frontend with your choice of three backend stacks:

- **Java:** Spring Boot
- **Node.js:** Express.js
- **Python:** FastAPI

```
mflix/
├── client/               # Next.js frontend
└── server/
    ├── express/          # Express.js backend
    ├── java-spring/      # Java/Spring backend
    └── python/           # Python/FastAPI backend
```

**Learn more:** See the [mflix README](mflix/README.md) for setup and usage.

---

### 2. **Framework Examples** (`frameworks/`)

Modern full-stack applications demonstrating MongoDB integration with popular JavaScript frameworks. These serve as the complete, tested source code for MongoDB framework tutorials.

**Current Examples:**
- **TanStack Start** - Restaurant browsing app with server-side data fetching ([Tutorial](https://www.mongodb.com/docs/drivers/node-frameworks/tanstack/))

```
frameworks/
└── javascript/
    └── tanstack/
        ├── app/              # Full working application
        ├── tests/            # Comprehensive test suite
        └── testedSnippets/   # Auto-generated code snippets for docs
```

**Learn more:** See the [frameworks README](frameworks/README.md) for available examples and usage.

---

## Quick Start

### Running MFlix Sample Apps

```bash
# Choose your backend language
cd mflix/server/express  # or java-spring, or python

# Follow the README in that directory
```

### Running Framework Examples

```bash
# Choose your framework example
cd frameworks/javascript/tanstack/app  # or other framework example

# Follow the README in the framework directory
```

---

## Testing

### Framework Examples
See individual framework README files for testing instructions.

### MFlix Sample Apps
See individual backend README files for testing instructions.

---

## For MongoDB Docs Team

### Artifact Repositories (MFlix Only)

This repository serves as the source for the following MFlix artifact repositories:

- Java: [mongodb/sample-app-java-mflix](https://github.com/mongodb/sample-app-java-mflix)
- Node.js: [mongodb/sample-app-nodejs-mflix](https://github.com/mongodb/sample-app-nodejs-mflix)
- Python: [mongodb/sample-app-python-mflix](https://github.com/mongodb/sample-app-python-mflix)

**Note:** Framework examples are maintained directly in this repository and do not use the copier tool.

### Development Workflow

#### MFlix Sample Apps

When you merge to `main`, a copier tool copies the source from this repository
to a target repository for each sample app. For configuration details, refer to
`copier-config.yaml`.

**Branching Model:** Work from the `development` branch. Make incremental PRs
containing new features and bug fixes to `development`, _not_ `main`.

When all development work is complete, create a release PR from
`development` to `main`. Upon merging to `main`, the copier tool runs
automatically and creates a new PR in the target repository.

#### Framework Examples

Framework examples follow a simpler workflow:
1. Make changes directly in `frameworks/`
2. Add/update Bluehawk annotations for code snippets
3. Run `npm run snip` to extract snippets
4. Commit both source code and extracted snippets
5. Create PR to `main`

No copier tool is used - snippets are committed directly to this repository.

### MFlix Release Process

When you merge a release PR from `development` to `main`, the copier tool
creates a new PR in the target repository. This PR must be tested and merged
manually.

**Deleting Files:** If a PR deletes any files, this creates a new entry in the
`deprecated_examples.json` file. The copier tool does not delete files from the
target repository. You must manually delete files from the target repository
that are listed in `deprecated_examples.json` to avoid accidentally deleting
files that are referred to in documentation.

**Testing the Release:** Navigate to the target repository and:
- [ ] Verify that the PR contains the expected changes
- [ ] Check out the PR locally and test
- [ ] Review `deprecated_examples.json` for any files that need deletion
- [ ] Merge the PR

---

## Issues

If you're having issues with any of the sample apps or framework examples, please open an issue in this repository. Include:

- **Which app** you're using (MFlix Java/Node.js/Python, or TanStack, etc.)
- **MongoDB version** and deployment type (local, Atlas, etc.)
- **Driver version** you're using
- **Steps to reproduce** the issue
- **Error messages** or unexpected behavior
- **Any other relevant information**

---

## Contributions

We are not currently accepting public contributions to this repository. However, if you find issues or have suggestions, please open an issue!

---

## Additional Resources

- [MongoDB Documentation](https://www.mongodb.com/docs/)
- [MongoDB Drivers](https://www.mongodb.com/docs/drivers/)
- [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
- [MongoDB Community Forums](https://www.mongodb.com/community/forums/)

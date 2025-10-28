# Docs Sample Apps

A repository of sample applications for the MongoDB documentation. This README
describes internal details for the repository maintainers. If you are a developer
having issues with the sample app, please refer to the `Issues` section below.

## Sample Apps

This repository currently contains a single sample app using the `mflix` dataset.

### MFlix Sample App

The sample app provides a Next.js frontend in the `client` directory, with the
choice of three backend stacks in the `server` directory:

- Java: Spring Boot
- JavaScript: Express.js
- Python: FastAPI

```
├── mflix/
│   ├── client/               # Next.js frontend - source for all `mflix` sample app backend repos
│   └── server/
│       ├── express/          # Express.js backend - source for sample-app-nodejs-mflix
│       ├── java-spring/      # Java/Spring backend - source for sample-app-java-mflix
│       └── python/           # Python/FastAPI backend - source for sample-app-python-mflix
├── README.md
├── copier-config.yaml
└── deprecated_examples.json
```

## Artifact Repositories

This repository serves as the source for the following artifact repositories:

- Java: [mongodb/sample-app-java-mflix](https://github.com/mongodb/sample-app-java-mflix)
- JavaScript: [mongodb/sample-app-nodejs-mflix](https://github.com/mongodb/sample-app-nodejs-mflix)
- Python: [mongodb/sample-app-python-mflix](https://github.com/mongodb/sample-app-python-mflix)

## Development

When you merge to `main`, a copier tool copies the source from this repository
to a target repository for each sample app. For configuration details, refer to
`copier-config.yaml`.

### Branching Model

For development, work from the `development` branch. Make incremental PRs
containing new features and bug fixes to `development`, *not* `main`.

When all development work is complete, *then* create a release PR from
`development` to `main`. Upon merging to `main,` the copier tool runs
automatically. It creates a new PR in the target repository, which must be
tested and merged manually.

### Deleting Files

If a PR from `development` to `main` deletes any files, this creates a new
entry in the `deprecated_examples.json` file. This entry resembles:

```json
{
  "filename": "go/gcloud24march_v2.go",
  "repo": "docs-code-examples-test-target",
  "branch": "v2.2",
  "deleted_on": "2025-03-24T18:16:30Z"
},
```

The copier tool does not delete files from the target repository. You must
manually delete files from the target repository that are listed in
`deprecated_examples.json`. This is an intentional step to avoid accidentally
deleting files that are referred to in documentation. Review documentation
references before deleting files.

## Release Process

When you merge a release PR from `development` to `main`, the copier tool
creates a new PR in the target repository. This PR must be tested and merged
manually. This is an intentional design choice to ensure:

- The sample app still functions as expected after copying.
- Any documentation references are updated as part of the release process.

To test and verify the PR, navigate to the target repository - see
`Artifact Repositories` above. Perform the following checks:

- [ ] Verify that the PR contains the expected changes.
- [ ] Check out the PR locally.
  - [ ] Build and test the changes.
  - [ ] Run the tests
  - [ ] Run the application and verify that it functions as expected.
- [ ] Review the `deprecated_examples.json` file for any files that need to be
  deleted. If files are deleted:
  - [ ] Add a commit to the copier PR to delete the files from the target repository.
- [ ] Merge the PR.

## Issues

If you are a developer having issues with the sample app, feel free to open an
issue in this repository. Please include the following information:

- [ ] The sample app you are using (Java, JavaScript, or Python)
- [ ] The version of the MongoDB database you are using
- [ ] The version of the MongoDB driver you are using
- [ ] What type of deployment you're using (local, Atlas, etc.)
- [ ] Any other relevant information that might help us reproduce the issue

## Contributions

We are not currently accepting public contributions to this repository.

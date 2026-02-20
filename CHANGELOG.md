# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project setup
- Maven configuration for Kotlin Multiplatform client generation
- npm configuration for TypeScript client generation
- GitHub Actions workflows for automated publishing
- CI/CD pipeline for continuous integration

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.0] - YYYY-MM-DD

### Added
- Kotlin Multiplatform client for Hex-Tractor API
- TypeScript/JavaScript client for Hex-Tractor API
- Support for all Hex-Tractor API endpoints:
  - Account information retrieval
  - League entries lookup
  - Match details and history
- OpenAPI Generator integration
- Comprehensive documentation
- GitHub Actions workflows for publishing to npm and Maven Central

---

## Version History Template

When releasing a new version, copy this template:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security fixes or improvements
```

## Release Process

1. Update this CHANGELOG with changes for the new version
2. Update version in `package.json` and `pom.xml` (or let workflow do it)
3. Commit changes: `git commit -am "Release version X.Y.Z"`
4. Create and push tag: `git tag vX.Y.Z && git push origin vX.Y.Z`
5. GitHub Actions will automatically publish both packages

See [PUBLISHING.md](PUBLISHING.md) for detailed release instructions.

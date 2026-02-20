# Quick Reference Guide

Quick reference for common operations with the Hex-Tractor API client generation project.

## 🚀 Quick Start

### Install and Build

```bash
# Install npm dependencies
npm install

# Build both clients
# Kotlin:
mvn clean package

# TypeScript:
npm run build
```

## 🔧 Development

### Generate Code Only (No Build)

```bash
# Kotlin
mvn generate-sources

# TypeScript
npm run generate
```

### Clean Generated Files

```bash
# Kotlin
mvn clean

# TypeScript
npm run clean

# Both
mvn clean && npm run clean
```

### Watch OpenAPI Changes

```bash
# No built-in watch mode, but you can use:
watch -n 5 mvn generate-sources
# or
npx nodemon --watch openApi/ --ext yml --exec "npm run generate"
```

## 📦 Publishing

### Quick Release

```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions will automatically:
# ✓ Build both clients
# ✓ Publish to npm
# ✓ Publish to Maven Central
# ✓ Create GitHub Release
```

### Manual Release via GitHub Actions

1. Go to **Actions** tab
2. Select **Release Packages**
3. Click **Run workflow**
4. Enter version (e.g., `1.0.0`)
5. Click **Run workflow**

### Publish Individual Packages

```bash
# npm only
git tag v1.0.0-npm
# Trigger "Publish npm Package" workflow manually

# Maven only
git tag v1.0.0-maven
# Trigger "Publish Maven Package" workflow manually
```

## 🧪 Testing

### Test Kotlin Client Locally

```kotlin
// Add to your test project's build.gradle.kts
dependencies {
    implementation(files("../hex-specs/target/hex-tractor-api-client-1.0.0.jar"))
}
```

### Test TypeScript Client Locally

```bash
# In hex-specs directory
npm link

# In your test project
npm link @hextractor/api-client
```

### Test Builds in CI

Push to any branch to trigger CI builds:

```bash
git push origin your-branch
```

## 📝 Common Tasks

### Update Version

```bash
# npm
npm version 1.2.3 --no-git-tag-version

# Maven
mvn versions:set -DnewVersion=1.2.3 -DgenerateBackupPoms=false
```

### Update Dependencies

```bash
# npm dependencies
npm update
npm outdated  # Check for updates

# Maven dependencies
mvn versions:display-dependency-updates
```

### Validate OpenAPI Spec

```bash
npm install -g @apidevtools/swagger-cli
swagger-cli validate openApi/hex-tractor-open-api.yml
```

### View Generated Code Structure

```bash
# Kotlin
tree generated/kotlin -L 3

# TypeScript
tree generated/typescript -L 2
```

## 🔍 Troubleshooting

### Build Fails

```bash
# Clear everything and rebuild
mvn clean
npm run clean
rm -rf generated/ dist/ target/ node_modules/
npm install
mvn clean package
npm run build
```

### GPG Issues (Local Signing)

```bash
# List keys
gpg --list-secret-keys --keyid-format LONG

# Test signing
echo "test" | gpg --clearsign
```

### Maven Central Not Showing Package

- Wait 2-4 hours after publishing
- Check https://central.sonatype.com/publishing/deployments
- Verify namespace is approved

### npm Import Errors

```bash
# Regenerate and rebuild
npm run clean
npm install
npm run build

# In consuming project
rm -rf node_modules package-lock.json
npm install
```

## 📊 Check Package Status

### npm Package

```bash
# View published versions
npm view @hextractor/api-client versions

# View latest version info
npm view @hextractor/api-client

# Download stats
npm view @hextractor/api-client
```

### Maven Package

```bash
# Search Maven Central
curl "https://search.maven.org/solrsearch/select?q=g:com.hextractor+AND+a:hex-tractor-api-client&rows=20&wt=json"

# Check specific version
curl "https://repo1.maven.org/maven2/com/hextractor/hex-tractor-api-client/1.0.0/"
```

## 🔐 Required Secrets (for Publishing)

Add to GitHub Settings → Secrets → Actions:

| Secret | Purpose |
|--------|---------|
| `NPM_TOKEN` | npm authentication |
| `OSSRH_USERNAME` | Maven Central username |
| `OSSRH_TOKEN` | Maven Central token |
| `GPG_PRIVATE_KEY` | Sign Maven artifacts |
| `GPG_PASSPHRASE` | GPG key password |

See [PUBLISHING.md](PUBLISHING.md) for detailed setup.

## 📚 Documentation Files

- **[README.md](README.md)** - Main documentation
- **[PUBLISHING.md](PUBLISHING.md)** - Complete publishing guide
- **[CHANGELOG.md](CHANGELOG.md)** - Version history
- **[openApi/hex-tractor-open-api.yml](openApi/hex-tractor-open-api.yml)** - API specification

## 🔗 Useful Links

- [OpenAPI Generator Docs](https://openapi-generator.tech/docs/generators)
- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [npm Publishing Docs](https://docs.npmjs.com/packages-and-modules/contributing-packages-to-the-registry)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Semantic Versioning](https://semver.org/)

## 💡 Tips

- Always update [CHANGELOG.md](CHANGELOG.md) before releasing
- Use semantic versioning (major.minor.patch)
- Test locally before publishing
- Pre-release versions: `1.0.0-alpha`, `1.0.0-beta.1`, `1.0.0-rc.1`
- Generated code is gitignored - only commit source files
- CI runs on every push/PR to validate builds

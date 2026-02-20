# Publishing Guide

This guide explains how to set up and publish your packages to npm and Maven Central using GitHub Actions.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Initial Setup](#initial-setup)
  - [npm Setup](#npm-setup)
  - [Maven Central Setup](#maven-central-setup)
  - [GitHub Secrets Configuration](#github-secrets-configuration)
- [Publishing Process](#publishing-process)
  - [Automated Release (Recommended)](#automated-release-recommended)
  - [Manual Publishing](#manual-publishing)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Before you can publish packages, you need:

1. A GitHub repository for this project
2. An npm account (https://www.npmjs.com/)
3. A Sonatype OSSRH account for Maven Central (https://central.sonatype.com/)
4. GPG key pair for signing Maven artifacts

## Initial Setup

### npm Setup

#### 1. Create an npm Account

If you don't have one already, sign up at https://www.npmjs.com/signup

#### 2. Generate an npm Access Token

1. Log in to npm: https://www.npmjs.com/
2. Click on your profile icon → Access Tokens
3. Click "Generate New Token" → "Classic Token"
4. Select "Automation" type (recommended for CI/CD)
5. Copy the generated token (you won't see it again!)

#### 3. Configure Package Scope

Your package is scoped as `@hextractor/api-client`. To publish scoped packages:

- **Public packages**: Free on npm
- **Private packages**: Requires npm Pro subscription

For public packages, the workflow uses `--access public` flag automatically.

If you want to change the scope or name:
1. Edit `package.json`: Change `name` field
2. Edit `.github/workflows/publish-npm.yml`: Update package references
3. Edit `.github/workflows/release.yml`: Update package references

### Maven Central Setup

#### 1. Create a Sonatype Account

1. Go to https://central.sonatype.com/
2. Sign up for an account
3. Verify your email address

#### 2. Claim Your Namespace

For the groupId `com.hextractor`:

1. Go to https://central.sonatype.com/publishing/namespaces
2. Click "Add Namespace"
3. Enter your namespace (e.g., `com.hextractor`)
4. Prove ownership by one of these methods:
   - **GitHub verification**: Add TXT record to GitHub repo
   - **Domain verification**: Add DNS TXT record to your domain
   - **GitHub Pages**: Add verification file

Follow the instructions provided by Sonatype for your chosen method.

#### 3. Generate GPG Key

Maven Central requires signed artifacts. Create a GPG key:

```bash
# Generate a new key
gpg --full-generate-key

# When prompted:
# - Key type: RSA and RSA (default)
# - Key size: 4096
# - Expiration: 0 (doesn't expire) or your preference
# - Real name: Your name
# - Email: your.email@example.com
# - Comment: Optional

# List your keys
gpg --list-secret-keys --keyid-format LONG

# Output will look like:
# sec   rsa4096/ABCD1234EFGH5678 2024-01-01 [SC]
#       1234567890ABCDEF1234567890ABCDEF12345678
# uid           [ultimate] Your Name <your.email@example.com>

# Export your private key (replace KEY_ID with your key ID)
gpg --armor --export-secret-keys KEY_ID > private-key.asc

# Export your public key
gpg --armor --export KEY_ID > public-key.asc

# Upload public key to key servers
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
gpg --keyserver keys.openpgp.org --send-keys KEY_ID
gpg --keyserver pgp.mit.edu --send-keys KEY_ID
```

**Important**: Keep your private key safe! Never commit it to git.

#### 4. Update pom.xml

Update the following placeholders in `pom.xml`:

```xml
<!-- Replace these values -->
<url>https://github.com/YOURUSERNAME/hex-specs</url>

<developers>
    <developer>
        <id>yourusername</id>
        <name>Your Name</name>
        <email>your.email@example.com</email>
    </developer>
</developers>

<scm>
    <connection>scm:git:git://github.com/YOURUSERNAME/hex-specs.git</connection>
    <developerConnection>scm:git:ssh://github.com/YOURUSERNAME/hex-specs.git</developerConnection>
    <url>https://github.com/YOURUSERNAME/hex-specs</url>
</scm>
```

### GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

**Settings → Secrets and variables → Actions → New repository secret**

#### Required Secrets

| Secret Name | Description | How to Get |
|------------|-------------|------------|
| `NPM_TOKEN` | npm access token | From npm (see npm setup above) |
| `OSSRH_USERNAME` | Sonatype username | Your Sonatype account username |
| `OSSRH_TOKEN` | Sonatype token | Generate at https://central.sonatype.com/account |
| `GPG_PRIVATE_KEY` | GPG private key | Content of `private-key.asc` (entire file including headers) |
| `GPG_PASSPHRASE` | GPG key passphrase | The passphrase you set when creating the GPG key |

#### Adding GPG_PRIVATE_KEY

The private key should include the full content:

```
-----BEGIN PGP PRIVATE KEY BLOCK-----

[key content]
-----END PGP PRIVATE KEY BLOCK-----
```

Copy the entire content from `private-key.asc` and paste it as the secret value.

## Publishing Process

### Automated Release (Recommended)

The easiest way to publish is using the combined release workflow:

#### Option 1: Using Git Tags (Recommended)

```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

This will automatically:
1. Validate the version format
2. Build both clients
3. Publish to npm Registry
4. Publish to Maven Central
5. Create a GitHub Release with download links

#### Option 2: Manual Workflow Trigger

1. Go to **Actions** tab in GitHub
2. Select **Release Packages** workflow
3. Click **Run workflow**
4. Enter the version (e.g., `1.0.0`)
5. Choose whether to create a git tag
6. Click **Run workflow**

### Manual Publishing

You can also publish each package separately:

#### Publish Only npm Package

```bash
git tag v1.0.0
git push origin v1.0.0
```

Then go to **Actions → Publish npm Package** and trigger manually, or the tag will trigger it automatically.

#### Publish Only Maven Package

```bash
git tag v1.0.0
git push origin v1.0.0
```

Then go to **Actions → Publish Maven Package** and trigger manually, or the tag will trigger it automatically.

### Version Formats

Supported version formats:
- Stable: `1.0.0`, `2.1.5`
- Pre-release: `1.0.0-alpha`, `1.0.0-beta.1`, `1.0.0-rc.2`

The workflow automatically marks pre-release versions (containing `-`) as pre-release on GitHub.

## Workflow Files

The project includes several GitHub Actions workflows:

- **`ci.yml`**: Runs on every push/PR to validate builds
- **`publish-npm.yml`**: Publishes to npm (can run standalone)
- **`publish-maven.yml`**: Publishes to Maven Central (can run standalone)
- **`release.yml`**: Combined workflow that publishes both packages

## Verification

After publishing, verify your packages:

### npm Package

```bash
npm view @hextractor/api-client
```

Or visit: https://www.npmjs.com/package/@hextractor/api-client

### Maven Package

Visit: https://central.sonatype.com/artifact/com.hextractor/hex-tractor-api-client

Or search: https://search.maven.org/search?q=g:com.hextractor

**Note**: It may take several hours (up to 24h) for packages to appear on Maven Central after publishing.

## Troubleshooting

### npm Publishing Issues

**Error: "You must be logged in to publish packages"**
- Verify `NPM_TOKEN` secret is set correctly
- Ensure the token has publish permissions
- Try regenerating the token

**Error: "Package name must be scoped"**
- Ensure package name starts with `@` in package.json
- Add `--access public` flag (already in workflow)

**Error: "Package already published"**
- You cannot republish the same version
- Increment the version number

### Maven Central Publishing Issues

**Error: "401 Unauthorized"**
- Verify `OSSRH_USERNAME` and `OSSRH_TOKEN` are correct
- Generate a new token at https://central.sonatype.com/account

**Error: "Failed to sign artifacts"**
- Verify `GPG_PRIVATE_KEY` contains the full key including headers
- Verify `GPG_PASSPHRASE` is correct
- Ensure the key hasn't expired

**Error: "Namespace not registered"**
- Complete the namespace registration process at Sonatype
- Wait for approval (can take a few days for first-time publishers)

**Error: "Missing required metadata"**
- Check that pom.xml includes: name, description, url, licenses, developers, scm
- Verify all sections are filled out correctly

### General Issues

**Workflow fails on "validate version"**
- Ensure version follows semantic versioning: X.Y.Z
- Check that the git tag matches the format `vX.Y.Z`

**GPG signing takes very long**
- This is normal for GPG operations in CI
- Typically takes 30-60 seconds per artifact

**Packages published but GitHub Release not created**
- Check GitHub token permissions
- Verify the workflow completed all jobs
- You can manually create a release from the GitHub UI

## Best Practices

1. **Use semantic versioning**: Follow X.Y.Z format
2. **Keep CHANGELOG.md updated**: Document changes for each release
3. **Test before releasing**: Use CI workflow to validate builds
4. **Tag releases**: Always create git tags for releases
5. **Verify publications**: Check both registries after publishing
6. **Secure your secrets**: Never commit tokens or keys to git
7. **Rotate credentials**: Periodically regenerate tokens and keys

## Additional Resources

- [npm Documentation](https://docs.npmjs.com/)
- [Maven Central Guide](https://central.sonatype.org/publish/publish-guide/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Semantic Versioning](https://semver.org/)
- [GPG Documentation](https://gnupg.org/documentation/)

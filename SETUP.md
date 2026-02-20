# Setup Guide for Publishing

This file provides a step-by-step checklist to help you set up publishing to npm and Maven Central.

## ✅ Pre-Publishing Checklist

### 1. Update Repository Information

- [ ] Update `pom.xml`:
  - [ ] Change `<url>` to your GitHub repository
  - [ ] Update `<developers>` section with your info
  - [ ] Update `<scm>` URLs with your repository URLs
  
- [ ] Update `package.json`:
  - [ ] Change `author` field
  - [ ] Add `repository` field (optional but recommended):
    ```json
    "repository": {
      "type": "git",
      "url": "git+https://github.com/yourusername/hex-specs.git"
    }
    ```
  
- [ ] Update `.github/dependabot.yml`:
  - [ ] Replace `yourusername` with your GitHub username

### 2. npm Setup

- [ ] Create npm account at https://www.npmjs.com/signup
- [ ] Generate npm access token (Automation type)
- [ ] Verify package name `@hextractor/api-client` is available (or change it)
- [ ] Add `NPM_TOKEN` to GitHub Secrets

### 3. Maven Central Setup

- [ ] Create Sonatype account at https://central.sonatype.com/
- [ ] Claim namespace `com.hextractor` (or use your own)
  - [ ] Complete verification (GitHub/Domain/etc.)
  - [ ] Wait for approval
- [ ] Generate User Token at https://central.sonatype.com/account
- [ ] Generate GPG key pair
  - [ ] Export public key
  - [ ] Upload to key servers
  - [ ] Export private key
- [ ] Add secrets to GitHub:
  - [ ] `OSSRH_USERNAME`
  - [ ] `OSSRH_TOKEN`
  - [ ] `GPG_PRIVATE_KEY`
  - [ ] `GPG_PASSPHRASE`

### 4. GitHub Setup

- [ ] Repository created
- [ ] All secrets added (see section 2 & 3)
- [ ] GitHub Actions enabled
- [ ] Branch protection rules configured (optional)

### 5. Documentation

- [ ] Update [README.md](README.md) with correct links
- [ ] Update [CHANGELOG.md](CHANGELOG.md) with initial version
- [ ] Review [PUBLISHING.md](PUBLISHING.md)
- [ ] Customize [QUICKREF.md](QUICKREF.md) if needed

### 6. Test Before Publishing

- [ ] Run `mvn clean package` successfully
- [ ] Run `npm run build` successfully
- [ ] Run CI workflow on a test branch
- [ ] Verify generated code is correct

### 7. First Release

- [ ] Update version to 1.0.0 in both files
- [ ] Update CHANGELOG.md
- [ ] Commit changes
- [ ] Create git tag: `git tag v1.0.0`
- [ ] Push: `git push origin v1.0.0`
- [ ] Watch GitHub Actions workflow
- [ ] Verify published packages

## 📋 GitHub Secrets Summary

All secrets go to: **Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Required For | Get From |
|------------|--------------|----------|
| `NPM_TOKEN` | npm publishing | https://www.npmjs.com/ → Access Tokens |
| `OSSRH_USERNAME` | Maven Central | Your Sonatype username |
| `OSSRH_TOKEN` | Maven Central | https://central.sonatype.com/account |
| `GPG_PRIVATE_KEY` | Maven Central | Your exported GPG private key |
| `GPG_PASSPHRASE` | Maven Central | GPG key passphrase |

## 🛠️ Quick Commands Reference

### Generate GPG Key
```bash
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --armor --export-secret-keys KEY_ID > private-key.asc
gpg --armor --export KEY_ID > public-key.asc
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

### Test Locally
```bash
# Kotlin
mvn clean package
java -jar target/hex-tractor-api-client-1.0.0.jar

# TypeScript
npm ci
npm run build
npm link
cd /path/to/test/project
npm link @hextractor/api-client
```

### First Release
```bash
# Update versions
vim package.json  # Set version to 1.0.0
vim pom.xml       # Set version to 1.0.0
vim CHANGELOG.md  # Document changes

# Commit and tag
git add .
git commit -m "Release version 1.0.0"
git tag v1.0.0
git push origin main
git push origin v1.0.0
```

## 🔍 Verification After Publishing

### Check npm
```bash
npm view @hextractor/api-client
# Or visit: https://www.npmjs.com/package/@hextractor/api-client
```

### Check Maven Central
```bash
# Wait 2-4 hours, then visit:
# https://central.sonatype.com/artifact/com.hextractor/hex-tractor-api-client
# Or: https://search.maven.org/search?q=g:com.hextractor
```

### Check GitHub Release
```bash
# Visit: https://github.com/yourusername/hex-specs/releases
```

## ❓ Common Issues

### "Namespace not registered" (Maven Central)
- Complete namespace verification at Sonatype
- This can take 1-3 business days for first-time publishers
- Check status at https://central.sonatype.com/publishing/namespaces

### "Package name already taken" (npm)
- Change the package name in package.json
- Or request transfer if you own it
- Scoped packages: `@yourorg/package-name`

### "GPG signing failed"
- Verify private key includes headers
- Check passphrase is correct
- Ensure key hasn't expired: `gpg --list-keys`

### "401 Unauthorized" (Maven Central)
- Regenerate Sonatype User Token
- Verify token permissions
- Check OSSRH_USERNAME is correct (not email)

## 📚 Additional Resources

- [PUBLISHING.md](PUBLISHING.md) - Complete publishing guide
- [QUICKREF.md](QUICKREF.md) - Quick command reference
- [Maven Central Guide](https://central.sonatype.org/publish/publish-guide/)
- [npm Publishing Docs](https://docs.npmjs.com/packages-and-modules/contributing-packages-to-the-registry)

## ✨ Tips

1. **Start with a test release**: Use version `0.1.0-test` to verify everything works
2. **Backup your GPG key**: Store it securely, you'll need it for future releases
3. **Use pre-release versions**: Test with `-alpha`, `-beta`, `-rc` versions first
4. **Document changes**: Keep CHANGELOG.md up to date
5. **Automate everything**: Let GitHub Actions handle the publishing

## 🎉 Ready to Publish?

Once you've completed all checklist items above:

1. Commit all changes
2. Create a version tag: `git tag v1.0.0`
3. Push: `git push origin v1.0.0`
4. Watch GitHub Actions do its magic! ✨

The workflow will automatically:
- ✅ Generate both clients
- ✅ Build and test
- ✅ Sign Maven artifacts
- ✅ Publish to npm Registry
- ✅ Publish to Maven Central
- ✅ Create GitHub Release

Good luck! 🚀

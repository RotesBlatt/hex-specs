# Hex-Tractor API Client Generation

This project generates API clients from the OpenAPI specification for both Kotlin Multiplatform and TypeScript/Node.js applications.

## Prerequisites

### For Kotlin Generation
- Java 11 or higher
- Maven 3.6 or higher

### For TypeScript Generation
- Node.js 18 or higher
- npm 9 or higher

## Project Structure

```
hex-specs/
├── openApi/
│   └── hex-tractor-open-api.yml          # OpenAPI specification
├── generated/                             # Generated code (gitignored)
│   ├── kotlin/                            # Generated Kotlin sources
│   └── typescript/                        # Generated TypeScript sources
├── dist/                                  # Compiled TypeScript (gitignored)
├── pom.xml                                # Maven configuration
├── package.json                           # npm configuration
├── tsconfig.json                          # TypeScript configuration
├── PUBLISHING.md                          # Publishing guide
├── CHANGELOG.md                           # Version history
└── README.md                              # This file
```

## 📦 Published Packages

This project is published to both npm and Maven Central:

- **npm**: [@hextractor/api-client](https://www.npmjs.com/package/@hextractor/api-client)
- **Maven Central**: [com.hextractor:hex-tractor-api-client](https://central.sonatype.com/artifact/com.hextractor/hex-tractor-api-client)

For publishing your own version, see [PUBLISHING.md](PUBLISHING.md) for complete setup instructions.

## Kotlin Multiplatform Client

### Generate Kotlin Code

```bash
mvn clean generate-sources
```

This will generate Kotlin client code in the `generated/kotlin` directory with the following packages:
- `com.hextractor.api` - API interfaces
- `com.hextractor.api.model` - Data models
- `com.hextractor.api.client` - Client infrastructure

### Build the Kotlin Library

```bash
mvn clean package
```

This will compile the generated Kotlin code and create a JAR file in the `target` directory.

### Using in Your Kotlin Multiplatform Project

Add the generated library as a dependency in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("path/to/hex-tractor-api-client-1.0.0.jar"))
    
    // Required dependencies
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
```

Or publish to your local Maven repository:

```bash
mvn clean install
```

Then add as a regular Maven dependency:

```kotlin
dependencies {
    implementation("com.hextractor:hex-tractor-api-client:1.0.0")
}
```

### Example Kotlin Usage

```kotlin
import com.hextractor.api.client.ApiClient
import com.hextractor.api.AccountApi
import io.ktor.client.*
import io.ktor.client.engine.cio.*

suspend fun main() {
    val client = HttpClient(CIO)
    val apiClient = ApiClient(basePath = "http://localhost:3000/api", httpClient = client)
    val accountApi = AccountApi(apiClient)
    
    try {
        val account = accountApi.accountRegionSummonerNameTagLineGet(
            region = "euw1",
            summonerName = "PlayerName",
            tagLine = "EUW"
        )
        println("Account: $account")
    } finally {
        client.close()
    }
}
```

## TypeScript/Node.js Client

### Install Dependencies

```bash
npm install
```

### Generate TypeScript Code

```bash
npm run generate
```

This will generate TypeScript client code in the `generated/typescript` directory.

### Build the TypeScript Library

```bash
npm run build
```

This will:
1. Generate the TypeScript code from the OpenAPI spec
2. Compile the TypeScript to JavaScript
3. Create type definitions
4. Output everything to the `dist` directory

### Clean Generated Files

```bash
npm run clean
```

### Using in Your Node.js/TypeScript Project

You can use the generated client in several ways:

#### Option 1: Link Locally During Development

```bash
# In the hex-specs directory
npm link

# In your Node.js project
npm link @hextractor/api-client
```

#### Option 2: Install from File

```bash
npm install path/to/hex-specs
```

#### Option 3: Publish to npm Registry

```bash
npm publish
```

### Example TypeScript Usage

```typescript
import { Configuration, AccountApi, MatchesApi } from '@hextractor/api-client';

const config = new Configuration({
  basePath: 'http://localhost:3000/api'
});

const accountApi = new AccountApi(config);
const matchesApi = new MatchesApi(config);

async function getAccountInfo() {
  try {
    const account = await accountApi.accountRegionSummonerNameTagLineGet(
      'euw1',
      'PlayerName',
      'EUW'
    );
    console.log('Account:', account.data);
    
    const matches = await matchesApi.matchesRegionPuuidGet(
      'euw1',
      account.data.generalInfo.puuid,
      0,
      10
    );
    console.log('Matches:', matches.data);
  } catch (error) {
    console.error('Error:', error);
  }
}

getAccountInfo();
```

### Example JavaScript Usage (CommonJS)

```javascript
const { Configuration, AccountApi } = require('@hextractor/api-client');

const config = new Configuration({
  basePath: 'http://localhost:3000/api'
});

const accountApi = new AccountApi(config);

accountApi.accountRegionSummonerNameTagLineGet('euw1', 'PlayerName', 'EUW')
  .then(response => {
    console.log('Account:', response.data);
  })
  .catch(error => {
    console.error('Error:', error);
  });
```

## Configuration

### Kotlin Configuration

The Kotlin client is configured in [pom.xml](pom.xml). Key settings:

- **Generator**: `kotlin` (multiplatform support)
- **Library**: `multiplatform` (works with JVM, JS, and Native)
- **Serialization**: `kotlinx_serialization`
- **HTTP Client**: Ktor
- **Packages**:
  - API: `com.hextractor.api`
  - Models: `com.hextractor.api.model`
  - Invoker: `com.hextractor.api.client`

### TypeScript Configuration

The TypeScript client is configured in [package.json](package.json) and [openapi-generator-typescript-config.json](openapi-generator-typescript-config.json). Key settings:

- **Generator**: `typescript-axios`
- **HTTP Client**: Axios
- **ES6 Support**: Enabled
- **Interfaces**: Enabled
- **Single Request Parameter**: Enabled

## Customization

### Modify Kotlin Generation

Edit [pom.xml](pom.xml) in the `openapi-generator-maven-plugin` configuration section to change:
- Package names
- Output directory
- Generator options
- Dependencies

### Modify TypeScript Generation

Edit the `generate` script in [package.json](package.json) or modify [openapi-generator-typescript-config.json](openapi-generator-typescript-config.json) to change:
- Generator options
- Output directory
- Additional properties

## Troubleshooting

### Kotlin Issues

**Problem**: Build fails with serialization errors
```bash
# Solution: Ensure kotlinx-serialization plugin is properly configured
mvn clean compile
```

**Problem**: Multiplatform compatibility issues
```bash
# Solution: Check that your target platform is supported by Ktor
# Update library version in pom.xml if needed
```

### TypeScript Issues

**Problem**: Module not found errors
```bash
# Solution: Regenerate and rebuild
npm run clean
npm run build
```

**Problem**: Type definition errors
```bash
# Solution: Check tsconfig.json and ensure all paths are correct
npm install
npm run build
```

## API Endpoints

The generated clients provide access to the following endpoints:

- **Account API**: Get Riot account information by summoner name/tag or PUUID
- **Leagues API**: Get league entries by queue, tier, and division
- **Matches API**: Get match details and match IDs by PUUID or match ID

Refer to [hex-tractor-open-api.yml](openApi/hex-tractor-open-api.yml) for detailed API documentation.

## License

MIT

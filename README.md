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
├── OPENAPI_ZOD_CLIENT.md                  # Zod client documentation
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

The TypeScript client is generated with [openapi-zod-client](https://github.com/astahmer/openapi-zod-client), providing **runtime validation** of API requests and responses using Zod schemas. This gives you:

- ✅ Runtime type safety with Zod validation
- ✅ Compile-time type safety with TypeScript
- ✅ Smaller bundle size (single file output)
- ✅ Better developer experience with automatic schema validation

See [OPENAPI_ZOD_CLIENT.md](OPENAPI_ZOD_CLIENT.md) for detailed documentation.

### Install Dependencies

```bash
npm install
```

### Generate TypeScript Code

```bash
npm run generate
```

This will generate a TypeScript client with Zod schemas in `generated/typescript/api.ts`.

### Build the TypeScript Library

```bash
npm run build
```

This will:
1. Generate the TypeScript client with Zod schemas from the OpenAPI spec
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
import { makeApi } from '@hextractor/api-client';
import Axios from 'axios';

// Create axios instance with base URL
const axios = Axios.create({ 
  baseURL: 'http://localhost:3000/api' 
});

// Create type-safe API client
const api = makeApi(axios);

async function getAccountInfo() {
  try {
    // All requests and responses are validated with Zod schemas
    const account = await api.accountRegionSummonerNameTagLineGet({
      params: {
        region: 'euw1',
        summonerName: 'PlayerName',
        tagLine: 'EUW'
      }
    });
    console.log('Account:', account);
    
    const matches = await api.matchesRegionPuuidGet({
      params: {
        region: 'euw1',
        puuid: account.generalInfo.puuid,
        start: 0,
        count: 10
      }
    });
    console.log('Matches:', matches);
  } catch (error) {
    // Zod validation errors or API errors
    console.error('Error:', error);
  }
}

getAccountInfo();
```

### Example JavaScript Usage (CommonJS)

```javascript
const { makeApi } = require('@hextractor/api-client');
const Axios = require('axios');

const axios = Axios.create({ 
  baseURL: 'http://localhost:3000/api' 
});

const api = makeApi(axios);

api.accountRegionSummonerNameTagLineGet({
  params: {
    region: 'euw1',
    summonerName: 'PlayerName',
    tagLine: 'EUW'
  }
})
  .then(account => {
    console.log('Account:', account);
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

The TypeScript client is configured in [package.json](package.json). Key features:

- **Generator**: `openapi-zod-client` (runtime validation)
- **Validation**: Zod schemas for runtime type checking
- **HTTP Client**: Axios (you provide your own instance)
- **Output**: Single file (`generated/typescript/api.ts`)
- **TypeScript**: Strict mode with ES2020 target

See [OPENAPI_ZOD_CLIENT.md](OPENAPI_ZOD_CLIENT.md) for detailed documentation on using the Zod-based client.

## Customization

### Modify Kotlin Generation

Edit [pom.xml](pom.xml) in the `openapi-generator-maven-plugin` configuration section to change:
- Package names
- Output directory
- Generator options
- Dependencies

### Modify TypeScript Generation

Edit the `generate` script in [package.json](package.json) to change:
- Output file location (`-o` flag)
- Additional openapi-zod-client options

See the [openapi-zod-client documentation](https://github.com/astahmer/openapi-zod-client) for all available options.

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

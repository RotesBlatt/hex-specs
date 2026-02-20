# OpenAPI Zod Client Configuration

This project uses `openapi-zod-client` to generate TypeScript clients with Zod schemas for runtime validation.

## Generated Output

The generator creates a single file: `generated/typescript/api.ts` containing:
- Zod schemas for all API models
- Type-safe API client functions
- Runtime validation for requests and responses

## Configuration Options

You can customize generation by adding options to the generate script in `package.json`:

```bash
openapi-zod-client ./openApi/hex-tractor-open-api.yml -o ./generated/typescript/api.ts [options]
```

### Available Options

- `--template` - Use a custom Handlebars template
- `--options-file` - Path to a JSON config file
- `--api-client` - API client to use (default: axios)
- `--base-url` - Base URL for the API
- `--with-docs` - Include JSDoc comments
- `--group-strategy` - How to group operations (default: none)

## Example Usage

After generation, use the client like this:

```typescript
import { makeApi } from './generated/typescript/api';
import Axios from 'axios';

const axios = Axios.create({ baseURL: 'http://localhost:3000/api' });
const api = makeApi(axios);

// Type-safe API call with runtime validation
const result = await api.accountRegionSummonerNameTagLineGet({
  params: {
    region: 'euw1',
    summonerName: 'PlayerName',
    tagLine: 'EUW'
  }
});

// Result is validated against Zod schema
console.log(result);
```

## Benefits

1. **Runtime Validation**: Zod schemas validate API responses at runtime
2. **Type Safety**: Full TypeScript type inference
3. **Developer Experience**: Auto-completion in IDEs
4. **Error Handling**: Clear validation errors if API contract changes
5. **Smaller Bundle**: Single file output vs. multiple files from traditional generators

## Documentation

- [openapi-zod-client GitHub](https://github.com/astahmer/openapi-zod-client)
- [Zod Documentation](https://zod.dev/)

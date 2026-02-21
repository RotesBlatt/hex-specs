#!/usr/bin/env node

/**
 * Automatically generate TypeScript API clients for all OpenAPI specs in the openApi directory
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const openApiDir = path.join(__dirname, 'openApi');
const outputDir = path.join(__dirname, 'generated', 'typescript');

// Ensure output directory exists
if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

// Find all .yml and .yaml files in openApi directory
const openApiFiles = fs.readdirSync(openApiDir)
    .filter(file => file.endsWith('.yml') || file.endsWith('.yaml'));

if (openApiFiles.length === 0) {
    console.warn('⚠️  No OpenAPI spec files found in openApi directory');
    process.exit(0);
}

console.log(`📦 Found ${openApiFiles.length} OpenAPI spec(s)`);

// Generate client for each OpenAPI file
openApiFiles.forEach(file => {
    const inputPath = path.join(openApiDir, file);

    // Derive output filename from input
    // e.g., "hex-tractor-open-api.yml" -> "hex-tractor-api.ts"
    const baseName = path.basename(file, path.extname(file))
        .replace('-open-api', '')
        .replace('_open_api', '');
    const outputFile = path.join(outputDir, `${baseName}-api.ts`);

    console.log(`🔨 Generating ${baseName}-api.ts from ${file}...`);

    try {
        execSync(
            `npx openapi-zod-client "${inputPath}" --export-schemas --export-types -o "${outputFile}"`,
            { stdio: 'inherit' }
        );
        console.log(`✅ Generated ${baseName}-api.ts`);
    } catch (error) {
        console.error(`❌ Failed to generate ${baseName}-api.ts:`, error.message);
        process.exit(1);
    }
});

// Create index file that exports all generated APIs with namespaces
const indexContent = openApiFiles
    .map(file => {
        const baseName = path.basename(file, path.extname(file))
            .replace('-open-api', '')
            .replace('_open_api', '');

        // Convert to PascalCase for namespace (e.g., "hex-tractor" -> "HexTractor")
        const namespaceName = baseName.split('-').map(part =>
            part.charAt(0).toUpperCase() + part.slice(1).toLowerCase()
        ).join('');

        return `export * as ${namespaceName} from './${baseName}-api';`;
    })
    .join('\n');

const indexPath = path.join(outputDir, 'index.ts');
fs.writeFileSync(indexPath, indexContent + '\n');
console.log(`📝 Created index.ts with namespaced exports`);

console.log('✨ All API clients generated successfully!');

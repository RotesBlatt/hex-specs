# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Repo Does

This is a **specs-and-codegen** repository for the Hex-Tractor application. It holds OpenAPI specifications and generates typed API clients from them in two target languages:

- **TypeScript** via [`openapi-zod-client`](https://github.com/astahmer/openapi-zod-client) — produces Zod-validated clients backed by Axios/Zodios
- **Kotlin Multiplatform** via Gradle's `org.openapi.generator` plugin — targets Android and iOS (arm64, x64, simulatorArm64)

The generated code is **not committed**; it is produced at build time and published to package registries.

## OpenAPI Specs

All specs live in `openApi/`. Both the TypeScript and Kotlin generators auto-discover every `*.yml`/`*.yaml` file there, so dropping a new spec file is enough for it to be included in the next build — no other configuration changes needed.

### `hex-tractor-open-api.yml` — Hex-Tractor Backend API

The primary spec, base URL `http://localhost:3000/api`. All endpoints (except auth registration and the challenge fetch) require the four-header device signature scheme.

**Account** — look up a Riot account by summoner name + tag line or by PUUID, scoped to a region.

**Matches**
- Get a single match by match ID
- Get a list of match IDs for a player (paginated, with optional queue filter)
- Get full match objects for a player (paginated, with optional `queueId` filter)

**Leagues** — paginated leaderboard for a specific region / queue (`RANKED_SOLO_5x5` or `RANKED_FLEX_SR`) / tier / division combination.

**Stats** — champion aggregate statistics, all scoped to region + patch + tier + queue. The stat types are:
| Endpoint | `operationId` | What it returns |
|---|---|---|
| `GET /stats/champions` | `getChampionStatsByRegionPatchTier` | Aggregate pick/win/ban rates for all champions |
| `GET /stats/champions/{championId}` | `getChampionStatsDetailByRegion` | Same metrics for a single champion |
| `GET /stats/champions/{championId}/matchups` | `getChampionMatchupsByRegion` | Directional head-to-head stats (A vs B ≠ B vs A); filterable by lane |
| `GET /stats/champions/{championId}/synergy` | `getChampionSynergyByRegion` | Per-duo winrate when two champions are on the same team |
| `GET /stats/champions/performance` | `getChampionPerformanceTableByRegion` | Per-minute gold/XP/CS performance table for all champions |
| `GET /stats/champions/{championId}/performance` | `getChampionPerformanceDetailByRegion` | Same, single champion |
| `GET /stats/champions/objectives` | `getChampionObjectiveStatsByRegion` | Objective contribution rates (dragon, baron, etc.) |
| `GET /stats/champions/{championId}/game-length` | `getChampionGameLengthStatsByRegion` | Winrate segmented by game-length buckets |
| `GET /stats/champions/{championId}/early-game` | `getChampionEarlyGameStatsByRegion` | Early-game indicators (gold diff, CS diff at 10 min) |

**PlayerStats** — async job system for per-player computed stats. `POST /players/{puuid}/stats/request` enqueues a job (or returns a cached result immediately with HTTP 200; returns HTTP 202 with `jobId` if the job is new or already running). `GET /players/{puuid}/stats/{jobId}` polls for the result. Job types: `champions`, `teammates`, `opponents`, `timeline`.

**Player** — `GET /players/autocomplete` — prefix search for player names within a region (case-insensitive, max 50 results).

**Authentication** — two unauthenticated endpoints used for device onboarding:
- `POST /auth/devices/register` — registers a device and returns a `deviceId`
- `GET /auth/challenge/{deviceId}` — issues a nonce the client signs to produce request signatures

Key shared schemas: `Region` (e.g. `euw1`, `na1`, `kr`), `RankedTier`, `RankedDivision`, `RankedQueue`, `QueueId`, `Lane`. All success responses follow a `{ success, data, meta }` envelope pattern.

---

### `riot-games-open-api.yml` — Riot Games API

A comprehensive mirror of the official Riot Games developer API, used as a reference or for direct passthrough calls. Covers:

- **Account v1** — account lookup by PUUID, Riot ID, or access token; active shard/region queries
- **Champion Mastery v4** — mastery entries and scores by PUUID
- **Champion v3** — champion rotation (free-to-play)
- **Clash v1** — players, teams, and tournaments
- **League v4 / League-Exp v4** — league entries by PUUID, challenger/grandmaster/master leagues, full league by ID
- **LoL Challenges v1** — challenge configs, leaderboards, and player challenge data
- **LoL RSO Match v1** — match IDs and full match objects via RSO (includes custom matches)
- **Match v5** — standard match IDs, match details, and timelines by PUUID
- **Spectator v5 / TFT Spectator v5** — live game info by PUUID
- **Summoner v4 / TFT Summoner v1** — summoner lookup by PUUID or access token
- **TFT League v1** — TFT-specific league entries, challenger/grandmaster/master, top-rated ladder
- **TFT Match v1** — TFT match IDs and match objects
- **LoR** (Legends of Runeterra) — decks, card inventory, match history, ranked leaderboards
- **Valorant** — matches, matchlists, ranked leaderboard, content, console variants
- **Riftbound Content v1** — Riftbound game content
- **Tournament v5 / Tournament Stub v5** — tournament code management
- **Status v4** — platform status for LoL, TFT, LoR

## Commands

### TypeScript

```bash
npm install          # install dependencies
npm run generate     # generate TypeScript clients from all OpenAPI specs → generated/typescript/
npm run build        # generate + tsc compile → dist/
npm run clean        # delete generated/typescript/ and dist/
```

### Kotlin Multiplatform

```bash
./gradlew.bat build                        # generate + compile all targets
./gradlew.bat generateHextractorApi        # generate Kotlin sources only (hex-tractor spec)
./gradlew.bat createDeploymentBundle       # build + create Maven Central zip in build/distributions/
```

Generated Kotlin sources land in `build/generated/<taskName>/src/main/kotlin` (not in `generated/kotlin/` — the README is outdated on this point).

## Published Packages

| Registry | Artifact |
|---|---|
| npm | `@rotesblatt/hex-tractor-data-api` |
| Maven Central | `io.github.rotesblatt:hex-tractor-api-client` |

Publishing is triggered automatically by pushing a semver git tag (`vX.Y.Z`). The GitHub Actions workflows handle building, signing (GPG), and uploading to both registries. Required secrets: `NPM_TOKEN`, `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.

## Architecture Notes

### TypeScript client shape

`generate-apis.js` runs `openapi-zod-client` for each spec file, outputting `generated/typescript/<name>-api.ts`, then writes an `index.ts` that re-exports each file under a PascalCase namespace (e.g. `HexTractor`). `index.ts` at the project root re-exports from `generated/typescript/index.ts`.

The generated client exports a `makeApi(axios)` factory. Callers supply their own Axios instance configured with a `baseURL`; the client validates every request/response against the generated Zod schemas at runtime.

### Kotlin client shape

`build.gradle.kts` dynamically creates one `GenerateTask` per spec file. The generator name is `kotlin` with `library=multiplatform`. Each spec gets its own package namespace derived from the filename (e.g. `io.hextractor.*`). All generation tasks are wired as `dependsOn` for every Kotlin compilation task so the build is always up to date.

The Kotlin targets are Android (minSdk 24, JVM 17) and iOS. HTTP transport is Ktor; serialization is `kotlinx-serialization`.

### Authentication model

The API uses a four-header device-based auth scheme: `DeviceId`, `Nonce`, `Timestamp`, and `Signature` (defined as security schemes in the spec). This is distinct from standard bearer/API-key auth and must be reflected in any new endpoints added to the spec.

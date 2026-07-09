# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Referee-Coach ProBasket is a web application for coaching basketball referees: coaches create video-based
reports/reviews of referees for games, referees discuss them, and results are exported. It is a single Maven
project bundling a **Spring Boot** backend and an **Angular** frontend that is built and served together (Angular
compiles into `target/webapp` and is packaged into the Spring Boot jar). Deployed to Heroku.

## Tech Stack

- Backend: Java 25, Spring Boot 4.1, jOOQ (type-safe SQL), Flyway (migrations), PostgreSQL, Spring Security (OAuth2
  resource server / JWT), Apache POI (Excel export), Lombok, ArchUnit.
- Frontend: Angular 22 (standalone components + signals), Angular Material, RxJS, Luxon. Source in `src/main/webapp`.
- The build runs jOOQ code generation, TypeScript-from-Java DTO generation, and the Angular build via Maven plugins.

## Common Commands

Backend / full build (use the Maven wrapper `./mvnw`):

- `./mvnw verify` — full build incl. tests, ArchUnit, and the frontend build.
- `./mvnw test -Dtest=YouTubeUtilTest` — run a single test class (`#methodName` for a single method).
- `./mvnw spring-boot:run` — run the app locally (needs the DB running; see below). Backend on port 8080.

Frontend (run in repo root; `npm install` first):

- `npm start` — `ng serve` with proxy to backend (`proxy-config.json` routes `/api` → `localhost:8080`).
- `npm run build` — production Angular build.
- `npm run lint` / `npm run lint-fix` — ESLint (`eslint.config.js`, angular-eslint).

Database (Docker required):

- Start local Postgres: `docker compose -p referee-coach-probasket -f src/main/docker/postgres.yml up --build`
  (prefix with the matching `... down &&` to reset). DB: `probasket/probasket` at `localhost:5432`.

## Code Generation (important)

Two layers of generated code must be regenerated when their sources change. Both are committed to the repo.

1. **jOOQ classes** (`ch.refereecoach.probasket.jooq`) are generated from the live DB schema via Testcontainers.
   Run `./mvnw clean test-compile -Djooq-codegen-skip=false` (Docker must be running) after any Flyway migration
   change. Codegen is skipped by default (`jooq-codegen-skip=true`).
2. **TypeScript API types** (`src/main/webapp/rest.ts`) are generated from Java `*DTO` classes via
   `./mvnw typescript-generator:generate`. Run this after changing any DTO so the frontend types stay in sync.

There are also IntelliJ run configs in `.run/` for these tasks.

## Architecture

Backend package layout under `ch.refereecoach.probasket`, with a **layered architecture enforced by
`ArchUnitTest`** (`src/test/java/.../ArchUnitTest.java`):

- `rest` — `@RestController` endpoints under `/api/...`, secured with `@Secured({"ROLE_..."})`; current user via
  `@AuthenticationPrincipal Jwt jwt`. May not be accessed by any other layer.
- `service` — all business logic; only accessible from `rest` and `configuration`. Sub-packages by domain
  (`report`, `basketplan`, `admin`, `auth`, `export`, `mail`).
- `dto` — Java `record` DTOs with `jakarta.validation` constraints. These drive `rest.ts` generation.
- `jooq` — generated, only accessible from `service`.
- `configuration`, `common`, `util`.

External integration: **Basketplan** (Swiss basketball system) — see the Basketplan Integration section below.

ArchUnit also enforces: no `LocalDate/Time.now()` without zone (use the project `DateUtil`), no jOOQ
`ResultQuery.stream()`, no field injection, no generic exceptions, no `System.out/err`, tests in the same package as
the class under test. Keep these in mind — they fail the build.

### jOOQ conventions

Use `DSLContext` (`jooqDsl`) for all queries; no raw SQL. Prefer `multiset` + `mapping` for nested/aggregated
reads (see `service/report/ReportSearchService.java` for the canonical example).

### Angular conventions

Standalone components only (no `NgModule`); signals for state (`signal`/`computed`/`effect`/`input()`/`output()`);
`ChangeDetectionStrategy.OnPush`; native control flow (`@if`/`@for`/`@switch`); `inject()` over constructor
injection; `host` property instead of `@HostBinding`/`@HostListener`; Reactive Forms; Angular Material + `<mat-icon>`.
Feature folders live in `src/main/webapp/app` (`overview`, `edit`, `view`, `discuss`, `export`, `admin`, `login`,
`tag-search`, `tag-selection`, plus shared `components`). Call the backend using the generated `rest.ts` types.

## Basketplan Integration

[Basketplan](https://www.basketplan.ch) is the external Swiss basketball management system and the source of truth
for games and referees. All integration lives in `service/basketplan` and calls Basketplan's HTTP endpoints via the
reactive `WebClient` (injected as `WebClient.Builder`). Responses are **XML** and parsed with the JAXP DOM API
(`DocumentBuilderFactory` with secure processing on) using helpers in `util/XmlUtil`; the Asport call is the one
JSON response. There is no client SDK — URLs are string-formatted constants in each service.

Config comes from `ApplicationProperties` (prefix `probasket`): `basketplan-api-key` (`BASKETPLAN_API_KEY` env var,
sent as the `refApiKey` header) and `federation-id` (default `10`).

Three services:

- **`BasketplanAuthenticationService`** — logs a user in against Basketplan (`authorizeUserXML.do`). Password is sent
  MD5-hashed; on success returns the Basketplan `personId`, which is used as the local user id. This backs the
  app's login (users authenticate with their Basketplan credentials).
- **`BasketplanUserSyncService`** — `@Scheduled` every 24h (and at startup): pulls referee master data
  (`showRefereeDataXML.do`) for the federation and upserts them into the `login` table (name, email, active flag,
  `Rank` derived from `highestRefereeQualificationId`). Referees no longer returned are deactivated (admins are
  never deactivated). The local user id equals the Basketplan referee id.
- **`BasketplanGameService`** — looks up a single game by number (`showSearchGames.do`, exposed via
  `GET /api/basketplan/{gameNumber}`, secured `REFEREE_COACH`/`TRAINER_COACH`). Maps teams, result, officiating mode
  (2PO/3PO), and resolves the game's referees to local `UserDTO`s. Video URL comes from Basketplan's `videoLink`,
  falling back to a lookup against the **Asport** video platform (`manager.asport.tv`, JSON) by Basketplan game
  number.

When touching this code, note the ArchUnit rules still apply (e.g. use `DateUtil.today()` not `LocalDate.now()`),
and failures are logged and swallowed into an empty `Optional`/no-op rather than thrown.

## Database Migrations

Every schema change requires a new `V<n>__description.sql` file in `src/main/resources/db/migration`. Flyway applies
them on startup. After adding a migration, regenerate jOOQ code (see above).

## CI / Release

- CI: `.github/workflows/maven.yml` (`./mvnw verify sonar:sonar`) and `angular.yml`, on `main` and `develop`.
- Work happens on `develop`; production installs merge a release tag into `main`.
- Release: `npm run release` (release-it) → answer prompts (usually patch / commit / tag / push). To deploy,
  `git merge <TAG>` into `main`.

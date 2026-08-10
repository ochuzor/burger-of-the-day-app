# Burger of the Day — Project Brief

## Product purpose

Build a small Spring Boot API inspired by the "Burger of the Day" board from
*Bob's Burgers*. Users submit short fictional burger-board ideas for today or
a future date, and published ideas are publicly readable.

This is both a usable application and a portfolio learning project. Its primary
learning purpose is to apply the completed Java and Spring Boot curricula in a
project designed and implemented from scratch.

## Intended users

Fans can publish and browse fictional Burger of the Day ideas. Viewing
published ideas does not require an account. During the first version, seeded
users are identified by a temporary `X-Username` request header; this is
explicitly not production authentication.

## Scope and exclusions

### Current scope

- Create a Burger of the Day scheduled for today or a future date.
- Let only its creator view and edit it before its publication date.
- Make it public and immutable when its publication date arrives.
- Let its creator hide and unhide it after publication.
- List public burgers by creator and publication date.
- Use one application-wide UTC business date.

### Current exclusions

Until explicitly brought into scope:

- Production authentication, registration, and password handling
- A browser or mobile frontend
- Voting, winners, and comments
- Tags and browsing or searching published posts by tag
- Microservices or distributed messaging
- Cloud deployment infrastructure
- External databases or third-party APIs
- Premature caching, scheduling, or background processing
- Features not required by the agreed MVP

These exclusions are guardrails, not permanent prohibitions. Change them only
through an explicit project decision recorded below.

## Current architecture

The repository currently contains a generated, minimal Spring Boot application:

- Java 21
- Maven Wrapper and Maven build
- Spring Boot 4.1.0
- Spring MVC
- Jakarta Validation
- Base package: `com.ochuzor.burgeroftheday`
- Application entry point: `BurgerOfTheDayApplication`
- One Spring context-load test
- Application name: `burger-of-the-day`
- Local HTTP port: `8081`

The create request DTO and its focused Jakarta Validation tests exist. The
initial PostgreSQL Flyway migration, JPA entities, and Spring Data repositories
for `app_user` and `burger_of_the_day` exist. No controller, service, or
completed HTTP feature exists yet.

## Completed features

No product features are complete.

Repository setup completed so far:

- Spring Boot project scaffold created.
- Maven Wrapper added.
- Spring MVC and Jakarta Validation dependencies selected.
- Basic context-load test created.
- Application configured to run on port 8081.
- Durable mentoring instructions and this project brief added.
- Create-request validation implemented and tested, including boundary cases.
- Initial PostgreSQL schema migration drafted and statically reviewed.
- Local PostgreSQL 18 Compose service configured and verified healthy, including
  application-role connectivity and persistent storage.
- Spring Data JPA, Flyway, Flyway PostgreSQL support, and the PostgreSQL JDBC
  driver added with Spring Boot-managed versions; packaging succeeds.
- Datasource configuration reads local credentials from environment variables.
- Flyway migration version 1 executed successfully against PostgreSQL 18;
  `app_user`, `burger_of_the_day`, and `flyway_schema_history` were verified.
- The full nine-test Maven suite passes with the local database environment.
- The Flyway-managed `app_user` table is mapped as a `User` JPA entity with
  application-generated UUID identity; Hibernate schema validation passes.
- Spotless 3.9.0 with Google Java Format is pinned, formats main and test Java
  sources, and enforces `spotless:check` during Maven `verify`.
- The Flyway-managed `burger_of_the_day` table is mapped as a
  `BurgerOfTheDay` JPA entity with database-generated numeric identity and a
  required lazy many-to-one creator relationship; schema validation passes.
- `UserRepository` and `BurgerOfTheDayRepository` are registered as Spring Data
  JPA repositories; application startup discovers both interfaces.
- A focused PostgreSQL `@DataJpaTest` verifies generated user and burger IDs,
  persisted burger fields, default visibility, and the creator relationship.
  Clearing the persistence context before retrieval ensures the assertions
  exercise actual database reads.
- `BurgerOfTheDayService` implements creator lookup, UTC publication-date
  validation, timestamp creation through an injected `Clock`, and transactional
  persistence. A production UTC `Clock` bean is configured.
- VS Code uses the same pinned google-java-format version as Spotless and loads
  the ignored local `.env` file when tests are launched from the editor.

## Next task

Add focused tests for the create service's successful, unknown-user, and past-
publication-date paths before adding the HTTP controller.

## Important decisions

### Confirmed

- This is a separate project and repository, not part of the Java or Spring
  Boot kata repositories.
- Development will be guided through small vertical slices.
- The learner makes the first design and implementation attempt.
- The mentor provides explanations, hints, verification, and senior-level code
  review without silently implementing the application.
- Java 21 and Maven are the baseline toolchain.
- The initial project uses Spring Boot 4.1.0, Spring MVC, and Jakarta Validation.
- The project currently uses port 8081 for local development.
- The MVP uses PostgreSQL locally and in deployment rather than maintaining
  separate SQLite and PostgreSQL migrations.
- Local development uses the official PostgreSQL 18 container image.
- User IDs are UUIDs; Burger of the Day IDs are generated numeric IDs.
- Burger board text is required, whitespace-preserving, and limited to 150
  characters. Optional commentary is limited to 500 characters.
- Publication is derived from `publishDate` relative to UTC; no midnight
  scheduler is required.
- Published burgers are immutable. Their creators may hide and unhide them.
- PostgreSQL schema changes are managed with versioned Flyway migrations.
- A future version may let creators add tags to their posts and let public
  users select a tag to browse related published Burger of the Day posts. Tags
  are explicitly excluded from the current MVP.

### Pending

- Testing strategy for the first vertical slice
- Deployment target

Record future decisions here with enough context to explain why they were made.

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

The create request DTO and its focused Jakarta Validation tests exist. A first
PostgreSQL Flyway migration has been drafted for `app_user` and
`burger_of_the_day`. No controller, service, JPA entity, repository, or
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

## Next task

Configure Spotless for deterministic Java formatting and enforce its check in
the Maven lifecycle. Then map `BurgerOfTheDay` and its required creator
relationship.

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

### Pending

- Testing strategy for the first vertical slice
- Deployment target

Record future decisions here with enough context to explain why they were made.

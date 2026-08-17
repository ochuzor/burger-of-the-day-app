# Burger of the Day — Project Brief

## Product purpose

Build a small Spring Boot API inspired by the "Burger of the Day" board from
*Bob's Burgers*. Users post short fictional burger-board ideas that become
public immediately.

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

- Create and immediately publish an immutable Burger of the Day.
- Let its creator hide and unhide it after posting.
- List public burgers by creator and publication date.
- Use UTC for publication timestamps and date-based filtering.

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

The create request DTO, controller, service, PostgreSQL Flyway migration, JPA
entities, and Spring Data repositories for `app_user` and
`burger_of_the_day` exist. Their focused validation, web, service, repository,
and full HTTP-to-database integration tests pass.

## Completed features

The create and public-read features use the immediate-publication lifecycle. A
successful create request publishes the burger immediately, and visible posts
can be retrieved publicly by ID.

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
- `BurgerOfTheDayService` implements creator lookup, immediate publication
  timestamp creation through an injected `Clock`, and transactional
  persistence. A production UTC `Clock` bean is configured.
- Focused Mockito unit tests cover successful immediate publication and unknown
  creators. The successful path captures and verifies the entity sent to
  persistence using a fixed UTC clock.
- The create controller implements request validation, temporary
  `X-Username` identification, `201 Created` with a `Location` header, and
  stable JSON errors for validation, authorization, and malformed JSON. Seven
  focused MockMvc tests cover these behaviors.
- A transactional `@SpringBootTest` sends a real create request through MockMvc
  and verifies the generated Burger of the Day through PostgreSQL after
  clearing the persistence context. The full 21-test Maven verification passes
  against local PostgreSQL, including Spotless enforcement and executable JAR
  packaging.
- Public `GET /burger-of-the-day/{id}` returns a response DTO containing the
  burger ID, board text, optional commentary, publication timestamp, and creator
  username without requiring `X-Username`.
- Public `GET /burger-of-the-day` returns visible burgers in a stable,
  newest-first paginated response. It supports optional UTC calendar-date
  filtering through `publish_date`, validates page and size bounds, and returns
  stable JSON errors for malformed dates and pagination parameters.
- Public burger listing supports optional creator filtering through `created_by`
  and permits combining creator and UTC publication-date filters. Repository,
  service, controller, and PostgreSQL-backed HTTP tests verify that hidden posts
  and posts belonging to other creators are excluded. Blank creator filters
  produce a stable bad-request response. The full 52-test Maven verification,
  Spotless check, and executable JAR packaging pass.
- Focused controller tests cover default and custom pagination, date filtering,
  and invalid query parameters. A PostgreSQL-backed HTTP integration test
  verifies ordering, hidden-record exclusion, and pagination metadata.
- Creator-only `PATCH /burger-of-the-day/{id}/visibility` hides or unhides an
  existing burger and returns `204 No Content`. Missing or unknown users are
  unauthorized, non-creators receive a stable forbidden response, and repeated
  requests for the current state are idempotent.
- Focused service and controller tests cover ownership, missing users and
  burgers, request validation, and HTTP error mapping. A PostgreSQL-backed HTTP
  integration test verifies the complete visible-to-hidden-to-visible lifecycle
  and confirms that unhiding preserves the original publication timestamp.
- The public read service conceals missing and hidden burgers behind the same
  not-found exception and HTTP response. Focused service and MockMvc tests cover
  the visibility boundary and response contract.
- Flyway migration V2 replaces `created_at` and `publish_date` with the required
  `published_at` timestamp, preserving existing rows as published records.
- PostgreSQL-backed integration tests verify immediate publication, successful
  public retrieval, and lazy creator loading with OSIV disabled. The full
  27-test Maven verification, Spotless check, and JAR packaging pass.
- VS Code uses the same pinned google-java-format version as Spotless and loads
  the ignored local `.env` file when tests are launched from the editor.

## Next task

Add an authenticated creator-management listing so creators can find all their
own posts, including hidden posts that they may want to unhide.

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
- Creating a Burger of the Day publishes it immediately. There are no drafts,
  scheduled posts, or separate publication action in the MVP.
- `published_at` is the single creation/publication timestamp. The earlier
  `created_at` and `publish_date` model is being retired through a new Flyway
  migration rather than by editing the applied version 1 migration.
- Posted burgers are immutable. Their creators may hide and unhide them;
  unhiding preserves the original publication timestamp.
- Visibility changes use `PATCH /burger-of-the-day/{id}/visibility` with a
  required Boolean `hidden` field. Successful changes return `204 No Content`,
  setting the current value is idempotent, and attempts by a different known
  user return `403 Forbidden`.
- PostgreSQL schema changes are managed with versioned Flyway migrations.
- A future version may let creators add tags to their posts and let public
  users select a tag to browse related published Burger of the Day posts. Tags
  are explicitly excluded from the current MVP.
- Public burger listing accepts an optional ISO 8601 `publish_date` filter. The
  filter selects `published_at` values within that UTC calendar day. An omitted
  date lists all visible burgers; a valid future date and a date with no matches
  both produce an empty result.
- Public listing is zero-based and paginated, defaults to 50 records, and
  accepts at most 200 records per page. Its stable order is publication
  timestamp descending, then ID descending.
- The listing API returns an application-owned page response containing
  `content`, `page`, `size`, `total_elements`, and `total_pages`, rather than
  exposing Spring Data's `Page` serialization as the HTTP contract.

### Pending

- Deployment target
- Production identity provider; Supabase Auth and Supabase PostgreSQL are under
  consideration, but the discussion is currently paused.

Record future decisions here with enough context to explain why they were made.

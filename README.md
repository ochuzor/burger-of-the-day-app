# Burger of the Day

This project is inspired by [*Bob's Burgers*](https://bobs-burgers.fandom.com/wiki/Bob%27s_Burgers_Wiki), in which Bob Belcher writes a new Burger of the Day on the restaurant's chalkboard.

## About

Burger of the Day is a Spring Boot REST API where users publish fictional burger ideas that other users can browse. Posts become public immediately and cannot be edited after publication, but their creators can hide and unhide them.

## Features

- Posts are published immediately and are publicly available
- Users can filter posts by publication date and creator
- Creators can list all their posts, including hidden posts
- Creators can hide and unhide their posts
- Hidden posts are visible only to their creator
- Published posts cannot be modified

## Technology

- Java 21
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- PostgreSQL 18
- Flyway
- Maven
- JUnit, Mockito, and MockMvc
- Docker Compose
- Spotless with Google Java Format

## Prerequisites

- Java 21
- Docker with Docker Compose
- A Unix-like shell for the documented environment-loading commands

Maven does not need to be installed because the project includes Maven Wrapper.

## Local setup

Copy the example environment file:

```sh
cp .env.example .env
```

It contains the required variables:

```sh
POSTGRES_DB=burger_of_the_day
POSTGRES_USER=your_local_username
POSTGRES_PASSWORD=your_local_password
```

Update `.env` with your local values, then load them into the current shell:

```sh
set -a
source .env
set +a
```

Start PostgreSQL and wait for it to become healthy:

```sh
docker compose up -d
```

The `.env` file contains local credentials, is ignored by Git, and must never be committed.

Run the application with the `local` profile:

```bash
./mvnw -Dspring-boot.run.profiles=local spring-boot:run
```

Flyway applies database migrations automatically during startup. When the `local` profile is active, the application also creates a development user named `tester` if it does not already exist.

The API is available at `http://localhost:8081`. The port is configured by the `server.port` property in `src/main/resources/application.properties`.

Stop the application with <kbd>Ctrl</kbd>+<kbd>C</kbd>. Stop PostgreSQL when it is no longer needed:

```bash
docker compose down
```

The named Docker volume retains the database between restarts.

## API usage

Public read endpoints do not require a user header. During this local MVP, write and creator-management requests use `X-Username: tester` as temporary identification. This header is not production authentication and must be replaced before public deployment.

### Create a Burger of the Day

```bash
curl -i -X POST "http://localhost:8081/burger-of-the-day" \
  -H "Content-Type: application/json" \
  -H "X-Username: tester" \
  -d '{"text":"The Local Yokel Burger","commentary":"Comes with locally sourced ingredients"}'
```

A successful request returns `201 Created`, an empty body, and a `Location` header identifying the new resource. Board text is required and limited to 150 characters; optional commentary is limited to 500 characters.

### Read a public Burger of the Day

Replace `1` with an existing burger ID:

```bash
curl -i "http://localhost:8081/burger-of-the-day/1"
```

Missing and hidden burgers both return `404 Not Found` so the public API does not reveal hidden resources.

### List public burgers

```bash
curl -i "http://localhost:8081/burger-of-the-day?page=0&size=50"
```

The optional `publish_date` filter accepts an ISO date interpreted as a UTC calendar day. The optional `created_by` filter accepts a username, and both filters can be combined:

```bash
curl -i "http://localhost:8081/burger-of-the-day?publish_date=2026-08-20&created_by=tester&page=0&size=20"
```

Pages are zero-based, default to 50 records, and allow at most 200. Results are ordered by publication time descending and then ID descending. Hidden burgers are excluded.

### List the creator's burgers

This management endpoint includes the creator's hidden posts and reports each post's `hidden` state:

```bash
curl -i "http://localhost:8081/me/burger-of-the-day?page=0&size=50" \
  -H "X-Username: tester"
```

It supports the same optional `publish_date`, `page`, and `size` parameters as the public listing.

### Hide or unhide a burger

Only the creator can change a burger's visibility. Replace `1` with an existing burger ID:

```bash
curl -i -X PATCH "http://localhost:8081/burger-of-the-day/1/visibility" \
  -H "Content-Type: application/json" \
  -H "X-Username: tester" \
  -d '{"hidden":true}'
```

Use `false` to unhide it. Successful visibility changes return `204 No Content`, and requesting the current state again is allowed.

Error responses use a stable JSON shape:

```json
{"error":"burger of the day not found"}
```

## Testing and packaging

PostgreSQL must be running and the `.env` variables must be loaded before running the complete test suite:

```bash
./mvnw verify
```

This runs the unit, MVC, repository, and HTTP-to-database integration tests, enforces formatting with Spotless, and builds the executable JAR under `target/`.

Run the packaged application with the local profile using:

```bash
java -jar target/burger-of-the-day-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

## Development approach

This is a learning and portfolio project developed incrementally through small HTTP-to-persistence vertical slices. Product decisions, scope changes, architecture, and progress are recorded in [`docs/PROJECT_BRIEF.md`](docs/PROJECT_BRIEF.md).

The project used AI-assisted mentoring. I made the product, design, and implementation decisions and wrote the application incrementally; AI was used for explanations, progressively stronger hints, code review, debugging guidance, and test feedback.

## Current limitations

- `X-Username` is temporary local identification, not secure authentication
- There is no frontend yet
- Registration, voting, winners, comments, and tags are outside the current MVP
- Production deployment configuration is not included

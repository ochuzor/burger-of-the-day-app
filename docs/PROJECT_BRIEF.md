# Burger of the Day — Project Brief

## Product purpose

Build a small Spring Boot API inspired by the "Burger of the Day" board from
*Bob's Burgers*. The precise product behavior and MVP are not yet agreed.

This is both a usable application and a portfolio learning project. Its primary
learning purpose is to apply the completed Java and Spring Boot curricula in a
project designed and implemented from scratch.

## Intended users

Pending product definition.

Likely users may include fans or developers who want to create, browse, or
select themed burger names, but this must be confirmed before features are
designed.

## Scope and exclusions

### Current scope

- Define a small, finishable MVP.
- Build the MVP through complete vertical features.
- Use production-oriented structure and habits in proportion to the project's
  size.
- Keep the application runnable locally without paid or external infrastructure
  during its initial development.

### Current exclusions

Until explicitly brought into scope:

- Authentication and authorization
- A browser or mobile frontend
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

No controller, domain model, service layer, persistence layer, database,
migration, or product feature exists yet.

## Completed features

No product features are complete.

Repository setup completed so far:

- Spring Boot project scaffold created.
- Maven Wrapper added.
- Spring MVC and Jakarta Validation dependencies selected.
- Basic context-load test created.
- Application configured to run on port 8081.
- Durable mentoring instructions and this project brief added.

## Next task

Define the product before adding more application code:

1. Describe the primary user.
2. State the problem or useful outcome the API provides.
3. Choose the smallest MVP behavior.
4. List explicit non-goals for the first version.
5. Define the first useful vertical feature and its acceptance criteria.
6. Define what "finished" means for the MVP.

Do not add domain classes, controllers, persistence, or more dependencies until
these decisions are made.

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

### Pending

- Exact product purpose and target user
- MVP use cases and acceptance criteria
- Domain vocabulary and model
- Persistence requirements and database choice
- Public API shape
- Testing strategy for the first vertical slice
- Deployment target

Record future decisions here with enough context to explain why they were made.

# Copilot Instructions for Artivact Project

## Project Overview

Artivact is a digital artifact management system following the **Ports and Adapters (Hexagonal) Architecture** with a
Spring Boot backend and Quasar/Vue.js frontend.

## Architecture Principles

- Follow the **Ports and Adapters** (Hexagonal Architecture) pattern strictly
- **Domain Module**: Contains only domain entities, value objects, and domain logic (no frameworks, no infrastructure)
- **Application Module**: Contains application services, use cases, and port definitions (interfaces for adapters)
- **Adapters Module**: Contains all infrastructure code (REST controllers, database repositories, external services)
- **Frontend Module**: SPA web frontend built with Quasar/Vue 3
- **Documentation Module**: Documentation written with VitePress
- Keep domain logic independent of frameworks and infrastructure
- Dependencies always point inward: Adapters → Application → Domain

## General Guidelines

- Write clean, maintainable, and well-documented code
- Follow SOLID principles and design patterns where appropriate
- Prioritize readability and simplicity over cleverness
- Use English for all code comments and documentation
- Keep modules loosely coupled and highly cohesive

## Domain Module

- Contains only pure domain logic (entities, value objects, domain services)
- **No framework dependencies** (no Spring, no JPA annotations)
- Use plain Java classes and interfaces
- Implement domain invariants and business rules
- Use domain events for communication between aggregates
- Keep entities rich with behavior, not anemic data structures
- Use value objects for concepts without identity
- Define domain exceptions for business rule violations

## Application Module

- Contains application services (use cases) and port definitions
- Define **inbound ports** (interfaces for application services)
- Define **outbound ports** (interfaces for repositories, external services)
- Orchestrate domain objects to fulfill use cases
- Handle transaction boundaries
- Map between domain objects and DTOs if needed
- No infrastructure code (no database, no HTTP)
- Use dependency injection via constructor injection
- Use Lombok annotations (`@Data`, `@Builder`, `@Slf4j`) to reduce boilerplate

## Adapters Module

- Contains all infrastructure and framework code
- **Inbound adapters**: REST controllers, message listeners, scheduled tasks
- **Outbound adapters**: Database repositories (JPA), external API clients, file system access
- Implement ports defined in the application module
- Use Spring Boot conventions and best practices
- Use Lombok annotations (`@Data`, `@Builder`, `@Slf4j`) to reduce boilerplate
- Follow RESTful API design principles for controllers
- Use `@RestController` for REST endpoints
- Use JPA/Hibernate for database access with proper entity relationships
- Write database migrations using Flyway or Liquibase
- Map between DTOs and domain objects in adapters
- Apply validation annotations (`@Valid`, `@NotNull`, `@Size`, etc.) on DTOs

## Frontend Module (Quasar/Vue 3)

- Built with **Quasar Framework** and Vue
- Use Vue 3 Composition API with `<script setup>` syntax
- Write components in TypeScript with proper type definitions
- Follow Vue.js style guide for component naming (PascalCase for files)
- Use Pinia for state management
- Leverage Quasar components and utilities
- Implement proper error handling and loading states
- Use composables for reusable logic
- Apply CSS scoping with `<style scoped>`
- Use async/await for API calls with try-catch blocks
- Implement proper TypeScript interfaces for API responses
- Use Vue Router for navigation with typed routes
- Follow Quasar's layout system and design patterns

## Documentation Module (VitePress)

- Documentation written with **VitePress**
- Use Markdown for all documentation files
- Structure documentation logically (architecture, API, guides)
- Include code examples and diagrams where helpful
- Keep documentation up-to-date with code changes
- Use VitePress features (custom containers, code groups, etc.)
- Document architecture decisions and design patterns

## Database

- Use meaningful table and column names (snake\_case)
- Use appropriate indexes for frequently queried columns
- Write migration scripts for schema changes (never modify existing migrations)
- Use foreign key constraints to maintain referential integrity
- Document complex queries with comments
- Keep database entities in the adapters module, separate from domain entities

## Tests

- **Focus on behavior, not implementation details**
- Test domain logic in isolation (unit tests without mocks)
- Test application services with mocked ports
- Test adapters with integration tests
- Use the **Given-When-Then** pattern for test structure
- Test classes and methods should have clear and descriptive names
- Each test should focus on a single functionality or behavior
- Use meaningful assertions with AssertJ for better readability
- Mock only external dependencies (ports), not domain objects
- **Avoid using Reflection** in tests unless absolutely necessary
- Test edge cases and error conditions, not just happy paths
- Ensure tests are independent and can be run in any order
- Use parameterized tests (`@ParameterizedTest`) to cover multiple scenarios
- Maintain a consistent structure and style throughout test code
- **Java**: Use JUnit 5, Mockito, and AssertJ
- **Frontend**: Use Vitest or Jest with Vue Test Utils
- Remove tests that only verify implementation details
- User getFirst() to access first element in collections in tests

## API Design

- Use HTTP methods correctly (GET, POST, PUT, DELETE, PATCH)
- Return appropriate HTTP status codes (200, 201, 204, 400, 404, 500, etc.)
- Use plural nouns for resource endpoints (e.g., `/api/artifacts`)
- Version APIs when making breaking changes (e.g., `/api/v1/artifacts`)
- Implement pagination for list endpoints with consistent parameters
- Use DTOs for request/response in adapters
- Map DTOs to domain objects in REST controllers
- Document APIs using OpenAPI/Swagger annotations
- Use meaningful error messages in API responses

## Security

- Never commit sensitive data (passwords, API keys, tokens) to version control
- Use environment variables for configuration
- Implement authentication and authorization in adapters
- Validate and sanitize all user inputs in adapters
- Use HTTPS for all production endpoints
- Implement CORS policies appropriately
- Apply rate limiting on public APIs

## Error Handling

- Define domain exceptions in the domain module
- Handle technical exceptions in adapters
- Provide meaningful error messages to users
- Log errors with appropriate severity levels (ERROR, WARN, INFO, DEBUG)
- Return consistent error response structures from APIs
- Include request context in logs for debugging
- Use `@ControllerAdvice` for global exception handling in REST adapters

## Code Style

- **TypeScript/JavaScript**: Use ESLint and Prettier for formatting
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Avoid magic numbers and strings (use constants or enums)
- Keep functions small and focused (single responsibility)
- Use early returns to reduce nesting
- Prefer immutability where possible

## Dependency Injection

- Use constructor injection exclusively (avoid field injection)
- Define dependencies as interfaces (ports) in application module
- Implement dependencies in adapters module
- Wire dependencies using Spring configuration

## Version Control

- Write clear, concise commit messages in English
- Use conventional commits format: `type(scope): description`
- Types: feat, fix, docs, style, refactor, test, chore
- Create feature branches from main
- Keep commits atomic and focused on single changes
- Squash commits before merging if necessary

## Module Dependencies

- **Domain**: No external dependencies (pure Java)
- **Application**: Depends on Domain only
- **Adapters**: Depends on Application and Domain
- **Frontend**: Independent SPA application
- **Documentation**: Independent documentation site
- Never create circular dependencies between modules

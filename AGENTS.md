# Repository Guidelines

## Project Structure & Module Organization
This is a Java 21 Spring Boot Maven backend for `com.procurement`. Application code lives in `src/main/java/com/procurement`, with feature modules under `modules` and shared infrastructure under `common`.

- `modules/auth`: authentication controllers, DTOs, services, security filters, and JWT support.
- `modules/users`: user controller, DTOs, entity, repository, and service.
- `common/config`: shared Spring configuration such as cache, OpenAPI, and seed data.
- `common/response`: reusable API response types.
- `src/main/resources/application.yaml`: Spring, database, Redis, JWT, and OpenAPI configuration.
- `src/main/resources/db/migrations`: Flyway SQL migrations.
- `src/test/java`: JUnit/Spring Boot tests.
- `docs`: project documentation.

## Build, Test, and Development Commands
Use the Maven wrapper when possible:

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw clean package
```

On Windows, use `mvnw.cmd` or the installed `mvn.cmd` if wrapper execution is blocked. `test` runs the Spring Boot test suite, `spring-boot:run` starts the API locally, and `clean package` builds the deployable jar in `target/`.

Use `docker compose up -d` to start local PostgreSQL and Redis services defined in `docker-compose.yaml`.

## Coding Style & Naming Conventions
Follow standard Java and Spring conventions: 4-space indentation, `PascalCase` classes, `camelCase` fields and methods, and lowercase package names. Keep controllers thin, put business rules in services, and keep persistence concerns in repositories/entities. DTOs should end with `Request`, `Response`, or another clear role suffix.

Prefer constructor injection for Spring beans. Lombok is available, but use it consistently and avoid hiding important behavior behind annotations.

## Testing Guidelines
Tests use Spring Boot's test stack and should live under `src/test/java` with packages matching production code. Name test classes with the `*Tests` or `*Test` suffix. Add focused unit or slice tests for service/controller changes, and run `./mvnw test` before opening a PR.

## Commit & Pull Request Guidelines
The current history uses Conventional Commit style, for example `feat: init repository`. Continue using short, imperative commit messages such as `fix: validate login payload` or `feat: add supplier repository`.

Pull requests should include a short description, linked issue when applicable, database migration notes, configuration changes, and test results. Include screenshots only for API documentation or UI-visible changes.

## Security & Configuration Tips
Do not commit real secrets. Keep local values in `.env`, especially `JWT_SECRET`, database credentials, and port overrides. When adding migrations, ensure Flyway locations in `application.yaml` match the migration directory.

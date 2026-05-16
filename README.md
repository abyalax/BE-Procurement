# Procurement Backend

Spring Boot 4 backend for procurement workflows. The service uses PostgreSQL for persistence, Redis for caching, Flyway for schema migration, and JWT-based security for auth and protected endpoints. Development reload is enabled through Spring Boot DevTools.

## Project Layout

- `src/main/java/com/procurement`: application source code.
- `src/main/java/com/procurement/modules/auth`: login, register, JWT, and security flow.
- `src/main/java/com/procurement/modules/users`: user APIs, service, repository, and DTOs.
- `src/main/java/com/procurement/modules/role_permissions`: role and permission management.
- `src/main/java/com/procurement/common`: shared config, middleware, exceptions, and API response types.
- `src/main/resources/application.yaml`: runtime configuration.
- `src/main/resources/db/migration`: Flyway SQL migrations.
- `src/test/java`: Spring Boot tests.

## Run Commands

```bash
./mvnw dependency:resolve
./mvnw test
./mvnw spring-boot:run
./mvnw clean package
docker compose up -d
```

- `dependency:resolve` downloads and syncs Maven dependencies.
- `test` runs the Spring Boot test suite.
- `spring-boot:run` starts the API locally with DevTools hot reload.
- `clean package` builds the application jar in `target/`.
- `docker compose up -d` starts PostgreSQL and Redis from `docker-compose.yaml`.

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Local Setup

Create or update `.env` with PostgreSQL, Redis, JWT, and seed credentials before starting the app. Spring Boot loads the file automatically on startup, so you do not need to export the variables manually in PowerShell or `cmd`.

Default local values are already provided in `.env.example`.

The application expects PostgreSQL on `POSTGRES_PORT` and Redis on `REDIS_PORT`. Flyway runs automatically on startup and seeds default roles, permissions, and an admin user.

## Dev Reload

`spring-boot:run` works with DevTools, but the JVM only restarts after compiled classes change. If edits do not trigger reload, enable automatic build in your IDE so `.java` changes are compiled into `target/classes` on save.

## Notes

- The API is guarded by permission checks such as `users:read`, `users:write`, `roles:read`, and `roles:write`.
- Use `GET /api/v1/users?page=0&size=20` for paginated user lists.
- Keep migration files in `src/main/resources/db/migration` using Flyway naming like `V1__description.sql`.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.6/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.6/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.6/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.6/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation](https://docs.spring.io/spring-boot/4.0.6/reference/io/validation.html)
* [Spring Security](https://docs.spring.io/spring-boot/4.0.6/reference/web/spring-security.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.0.6/reference/actuator/index.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

_last Updated at 16 May 2026_

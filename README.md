# Procurement Backend

Spring Boot 4 backend for procurement workflows. The service uses PostgreSQL for persistence, Redis for caching, Liquibase for schema migration, and JWT-based security for auth and protected endpoints. Development reload is enabled through Spring Boot DevTools.

## Project Layout

- `src/main/java/com/procurement`: application source code.
- `src/main/java/com/procurement/modules/auth`: login, register, JWT, and security flow.
- `src/main/java/com/procurement/modules/users`: user APIs, service, repository, and DTOs.
- `src/main/java/com/procurement/modules/role_permissions`: role and permission management.
- `src/main/java/com/procurement/common`: shared config, middleware, exceptions, and API response types.
- `src/main/resources/application.yaml`: runtime configuration.
- `src/main/resources/db/changelog`: Liquibase master changelog and formatted SQL migrations.
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

The application expects PostgreSQL on `POSTGRES_PORT` and Redis on `REDIS_PORT`. Liquibase is disabled by default on startup, and schema migration plus seed data are run manually through the maintenance application.

## Liquibase Migration Workflow

### Create a new migration

Add a new formatted SQL file under `src/main/resources/db/changelog/changes` and include it from `src/main/resources/db/changelog/db.changelog-master.yml`.

```txt
003-add_purchase_order_status.sql
```

Rules to follow:

- Use the next unused numeric prefix.
- Start the SQL file with `--liquibase formatted sql` and a unique `--changeset` line.
- Keep the filename stable after it has been committed and applied.
- Do not edit an applied migration file unless you also reset the local database. For shared databases, add a new forward migration instead.

### Run migrations

Run migrations manually with the maintenance application:

```bash
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=migrate"
```

On Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=migrate"
```

Liquibase reads `src/main/resources/db/changelog/db.changelog-master.yml`, which currently includes only schema changes.

### Revert a migration

Liquibase does not automatically undo an applied SQL migration during normal startup. The recommended workflow is:

1. Create a new migration that reverses the previous schema change.
2. Include it from the master changelog.
3. Start the application again so Liquibase applies the new migration.

Example: if `003` added a column, create `004-revert_003.sql` or a forward-fixing migration that restores the desired schema.

## Dev Reload

`spring-boot:run` works with DevTools, but the JVM only restarts after compiled classes change. If edits do not trigger reload, enable automatic build in your IDE so `.java` changes are compiled into `target/classes` on save.

## Code Formatter

This project uses Prettier with Java plugin for consistent code formatting across the team.

### Required VSCode Extension

Install the following extension:

* [Prettier Java VSCode Extension](https://marketplace.visualstudio.com/items?itemName=rudrapatel.prettier-plugin-java-vscode&utm_source=chatgpt.com)

Project extension recommendations are already configured in:

```txt
.vscode/extensions.json
```

---

## Formatter Setup

Install dependencies:

```bash
npm install
```

Formatter dependencies:

```json
{
  "devDependencies": {
    "prettier": "^3.x",
    "prettier-plugin-java": "^2.x"
  }
}
```

---

## Format All Java Files

Run formatter manually:

```bash
npm run format
```

Script configuration:

```json
{
  "scripts": {
    "format": "prettier --config .prettierrc --write \"src/**/*.java\""
  }
}
```

---

## VSCode Settings

Workspace formatter configuration:

```json
{
  "[java]": {
    "editor.defaultFormatter": "rudrapatel.prettier-plugin-java-vscode"
  },
  "editor.formatOnSave": true,
  "prettier-plugin-java-vscode.prettierConfigPath": ".prettierrc"
}
```

Location:

```txt
.vscode/settings.json
```

---

## Formatter Configuration

Formatter rules are configured in:

```txt
.prettierrc
```

Example:

```json
{
    "tabWidth": 2,
    "useTabs": false,
    "trailingComma": "all",
    "printWidth": 100
}
```

---

## Usage

### Format on Save

Save `.java` file and formatter will run automatically.

### Manual Format

VSCode shortcut:

```txt
Shift + Alt + F
```

Or use:

```bash
npm run format
```

## Notes

- The API is guarded by permission checks such as `users:read`, `users:write`, `roles:read`, and `roles:write`.
- Use `GET /api/v1/users?page=0&size=20` for paginated user lists.
- Keep migration files in `src/main/resources/db/changelog/changes` and include them from `db.changelog-master.yml`.
- Never edit a migration file that has already been applied to a shared database. Add a new version instead.

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

_last Updated at 17 May 2026_

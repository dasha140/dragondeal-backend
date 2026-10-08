# Contributing to DragonDeal Backend

## Development Workflow

1. **Create a feature branch** from `develop`
   ```bash
   git checkout -b feature/my-feature develop
   ```

2. **Make changes** following the layered architecture:
   - New API → Controller + Service + Repository + DTOs
   - New entity → Entity + Repository + Liquibase changelog
   - New blockchain interaction → Service + Web3j integration

3. **Write tests**
   - Unit tests in `src/test/java/`
   - Use `@DataJpaTest` for repository tests
   - Use `@WebMvcTest` for controller tests

4. **Commit with conventional messages**
   ```
   feat: add seller rating endpoint
   fix: resolve JWT token expiration issue
   test: add service tests for OrderService
   ```

5. **Push and create PR**

## Code Style

- **Java 21** features allowed (records, sealed classes, pattern matching)
- **Lombok** for boilerplate (`@Data`, `@Builder`, `@AllArgsConstructor`)
- **Package naming**: `org.example.controller`, `org.example.service`, etc.
- **Class naming**: `*Controller`, `*Service`, `*Repository`, `*DTO`

## Architecture Rules

- **Controllers are thin** — delegate to services, no business logic
- **Services are transactional** — `@Transactional` on service methods
- **Repositories extend JpaRepository** — use derived queries or `@Query`
- **DTOs for API contracts** — never expose entities directly
- **Mappers for conversion** — Entity ↔ DTO, no manual copying in services

## Before Submitting a PR

- [ ] `mvn test` passes
- [ ] `mvn clean package` builds successfully
- [ ] No new compiler warnings
- [ ] Liquibase changelog updated (if DB changes)

## Database Migrations

All DB changes go through Liquibase:

- Changelog: `src/main/resources/db/changelog/`
- Each migration: `db/changelog/YYYY-MM-DD-description.sql`
- Register in `db.changelog-master.yaml`

## Running Locally

```bash
# Dev mode (H2)
mvn spring-boot:run

# Prod mode (PostgreSQL)
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

H2 Console: `http://localhost:8081/h2-console` (dev only)

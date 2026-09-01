# AGENTS.md

## Stack
- Java 17 (pom target) + Spring Boot **4.1.1** + Maven wrapper (downloads Maven 3.9.16).
- Machine currently runs Java 25 via SDKMAN; build still targets 17 via `pom.xml`.
- Infra via Docker Compose: Redis (6379), Minio (9000), Postgres (5432, db `image-processor`, admin/admin), RabbitMQ (5672 + mgmt 15672).

## Commands
- `make build` / `make test` / `make run` / `make package` / `make up` / `make down` (see `Makefile`).
- Direct: `./mvnw <goal>`. Run a single test: `./mvnw -Dtest=MinioHealthIndicatorTest test`.
- `make run` / `./mvnw spring-boot:run` auto-starts docker-compose services via the `spring-boot-docker-compose` starter — Docker must be running.

## Gotchas
- **Spring Boot 4 package renames**: actuator health classes live under `org.springframework.boot.health.contributor` (`Health`, `HealthIndicator`, `Status`), NOT the Spring Boot 3 `org.springframework.boot.actuate.health` package. Don't import the old package.
- `mvnw` uses `distributionType=only-script` (no bundled jar); it downloads Maven from `distributionUrl` on first run.
- `@SpringBootTest` (`ImageProcessor1ApplicationTests`) boots the full context and will start docker-compose, so it requires Docker.

## Architecture
- Single-module, package-by-domain (modular monolith). Package root: `br.com.imageprocessor`.
- Layout convention: `infra/` for cross-cutting plumbing (e.g. `infra/security/SecurityConfig`, `infra/messaging/`); each domain is self-contained under its own top-level package: `imagestorage/{config,health}`, `imageprocessor/{controller,domain,application}`. Future domain: `auth` — create only when the first code lands.
- Within a domain: `controller/` = inbound REST adapter; `domain/` = core logic (`ImageOperationRegistry` + `domain/operations/*`); `application/` = use cases + ports (`ImageProcessingService`, `ImageProcessPublisher`, `ImageProcessCommand`).
- Dependency direction: `imageprocessor` -> `imagestorage` (never reverse). `imagestorage` is an adapter (Minio), not a service.
- Messaging: the domain owns the port interfaces (`imageprocessor/application/ImageProcessPublisher`, `ImageProcessingService`); the RabbitMQ adapters + config live in `infra/messaging/` (`RabbitMQImageProcessPublisher`, `RabbitMQImageProcessConsumer`, `RabbitMQConfig`). Controllers never touch RabbitTemplate/AMQP types.
- Security: all endpoints require auth except `/actuator/health/**`; HTTP Basic enabled; no `UserDetailsService`, so Spring generates a `user`/random-password default at startup.

## Config
- App config in `src/main/resources/application.yaml` (datasource, redis, minio props via `@ConfigurationProperties(prefix = "minio")`).
- Docker service credentials in `docker-compose.yaml` (minio admin/password; postgres admin/admin).

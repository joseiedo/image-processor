# AGENTS.md

## Stack
- Java 17 (pom target) + Spring Boot **4.1.1** + Maven wrapper (downloads Maven 3.9.16).
- Machine currently runs Java 25 via SDKMAN; build still targets 17 via `pom.xml`.
- Infra via Docker Compose: Redis (6379), Minio (9000), Postgres (5432, db `image-processor`, admin/admin), RabbitMQ (5672 + mgmt 15672).

## Commands
- `make build` / `make test` / `make run` / `make smoke` / `make package` / `make up` / `make down` (see `Makefile`).
- Direct: `./mvnw <goal>`. Run a single test: `./mvnw -Dtest=MinioHealthIndicatorTest test`.
- `make run` / `./mvnw spring-boot:run` auto-starts docker-compose services via the `spring-boot-docker-compose` starter — Docker must be running.
- `make smoke` runs `scripts/e2e-smoke.js`: end-to-end smoke against the running app. It uploads `monke.jpg` for each operation (grayscale/resize/rotate/crop/blur) via `POST /api/v1/images/process`, polls, GETs each result, verifies PNG, and writes input + results + `index.html` to `target/e2e-report/`. App + stack must be up.

## Gotchas
- **Spring Boot 4 package renames**: actuator health classes live under `org.springframework.boot.health.contributor` (`Health`, `HealthIndicator`, `Status`), NOT the Spring Boot 3 `org.springframework.boot.actuate.health` package. Don't import the old package.
- **Jackson 3**: Boot 4 defaults to Jackson 3 under the `tools.jackson.*` package. The auto-configured `ObjectMapper` bean is `tools.jackson.databind.ObjectMapper`; the Jackson 2 `com.fasterxml.jackson.databind.ObjectMapper` is NOT a Spring bean. Never import Jackson 2 types in Spring-managed code (both versions are on the classpath, so it compiles but fails at runtime with "No qualifying bean"). Exception base class is `tools.jackson.core.JacksonException`.
- `mvnw` uses `distributionType=only-script` (no bundled jar); it downloads Maven from `distributionUrl` on first run.
- `@SpringBootTest` (`ImageProcessorApplicationTests`) boots the full context. With `spring.jpa.database-platform` set explicitly, `contextLoads` passes even if Postgres is unreachable (connection refusal is a non-fatal WARN), so Docker is not strictly required for it.

## Architecture
- Single-module, package-by-domain (modular monolith). Package root: `br.com.imageprocessor`.
- Layout convention: `infra/` for cross-cutting plumbing (e.g. `infra/security/SecurityConfig`, `infra/messaging/`); each domain is self-contained under its own top-level package: `imagestorage/{config,health}`, `imageprocessor/{controller,domain,application}`. Future domain: `auth` — create only when the first code lands.
- Within a domain: `controller/` = inbound REST adapter; `domain/` = core logic (`ImageOperationRegistry` + `domain/operations/*`); `application/` = use cases + ports (`ImageProcessingService`, `ImageProcessPublisher`, `ImageProcessCommand`).
- Dependency direction: `imageprocessor` -> `imagestorage` (never reverse). `imagestorage` is an adapter (Minio), not a service — it defines the `ImageStorage` port (interface) and implements it via `MinioImageStorage`; the `imageprocessor` service consumes the interface only.
- Minio: bucket name comes from `minio.bucket` in `application.yaml`; `MinioImageStorage` creates the bucket eagerly at startup (`@PostConstruct`, tolerant WARN) and lazily on `save` as fallback; encodes results as PNG. Note: Minio has no volume in docker-compose, so buckets vanish on `docker compose down`.
- Messaging: the domain owns the port interfaces (`imageprocessor/application/ImageProcessPublisher`, `ImageProcessingService`); the RabbitMQ adapters + config live in `infra/messaging/` (`RabbitMQImageProcessPublisher`, `RabbitMQImageProcessConsumer`, `RabbitMQConfig`). Controllers never touch RabbitTemplate/AMQP types.
- Security: **auth currently disabled** (`anyRequest().permitAll()` in `SecurityConfig`); originally all endpoints required auth except `/actuator/health/**`, HTTP Basic, default `user`/random-password. Re-enable when auth lands.

## Config
- App config in `src/main/resources/application.yaml` (datasource, redis, minio props via `@ConfigurationProperties(prefix = "minio")`).
- Docker service credentials in `docker-compose.yaml` (minio admin/password; postgres admin/admin).

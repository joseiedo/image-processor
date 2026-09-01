.PHONY: help build test run smoke package up down clean clean-redis

help:            ## List all targets
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

build:           ## Compile
	./mvnw -q compile

test:            ## Run tests
	./mvnw test

run:             ## Run the app (auto-starts docker-compose)
	./mvnw spring-boot:run

smoke:           ## End-to-end smoke test (app must be running)
	node scripts/e2e-smoke.js

package:         ## Build jar
	./mvnw package

up:              ## Start Minio/Redis/Postgres
	docker compose up -d

down:            ## Stop Minio/Redis/Postgres
	docker compose down

clean:           ## Clean build artifacts
	./mvnw clean

clean-redis:     ## Flush the image:* cache keys in Redis
	@container=$$(docker ps --filter ancestor=redis --format '{{.Names}}' | head -1); \
	docker exec "$$container" redis-cli --scan --pattern 'image:*' | xargs -r docker exec "$$container" redis-cli DEL

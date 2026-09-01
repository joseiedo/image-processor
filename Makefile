.PHONY: help build test run smoke ratelimit package up down clean clean-redis

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

ratelimit:       ## Burst test: verify the rate limiter blocks with 429
	node scripts/rate-limit-check.js

package:         ## Build jar
	./mvnw package

up:              ## Start Minio/Redis/Postgres
	docker compose up -d

down:            ## Stop Minio/Redis/Postgres
	docker compose down

clean:           ## Clean build artifacts
	./mvnw clean

clean-redis:     ## Flush app keys (image:* cache + ratelimit:*) in Redis
	@container=$$(docker ps --filter ancestor=redis --format '{{.Names}}' | head -1); \
	for pattern in 'image:*' 'ratelimit:*'; do \
	  docker exec "$$container" redis-cli --scan --pattern "$$pattern" | xargs -r docker exec "$$container" redis-cli DEL; \
	done

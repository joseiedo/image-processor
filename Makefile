.PHONY: help build test run package up down clean

help:            ## List all targets
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

build:           ## Compile
	./mvnw -q compile

test:            ## Run tests
	./mvnw test

run:             ## Run the app (auto-starts docker-compose)
	./mvnw spring-boot:run

package:         ## Build jar
	./mvnw package

up:              ## Start Minio/Redis/Postgres
	docker compose up -d

down:            ## Stop Minio/Redis/Postgres
	docker compose down

clean:           ## Clean build artifacts
	./mvnw clean

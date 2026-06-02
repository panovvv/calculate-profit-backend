# Deployment

Container packaging for the Calculate Profit backend.

## `Dockerfile`

A multi-stage build:

1. **build** — `maven:3.9-eclipse-temurin-21` compiles and packages the Spring Boot executable jar
   (`-DskipTests`; tests and Spotless run in the CI `build` job instead).
2. **runtime** — `eclipse-temurin:21-jre` runs the jar as a non-root user, exposes port `8080`, and
   declares a `HEALTHCHECK` against the Actuator health endpoint (`/actuator/health`).

The build context is the repository root.

### Build & run locally

```shell
# from the repository root
docker build -f deployment/Dockerfile -t calculate-profit-backend:local .
docker run --rm -p 8080:8080 calculate-profit-backend:local
# or simply:
docker compose -f deployment/docker-compose.yml up --build
```

The app starts on http://localhost:8080 with an empty in-memory H2 database (schema applied by Flyway).

## CI/CD

The GitHub Actions `docker-image` job (see [`.github/workflows/backend-ci.yml`](../.github/workflows/backend-ci.yml))
runs after the `build` job and:

1. builds this image, and
2. **pushes it — currently a no-op placeholder.** When a registry is chosen, replace the placeholder
   step with a login + `docker push` (image tag/registry are already parameterised as workflow env vars).

# Calculate Profit

[![Backend CI](https://github.com/panovvv/calculate-profit-backend/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/panovvv/calculate-profit-backend/actions/workflows/backend-ci.yml)
[![codecov](https://codecov.io/gh/panovvv/calculate-profit-backend/branch/main/graph/badge.svg)](https://codecov.io/gh/panovvv/calculate-profit-backend)

A Spring Boot service implementing the DACHSER "Calculate Profit" use case: it records the income and
costs of a shipment, computes the profit or loss (`Profit = Total Income - Total Costs`), stores it,
and exposes it over a REST API. We follow a hexagonal (ports-and-adapters) architecture:

- **domain/**: the pure core. No Spring, no JPA.
- **application/**: use-case services and ports (i.e. interfaces. `port/incoming`, `port/outgoing`).
- **adapter/incoming/**: REST controller, DTOs, MapStruct mapper, security and error handling.
- **adapter/outgoing/**: JPA entities, Spring Data repositories and the persistence adapters.

## Implementation notes:

- Money is `BigDecimal` everywhere (stored as `DECIMAL(12,2)`, normalised to 2 dp with `HALF_UP`).
- The domain is kept free of JPA: separate `*JpaEntity` classes live in the outgoing adapter and are
  mapped to/from the pure domain model — that is the headline hexagonal boundary.
- Flyway owns the schema (`src/main/resources/db/migration`, Hibernate runs `ddl-auto=validate`, 
- so the mappings can never drift from the migrated schema.
- No SQL seed scripts: tests generate randomized data in Java (`ProfitTestData`) for isolation and easy
  parallelization, rather than static inserts that are brittle to maintain.
- Versions: the brief suggested Java 17 / Spring Boot 3.1.5; this targets **Java 21 (LTS)** and the
  latest **Spring Boot 4.0.x** (Java 21 being the LTS available on the build machine).

## 1) Running locally

### Prerequisites
- JDK 21+
- Maven 3.9+ (or the bundled IDE Maven)
- Docker & Docker Compose (optional)

### Via Maven
From the project root:
```shell
mvn spring-boot:run
```
The app starts on port 8080. The API uses HTTP Basic auth (default `dachser` / `dachser`):
```shell
# 1000 income - 200 cost = 800 profit
curl -s -u dachser:dachser -X POST http://localhost:8080/api/profit/calculations \
  -H 'Content-Type: application/json' \
  -d '{"shipmentReference":"0001","income":1000,"cost":200,"additionalCost":0}'

curl -s -u dachser:dachser http://localhost:8080/api/profit/calculations   # ?shipment=0001 to filter
```
To call the API conveniently, import the live OpenAPI spec (`http://localhost:8080/v3/api-docs`, File →
Import → Link) in Postman or your preferred REST API client.

### Via Docker Compose
```shell
docker compose -f deployment/docker-compose.yml up --build
```
Both methods start the app on port 8080.

## 2) Deploying & Metrics

All Actuator endpoints (`/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`) are public so
they can be scraped without credentials. Besides the framework metrics there is a custom
`profit.requests` counter (tagged `endpoint`, `status`), recorded by `ProfitMetricsAspect` via the
`ProfitMetrics` outgoing port — so controllers stay free of metrics code and the core free of Micrometer.

Security is enabled:
- HTTP Basic, CSRF disabled (stateless API).
- Credentials from `spring.security.user.*`, overridable with `API_USER` / `API_PASSWORD`.
- Public: Swagger UI / OpenAPI docs, Actuator, and the dev H2 console; everything under `/api/**` requires auth.

OpenAPI is generated from the code by springdoc (no hand-maintained spec):
- Swagger UI: http://localhost:8080/swagger-ui.html (has an **Authorize** button for Basic auth)
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The H2 console (dev) is at http://localhost:8080/h2-console (URL `jdbc:h2:mem:profitdb`, user `sa`, no password).

The container image is a multi-stage [`deployment/Dockerfile`](deployment/Dockerfile) (runs as non-root,
with an Actuator `HEALTHCHECK`). CI builds it on every push; pushing is a no-op placeholder.
Build it directly with:
```shell
docker build -f deployment/Dockerfile -t calculate-profit-backend:local .
```

## 3) Contributing

### Tests

We got the following kinds of tests:

- **Unit tests**
- **Integration context tests** are marked with @SpringBootTest. Those bring up the full app context but nothing beside that.
- **REST API tests** extend `BaseRestApiTest` and make use of REST Assured to verify real API interactions with an app started on embedded server.
- **Architecture Tests**: we use ArchUnit to enforce hexagonal boundaries via `HexArchitectureTest`:
  - **domain/** must not depend on Spring, Jackson, application, or adapters
  - **application/** must not depend on incoming or outgoing adapters
  - **adapters/incoming/** must not call outgoing adapters directly
  - **adapters/outgoing/** must not depend on incoming adapters

Tests run in parallel by default (see `src/test/resources/junit-platform.properties`): methods run
concurrently, while the `@SpringBootTest` classes run sequentially so they share a single in-memory
database without racing on Flyway. Each test creates its own data under a unique reference, so no
cleanup or shared state is needed.

Run all tests (+ Spotless check + JaCoCo at `target/site/jacoco/index.html`):
```shell
mvn verify
```

### Code Formatting

We use Spotless to enforce consistent code style:

- Apply formatting:
```shell
mvn spotless:apply
```
- Check formatting (should be ran by CI/CD pipeline):
```shell
mvn spotless:check
```

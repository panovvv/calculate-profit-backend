# Calculate Profit

[![Backend CI](https://github.com/panovvv/calculate-profit-backend/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/panovvv/calculate-profit-backend/actions/workflows/backend-ci.yml)
[![codecov](https://codecov.io/gh/panovvv/calculate-profit-backend/branch/main/graph/badge.svg)](https://codecov.io/gh/panovvv/calculate-profit-backend)

A Spring Boot service implementing the DACHSER "Calculate Profit" use case: it records the income and
costs of a shipment, computes the profit or loss (`Profit = Total Income - Total Costs`), stores it,
and exposes it over a REST API. We follow a hexagonal (ports-and-adapters) architecture:

- **domain/**: the pure core — model + `ProfitCalculator`. No Spring, no JPA.
- **application/**: use-case service (`ProfitService`) and ports (`port/incoming`, `port/outgoing`).
- **adapter/incoming/web/**: REST controller, DTOs, MapStruct mapper, security and error handling.
- **adapter/outgoing/persistence/**: JPA entities, Spring Data repositories and the persistence adapters.

## Implementation notes:

- Money is `BigDecimal` everywhere (stored as `DECIMAL(12,2)`, normalised to 2 dp with `HALF_UP`).
- The domain is kept free of JPA: separate `*JpaEntity` classes live in the outbound adapter and are
  mapped to/from the pure domain model — that is the headline hexagonal boundary.
- Flyway owns the schema (`src/main/resources/db/migration`, one migration per table, datetime-versioned
  `V<yyyyMMddHHmmss>__*.sql`). Hibernate runs `ddl-auto=validate`, so the mappings can never drift from
  the migrated schema. The app starts with an **empty** database (no seed data).
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
Invalid amounts return `400` (the use case's "Data Retrieval Error" flow); missing credentials, `401`.
For a Postman collection, import the live OpenAPI spec (`http://localhost:8080/v3/api-docs`, File →
Import → Link) instead of maintaining a separate file.

### Via Docker Compose
```shell
docker compose -f deployment/docker-compose.yml up --build
```
Both methods start the app on port 8080.

## 2) Deploying & Metrics

All Actuator endpoints (`/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`) are public so
they can be scraped without credentials. Besides the framework metrics there is a custom
`profit.requests` counter (tagged `endpoint`, `status`), recorded by `ProfitMetricsAspect` via the
`ProfitMetrics` outbound port — so controllers stay free of metrics code and the core free of Micrometer.

Security is enabled:
- HTTP Basic, CSRF disabled (stateless API).
- Credentials from `spring.security.user.*`, overridable with `API_USER` / `API_PASSWORD`.
- Public: Swagger UI / OpenAPI docs, Actuator, and the dev H2 console; everything under `/api/**` requires auth.

OpenAPI is generated from the code by springdoc (no hand-maintained spec):
- Swagger UI: http://localhost:8080/swagger-ui.html (has an **Authorize** button for Basic auth)
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The H2 console (dev) is at http://localhost:8080/h2-console (URL `jdbc:h2:mem:profitdb`, user `sa`, no password).

The container image is a multi-stage [`deployment/Dockerfile`](deployment/Dockerfile) (runs as non-root,
with an Actuator `HEALTHCHECK`). CI builds it on every push; pushing is a no-op placeholder until a
registry is configured. Build it directly with:
```shell
docker build -f deployment/Dockerfile -t calculate-profit-backend:local .
```

## 3) Contributing

### Tests

We have the following kinds of tests:

- **Unit tests** — the domain rule (`ProfitCalculatorTest`) and the service with mocked ports (`ProfitServiceTest`).
- **Web tests** — `ProfitControllerTest` (standalone `MockMvc`) for mapping, validation and the JSON contract.
- **Integration tests** — `@SpringBootTest` bring up the full context against real H2 (`PersistenceAdapterTest`,
  which also proves Flyway + `ddl-auto=validate`; `SecurityIntegrationTest`, which checks auth on a real server).
- **Architecture tests** — `HexagonalArchitectureTest` (ArchUnit) enforces the hexagonal boundaries:
  - **domain/** must not depend on Spring, JPA, the application or the adapters
  - **application/** must not depend on the inbound or outbound adapters
  - **adapter/incoming/** must not call the outbound adapter directly, and vice-versa

Tests run in parallel by default (see `src/test/resources/junit-platform.properties`): methods run
concurrently, while the `@SpringBootTest` classes run sequentially so they share a single in-memory
database without racing on Flyway. Each test creates its own data under a unique reference, so no
cleanup or shared state is needed.

Run all tests (+ Spotless check + JaCoCo at `target/site/jacoco/index.html`):
```shell
mvn verify
```

### Code Formatting

We use Spotless (Google Java Format + import ordering), also run by CI and bound to `mvn verify`:
```shell
mvn spotless:apply   # apply
mvn spotless:check   # verify
```

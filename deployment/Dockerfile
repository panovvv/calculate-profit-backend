# syntax=docker/dockerfile:1
#
# Container image for the Calculate Profit backend. Build from the repo root:
#   docker build -f deployment/Dockerfile -t calculate-profit-backend:local .
#
# Tests and Spotless run in the CI `build` job (mvn verify); the image build skips them
# (-DskipTests, and `package` runs before the verify-bound spotless:check) to keep it fast.

# ---- Build stage: compile and package the Spring Boot executable jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests clean package

# ---- Runtime stage: minimal JRE, running as an unprivileged user ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user
RUN useradd --system --uid 10001 appuser
COPY --from=build /workspace/target/*.jar app.jar
USER appuser

EXPOSE 8080

# Liveness/readiness can be probed via the Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD ["sh", "-c", "wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

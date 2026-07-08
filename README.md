# Enterprise Marketplace Platform

Production-ready B2B marketplace backend platform (IndiaMART-like) built with Java 21, Spring Boot 3.5.x, and cloud-native microservices architecture.

## Overview

This repository contains the **Enterprise Project Bootstrap** — a Maven multi-module monorepo with shared libraries, API gateway, and domain microservice scaffolds. Business APIs, database migrations, and Docker Compose infrastructure will be added in subsequent milestones.

## Technology Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.x, Spring Cloud 2025.0.x |
| Security | Spring Security, Keycloak (future milestone) |
| Gateway | Spring Cloud Gateway |
| Database | Neon PostgreSQL (per service) |
| Cache | Redis |
| Messaging | Apache Kafka (KRaft) |
| Search | Elasticsearch |
| AI | Ollama |
| Observability | Prometheus, Grafana, ELK, OpenTelemetry, Jaeger |
| Migration | Flyway |
| Build | Maven |
| Quality | Checkstyle, Spotless, SonarQube, JaCoCo |
| Testing | JUnit 5, Mockito, Testcontainers |

## Modules

| Module | Port | Description |
|--------|------|-------------|
| `common-library` | — | Shared cross-cutting concerns |
| `gateway-service` | 8080 | API Gateway |
| `identity-service` | 8081 | Identity & authentication |
| `product-service` | 8082 | Product catalog |
| `seller-service` | 8083 | Seller management |
| `buyer-service` | 8084 | Buyer management |
| `category-service` | 8085 | Category taxonomy |
| `inventory-service` | 8086 | Inventory management |
| `pricing-service` | 8087 | Pricing engine |
| `workflow-service` | 8088 | Business workflows |
| `notification-service` | 8089 | Notifications |
| `search-service` | 8090 | Search indexing |
| `ai-service` | 8091 | AI capabilities |
| `audit-service` | 8092 | Audit trail |
| `subscription-service` | 8093 | Subscriptions |
| `report-service` | 8094 | Reporting |
| `admin-service` | 8095 | Administration |

## Prerequisites

- JDK 21
- Maven 3.9+
- Git

## Quick Start

```bash
# Build entire platform
mvn clean install

# Run a specific service (example: product-service)
mvn spring-boot:run -pl product-service -am

# Format code
mvn spotless:apply

# Run with coverage profile
mvn verify -Pcoverage
```

## Bootstrap Health Check (Postman)

Each servlet-based microservice exposes a bootstrap health endpoint:

```
GET http://localhost:{port}/api/v1/bootstrap/health
Headers:
  X-Correlation-Id: {optional-uuid}
  X-Request-Id: {optional-uuid}
```

## Project Structure

See [docs/folder-structure.md](docs/folder-structure.md) for the complete layout.

## Documentation

- [Architecture](docs/architecture.md)
- [Coding Standards](docs/coding-standards.md)
- [Folder Structure](docs/folder-structure.md)
- [Logging Standard](docs/logging-standard.md)
- [Branching Strategy](docs/branching-strategy.md)

## Configuration Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Local development (default) |
| `dev` | Development environment |
| `qa` | QA environment |
| `prod` | Production environment |

YAML templates are available under `config/templates/`.

## License

MIT — see [LICENSE](LICENSE).

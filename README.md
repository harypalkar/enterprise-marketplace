# Enterprise Marketplace Platform

Production-ready B2B marketplace backend platform (IndiaMART-like) built with Java 21, Spring Boot 3.5.x, and cloud-native microservices architecture.

## Overview

This repository contains the **Enterprise Marketplace Platform** — a Maven multi-module monorepo with shared libraries, API gateway, domain microservice scaffolds, and a complete **Infrastructure Foundation** (Docker Compose, Keycloak, Kafka, Redis, Elasticsearch, observability stack).

Business APIs for the core B2B domain (product, seller, buyer, category, inventory, pricing) are implemented with full CRUD. **Workflow**, **Notification**, **Audit**, **Subscription**, **Report**, and **Admin** services are production-ready. Remaining scaffolds (search, ai) will be added in subsequent milestones.

## Technology Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.x, Spring Cloud 2025.0.x |
| Security | Spring Security, Keycloak (OIDC/JWT) |
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
| `gateway-service` | 8080 | API Gateway (JWT, routing, tracing) |
| `identity-service` | 8081 | Identity, Redis, Kafka, Neon DB |
| `product-service` | 8082 | Product catalog (CRUD) |
| `seller-service` | 8083 | Seller management (CRUD) |
| `buyer-service` | 8084 | Buyer management (CRUD) |
| `category-service` | 8085 | Category taxonomy (CRUD) |
| `inventory-service` | 8086 | Inventory management (CRUD + reserve/release) |
| `pricing-service` | 8087 | Pricing engine (CRUD) |
| `workflow-service` | 8088 | Business workflow lifecycle (production-ready) |
| `notification-service` | 8089 | Multi-channel notifications (production-ready) |
| `search-service` | 8090 | Elasticsearch integration |
| `ai-service` | 8091 | Ollama integration |
| `audit-service` | 8092 | Central audit trail (production-ready) |
| `subscription-service` | 8093 | Subscriptions and billing plans (production-ready) |
| `report-service` | 8094 | Report generation and analytics (production-ready) |
| `admin-service` | 8095 | Platform administration (production-ready) |

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker Desktop (for local infrastructure)
- Git

## Quick Start

### 1. Start Infrastructure

```bash
cd docker
cp .env.example .env
docker compose --env-file .env up -d
```

### 2. Build Platform

```bash
mvn clean install
```

### 3. Run a Service

```powershell
cd gateway-service
mvn spring-boot:run
```

Repeat for `identity-service`, `search-service`, and `ai-service` as needed.

### 4. Verify Health

```
GET http://localhost:8080/actuator/health
GET http://localhost:8081/api/v1/infrastructure/health
GET http://localhost:8090/api/v1/infrastructure/health/elasticsearch
GET http://localhost:8091/api/v1/infrastructure/health/ollama
```

Import the Postman collection from `docs/postman/Enterprise-Marketplace-Infrastructure.postman_collection.json`.

## Project Structure

See [docs/folder-structure.md](docs/folder-structure.md) for the complete layout.

## Documentation

- [Architecture](docs/architecture.md)
- [Product Service](docs/product-service.md)
- [Domain Services](docs/domain-services.md)
- [Coding Standards](docs/coding-standards.md)
- [Folder Structure](docs/folder-structure.md)
- [Logging Standard](docs/logging-standard.md)
- [Branching Strategy](docs/branching-strategy.md)
- [Docker README](docker/README.md)

## Configuration Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Local development (default) |
| `dev` | Development environment |
| `qa` | QA environment |
| `prod` | Production environment |

## License

MIT — see [LICENSE](LICENSE).

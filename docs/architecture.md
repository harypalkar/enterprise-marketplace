# Architecture

## Enterprise Marketplace Platform

### Architectural Style

The platform follows **Hexagonal Architecture (Ports & Adapters)**, **Clean Architecture**, and **Domain-Driven Design (DDD)** principles within a **microservices** topology.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer (Postman / API Consumers)    │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                   Spring Cloud Gateway (8080)                    │
│         Routing │ Rate Limiting │ Correlation ID │ Auth          │
└───────────────────────────────┬─────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
┌───────▼──────┐      ┌────────▼────────┐     ┌───────▼──────┐
│ Identity     │      │ Product         │     │ Seller       │
│ Service      │      │ Service         │     │ Service      │
└───────┬──────┘      └────────┬────────┘     └───────┬──────┘
        │                      │                       │
        └──────────────────────┼───────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
      ┌───────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
      │ PostgreSQL   │  │ Redis       │  │ Kafka       │
      │ (per service)│  │ Cache       │  │ Events      │
      └──────────────┘  └─────────────┘  └─────────────┘
```

## Core Principles

### Database Per Service

Each microservice owns its PostgreSQL schema/database. Cross-service data access is prohibited; services communicate via APIs and events.

### Event-Driven Architecture

Domain events are published to Apache Kafka. The **Outbox Pattern** ensures reliable event delivery (to be implemented in domain milestones).

### API First

OpenAPI 3 specifications drive contract-first development. All REST APIs are versioned under `/api/v1/`.

### 12-Factor App

- Configuration via environment variables
- Stateless processes
- Port binding
- Disposability (graceful shutdown)
- Dev/prod parity

## Service Internal Structure (Hexagonal)

```
service/
├── domain/           # Entities, value objects, domain services, ports
├── application/      # Use cases, application services
├── infrastructure/   # Adapters: JPA, Kafka, Redis, external APIs
└── bootstrap/        # Spring configuration, controllers (driving adapters)
```

## Cross-Cutting Concerns (common-library)

| Component | Responsibility |
|-----------|----------------|
| `ApiResponse` | Standard success envelope |
| `ErrorResponse` | Standard error envelope |
| `GlobalExceptionHandler` | Centralized exception mapping |
| `CorrelationIdFilter` | Request tracing propagation |
| `RequestContext` | Thread-local context (correlation, tenant, user) |
| `IdempotencyAspect` | Safe retry for mutating operations |
| `BaseEntity` | UUID PK, audit columns, optimistic locking |
| `AuditModel` | Embeddable audit metadata |
| `LoggingUtility` | Structured logging helpers |
| `ValidationUtility` | Shared validation (GSTIN, PAN, mobile) |

## Security Architecture (Future Milestone)

- **Keycloak** as Identity Provider
- **OAuth 2.0 / OIDC** with JWT bearer tokens
- **Spring Security** resource server per microservice
- Gateway-level token validation and claim propagation

## Observability Stack

| Tool | Purpose |
|------|---------|
| OpenTelemetry | Distributed tracing instrumentation |
| Jaeger | Trace visualization |
| Prometheus | Metrics collection |
| Grafana | Dashboards and alerting |
| ELK (Elasticsearch, Logstash, Kibana) | Centralized logging |

## Deployment Topology

- **Docker Compose** — local development (future milestone)
- **Kubernetes** — production orchestration
- **Helm** — package management
- **Terraform** — cloud infrastructure as code
- **Jenkins / GitLab CI** — CI/CD pipelines

## Data Flow Example (Future)

```
Buyer Request → Gateway → Product Service → Inventory Service
                              │                    │
                              ▼                    ▼
                         PostgreSQL            Kafka Event
                              │                    │
                              ▼                    ▼
                         Outbox Table        Search Service → Elasticsearch
```

## Milestone Roadmap

1. **Bootstrap** (current) — Project structure, common library, service scaffolds
2. Infrastructure — Docker Compose, Keycloak, Kafka, Redis, Elasticsearch
3. Identity — Keycloak integration, user/role management
4. Domain Services — Business APIs per bounded context
5. Search & AI — Elasticsearch indexing, Ollama integration
6. Observability — Full ELK, Prometheus, Grafana dashboards
7. CI/CD — Jenkins/GitLab pipelines, Kubernetes deployment

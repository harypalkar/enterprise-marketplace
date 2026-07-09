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

## Security Architecture

- **Keycloak** as Identity Provider (`marketplace` realm)
- **OAuth 2.0 / OIDC** with JWT bearer tokens
- **Spring Security** resource server on gateway and identity service
- Gateway-level token validation and claim propagation (`X-User-Id`, `X-Roles`)
- Realm roles: `ADMIN`, `SELLER`, `BUYER`

## Infrastructure Stack (Current Milestone)

| Component | Integration Service | Health Endpoint |
|-----------|--------------------|-----------------|
| Keycloak | Gateway, Identity | JWT issuer validation |
| Neon PostgreSQL | Identity | Hikari pool (no business tables yet) |
| Redis | Identity | `/api/v1/infrastructure/health/redis` |
| Kafka | Identity | `/api/v1/infrastructure/health/kafka` |
| Elasticsearch | Search | `/api/v1/infrastructure/health/elasticsearch` |
| Ollama | AI | `/api/v1/infrastructure/health/ollama` |
| Prometheus / Grafana | All (Actuator) | `/actuator/prometheus` |
| Jaeger | All (OTLP) | Trace export |
| Mailhog | Notification (future) | SMTP capture |

See [infrastructure.md](infrastructure.md) for Docker Compose, environment variables, and run instructions.

| Tool | Purpose |
|------|---------|
| OpenTelemetry | Distributed tracing instrumentation |
| Jaeger | Trace visualization |
| Prometheus | Metrics collection |
| Grafana | Dashboards and alerting |
| ELK (Elasticsearch, Logstash, Kibana) | Centralized logging |

## Deployment Topology

- **Docker Compose** — local development ([docker/docker-compose.yml](../docker/docker-compose.yml))
- **Kubernetes** — production orchestration (future)
- **Helm** — package management (future)
- **Terraform** — cloud infrastructure as code (future)
- **Jenkins / GitLab CI** — CI/CD pipelines (future)

## Observability Stack

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

1. **Bootstrap** — Project structure, common library, service scaffolds
2. **Infrastructure** — Docker Compose, Keycloak, Kafka, Redis, Elasticsearch, gateway security
3. **Product Service** (current) — Product CRUD, Flyway, domain model
4. Identity — User/role management APIs
5. Domain Services — Business APIs per remaining bounded contexts
5. Search & AI — Indexing and LLM features
6. Observability — Full ELK, Grafana dashboards
7. CI/CD — Jenkins/GitLab pipelines, Kubernetes deployment

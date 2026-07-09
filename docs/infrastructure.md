# Infrastructure Foundation

## Overview

Milestone 2 delivers the **Infrastructure Foundation** for the Enterprise Marketplace Platform: Docker Compose stack, Spring Cloud Gateway security and routing, shared library extensions, and infrastructure health endpoints across identity, search, and AI services.

No business APIs, database entities, or Product CRUD are included in this milestone.

## Docker Compose Stack

Location: `docker/docker-compose.yml`

| Service | Port | Purpose |
|---------|------|---------|
| Keycloak | 8180 | Identity Provider (OIDC/JWT) |
| PostgreSQL (Keycloak) | 5432 (internal) | Keycloak database only |
| Redis | 6379 | Cache and session store |
| Kafka (KRaft) | 9092 | Event streaming |
| Kafka UI | 8088 | Kafka management UI |
| Elasticsearch | 9200 | Search engine |
| Kibana | 5601 | Log and search visualization |
| Ollama | 11434 | Local LLM runtime |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Dashboards |
| Jaeger | 16686 | Distributed tracing UI |
| Mailhog | 8025 / 1025 | SMTP testing |
| Zipkin (optional) | 9411 | Alternative tracing |

### Quick Start

```bash
cd docker
cp .env.example .env
docker compose --env-file .env up -d
docker compose ps
```

### Keycloak Realm

Imported from `docker/keycloak/marketplace-realm.json`:

| Item | Value |
|------|-------|
| Realm | `marketplace` |
| Roles | `ADMIN`, `SELLER`, `BUYER` |
| Gateway client | `marketplace-gateway` |
| Services client | `marketplace-services` |

Demo users (local only): `admin@marketplace.local`, `seller@marketplace.local`, `buyer@marketplace.local` (password: `changeme`).

## Microservice Infrastructure

### Gateway Service (8080)

- JWT authentication filter (Keycloak resource server)
- Correlation ID and request ID propagation
- Structured gateway logging filter
- Global reactive exception handler
- Routes: `/api/identity/**`, `/api/search/**`, `/api/ai/**`
- Actuator: health, info, prometheus, liveness, readiness

### Identity Service (8081)

- Neon PostgreSQL connection pool (Hikari, SSL)
- Redis client and health indicator
- Kafka producer/consumer configuration and topic bootstrap
- Keycloak JWT validation with realm role mapping
- Infrastructure health: `/api/v1/infrastructure/health`

### Search Service (8090)

- Elasticsearch REST client
- Elasticsearch health indicator
- Infrastructure health: `/api/v1/infrastructure/health/elasticsearch`

### AI Service (8091)

- Ollama WebClient
- Ollama health indicator
- Infrastructure health: `/api/v1/infrastructure/health/ollama`

## Environment Variables

| Variable | Description | Default (local) |
|----------|-------------|-----------------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `local` |
| `NEON_DB_URL` | Neon PostgreSQL JDBC URL | localhost |
| `NEON_DB_USER` / `NEON_DB_PASSWORD` | Database credentials | — |
| `NEON_DB_SSL` / `NEON_DB_SSL_MODE` | SSL settings | `true` / `prefer` |
| `REDIS_HOST` / `REDIS_PORT` | Redis connection | `localhost:6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `ELASTICSEARCH_URIS` | Elasticsearch cluster | `http://localhost:9200` |
| `OLLAMA_BASE_URL` | Ollama API | `http://localhost:11434` |
| `KEYCLOAK_ISSUER_URI` | JWT issuer | `http://localhost:8180/realms/marketplace` |
| `KEYCLOAK_JWK_SET_URI` | JWK set | Keycloak certs endpoint |
| `MARKETPLACE_SECURITY_ENABLED` | Enable JWT security | `true` (gateway prod), `false` (local optional) |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | Jaeger OTLP | `http://localhost:4318/v1/traces` |

## Configuration Profiles

Each infrastructure service supports `local`, `dev`, `qa`, and `prod` profiles under `src/main/resources/application-{profile}.yml`.

- **local** — verbose logging, Swagger enabled, security optionally disabled
- **dev/qa** — environment variables required, tracing sampling reduced in QA
- **prod** — Swagger disabled, minimal health details, reduced trace sampling

## Running Services Locally

```powershell
# 1. Start infrastructure
cd docker
docker compose up -d

# 2. Run services (from module directory)
cd gateway-service
mvn spring-boot:run

cd identity-service
mvn spring-boot:run

cd search-service
mvn spring-boot:run

cd ai-service
mvn spring-boot:run
```

## Health Endpoints

| Endpoint | Service | Description |
|----------|---------|-------------|
| `GET /actuator/health` | All | Spring Boot health |
| `GET /actuator/health/liveness` | All | Kubernetes liveness |
| `GET /actuator/health/readiness` | All | Kubernetes readiness |
| `GET /actuator/prometheus` | All | Prometheus metrics |
| `GET /api/v1/infrastructure/health/redis` | Identity | Redis connectivity |
| `GET /api/v1/infrastructure/health/kafka` | Identity | Kafka connectivity |
| `GET /api/v1/infrastructure/health/elasticsearch` | Search | Elasticsearch connectivity |
| `GET /api/v1/infrastructure/health/ollama` | AI | Ollama connectivity |

## Postman Collection

Import `docs/postman/Enterprise-Marketplace-Infrastructure.postman_collection.json` for ready-made health and actuator requests.

## Testing

Integration tests use Testcontainers for Redis, Kafka, PostgreSQL, and Elasticsearch. Run:

```bash
mvn clean install
```

Tests:

- `GatewayActuatorIntegrationTest`
- `IdentityInfrastructureHealthIntegrationTest`
- `SearchElasticsearchHealthIntegrationTest`
- `AiOllamaHealthIntegrationTest` (MockWebServer for Ollama)

## Dockerfiles

| File | Purpose |
|------|---------|
| `docker/Dockerfile.gateway` | Multi-stage build for gateway |
| `docker/Dockerfile.service` | Multi-stage build for servlet services |

## Next Milestone

After approval: Product Service APIs, domain entities, Flyway migrations, and business logic.

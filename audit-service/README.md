# Audit Service

Production-ready central audit trail microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8092 |
| Base path | `/api/v1/audits` |
| Gateway | `http://localhost:8080/api/audits/api/v1/audits` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (audit records + timelines) |
| Messaging | Kafka (consumes `audit-created`, publishes `audit-indexed`) |

## Architecture

- **Immutable audit records** — append-only with soft archive for compliance
- **Correlation timelines** — ordered audit chain per `correlationId`
- **Kafka ingestion** from Workflow, Notification, Product services via `audit-created`
- **Transactional outbox** for `audit-indexed` and `audit-archived` events
- **Idempotent event keys** — `{sourceService}:{requestId}:{operation}`

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/audits` | Create audit record (`Idempotency-Key`) |
| GET | `/api/v1/audits/{id}` | Get audit by ID |
| GET | `/api/v1/audits/request/{requestId}` | Get by request ID |
| GET | `/api/v1/audits/correlation/{correlationId}` | Get correlation timeline |
| GET | `/api/v1/audits/aggregate` | Search by aggregate type + ID |
| GET | `/api/v1/audits/actor/{actor}` | Search by actor |
| GET | `/api/v1/audits/source/{sourceService}` | Search by source service |
| GET | `/api/v1/audits/search` | Advanced search with filters |
| DELETE | `/api/v1/audits/{id}` | Archive (ADMIN only) |

## Database Tables

- `audit_record`, `audit_event_log`, `audit_timeline`, `outbox_event`

Flyway: `V1__create_audit_domain_tables.sql`

## Kafka Topics

**Consumed:** `audit-created`  
**Published:** `audit-indexed`, `audit-archived`  
**DLQ:** `audit-dead-letter`

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd audit-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl audit-service -am
```

## Documentation

- [Audit Architecture](../docs/audit-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Audit.postman_collection.json)

# Workflow Service

Production-ready workflow lifecycle microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8088 |
| Base path | `/api/v1/workflows` |
| Gateway | `http://localhost:8080/api/workflows/api/v1/workflows` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (workflow status, transition rules) |
| Messaging | Kafka (transactional outbox + event consumers) |

## Architecture

- **Hexagonal / clean architecture** with packages: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `workflow`, `outbox`, `audit`, `validation`, `config`, `security`, `redis`, `kafka`
- **Workflow engine** validates status transitions against DB rules (cached in Redis)
- **Transactional outbox** for reliable Kafka publishing
- **Kafka consumers** integrate with Product Service events (`product-created`, `product-updated`, `workflow-updated`)

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/workflows` | Create workflow (`Idempotency-Key`) |
| GET | `/api/v1/workflows/{workflowId}` | Get workflow by ID |
| GET | `/api/v1/workflows/request/{requestId}` | Search by request ID |
| GET | `/api/v1/workflows/status/{status}` | Search by status (paginated) |
| PUT | `/api/v1/workflows/{workflowId}` | Update workflow metadata |
| PATCH | `/api/v1/workflows/{workflowId}/status` | Update workflow status |
| DELETE | `/api/v1/workflows/{workflowId}` | Soft delete |

## Workflow Statuses

`INITIAL` → `RECEIVED` → `TECHNICAL_VALIDATION` → `BUSINESS_VALIDATION` → `REDIS_VALIDATION` → `DATABASE_SAVED` → `OUTBOX_CREATED` → `EVENT_PUBLISHED` → `SEARCH_UPDATED` → `NOTIFICATION_SENT` → `AI_COMPLETED` → `COMPLETED`

Terminal/alternate: `FAILED`, `RETRY`, `CANCELLED`, `AMENDED`, `ROLLBACK`

## Database Tables

- `workflow`, `workflow_history`, `workflow_transition`, `workflow_event`, `workflow_audit`, `outbox_event`

Flyway: `V1__create_workflow_domain_tables.sql`

## Kafka Topics

**Published:** `workflow-created`, `workflow-updated`, `workflow-completed`, `workflow-failed`, `workflow-cancelled`, `audit-created`, `notification-created`

**Consumed:** `product-created`, `product-updated`, `workflow-updated`

**DLQ:** `workflow-dead-letter`

## Run Locally

### With Docker infrastructure

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd docker
docker compose up -d
cd ../workflow-service
mvn spring-boot:run
```

### Standalone (H2, no Kafka/Redis)

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl workflow-service -am
```

## Documentation

- [Workflow Architecture](../docs/workflow-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Workflow.postman_collection.json)

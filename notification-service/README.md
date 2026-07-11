# Notification Service

Production-ready notification delivery microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8089 |
| Base path | `/api/v1/notifications` |
| Gateway | `http://localhost:8080/api/notifications/api/v1/notifications` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (template cache) |
| Messaging | Kafka (transactional outbox + event consumers) |

## Architecture

- **Hexagonal / clean architecture** with packages: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `channel`, `outbox`, `audit`, `validation`, `config`, `security`, `redis`, `kafka`
- **Multi-channel delivery**: EMAIL, SMS, PUSH, IN_APP, WEBHOOK
- **Template engine** with `{{variable}}` substitution (DB templates + Redis cache)
- **Transactional outbox** for reliable Kafka publishing
- **Kafka consumers** for `notification-created`, `workflow-completed`, `workflow-failed`

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/notifications` | Create notification (`Idempotency-Key`) |
| GET | `/api/v1/notifications/{id}` | Get notification by ID |
| GET | `/api/v1/notifications/request/{requestId}` | Search by request ID |
| GET | `/api/v1/notifications/recipient/{recipientId}` | Search by recipient |
| GET | `/api/v1/notifications/status/{status}` | Search by status |
| PUT | `/api/v1/notifications/{id}` | Update notification |
| PATCH | `/api/v1/notifications/{id}/status` | Update status |
| POST | `/api/v1/notifications/{id}/retry` | Retry failed notification |
| DELETE | `/api/v1/notifications/{id}` | Soft delete |
| GET | `/api/v1/inbox/{recipientId}` | Get in-app inbox |

## Notification Statuses

`PENDING` → `QUEUED` → `PROCESSING` → `SENT` → `DELIVERED` | `FAILED` → `RETRY` | `CANCELLED`

## Database Tables

- `notification_template`, `notification`, `notification_delivery`, `notification_inbox`, `notification_audit`, `outbox_event`

Flyway: `V1__create_notification_domain_tables.sql`

## Kafka Topics

**Published:** `notification-sent`, `notification-failed`, `notification-delivered`, `audit-created`

**Consumed:** `notification-created`, `workflow-completed`, `workflow-failed`

**DLQ:** `notification-dead-letter`

## Run Locally

### Standalone (H2, no Kafka/Redis)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd notification-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl notification-service -am
```

## Documentation

- [Notification Architecture](../docs/notification-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Notification.postman_collection.json)

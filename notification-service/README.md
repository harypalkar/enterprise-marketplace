# Notification Service

Production-ready notification delivery microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8089 |
| Base path | `/api/v1/notifications` |
| Gateway | `http://localhost:8080/api/notifications/api/v1/notifications` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (templates, channel config, rate limits, user preferences) |
| Messaging | Kafka (transactional outbox + 8 domain event consumers) |

## Architecture

- **Hexagonal / clean architecture** with packages: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `provider`, `template`, `channel`, `outbox`, `audit`, `exception`, `validation`, `config`, `security`, `redis`, `kafka`
- **Multi-channel delivery**: EMAIL (SMTP/SES), SMS (Twilio), PUSH (FCM), IN_APP, WEBHOOK
- **Template engine** with HTML, plain text, SMS, push JSON, and webhook payload templates
- **Transactional outbox** for reliable Kafka publishing with retry and dead-letter
- **Kafka consumers** for product, workflow, seller, buyer, inventory, and subscription events

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/notifications` | Create notification (`Idempotency-Key`) |
| POST | `/api/v1/notifications/send` | Create and dispatch immediately |
| POST | `/api/v1/notifications/retry` | Bulk retry failed notifications |
| GET | `/api/v1/notifications` | List all notifications (paginated) |
| GET | `/api/v1/notifications/{id}` | Get notification by ID |
| GET | `/api/v1/notifications/request/{requestId}` | Search by request ID |
| GET | `/api/v1/notifications/recipient/{recipientId}` | Search by recipient |
| GET | `/api/v1/notifications/status/{status}` | Search by status |
| PUT | `/api/v1/notifications/{id}` | Update notification |
| PATCH | `/api/v1/notifications/{id}/status` | Update status |
| POST | `/api/v1/notifications/{id}/retry` | Retry single notification |
| DELETE | `/api/v1/notifications/{id}` | Soft delete |
| GET | `/api/v1/inbox/recipient/{recipientId}` | Get in-app inbox |

## Notification Statuses

`CREATED` → `QUEUED` → `PROCESSING` → `SENT` → `DELIVERED` | `FAILED` → `RETRYING` | `CANCELLED` | `EXPIRED`

## Database Tables

- `notification_template`, `notification`, `notification_history`, `notification_channel`, `notification_retry`, `notification_delivery`, `notification_inbox`, `notification_audit`, `outbox_event`

Flyway: `V1__create_notification_domain_tables.sql`, `V2__extend_notification_schema.sql`

## Kafka Topics

**Published:** `notification-sent`, `notification-failed`, `notification-retry`, `notification-delivered`, `audit-created`

**Consumed:** `notification-created`, `product-created`, `product-updated`, `workflow-completed`, `workflow-failed`, `seller-approved`, `buyer-registered`, `inventory-low`, `subscription-expired`

**DLQ:** `notification-dead-letter`

## Provider Configuration

| Channel | Provider | Environment Variables |
|---------|----------|----------------------|
| EMAIL | SMTP / SES | `NOTIFICATION_EMAIL_PROVIDER`, `SMTP_*`, `AWS_REGION` |
| SMS | Twilio / HTTP | `TWILIO_ENABLED`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`, `SMS_GATEWAY_URL` |
| PUSH | FCM / HTTP | `FCM_ENABLED`, `FCM_SERVICE_ACCOUNT_PATH`, `PUSH_GATEWAY_URL` |
| WEBHOOK | REST | `WEBHOOK_TIMEOUT_MS` |
| IN_APP | Database | — |

See [Provider Integration Guide](../docs/notification-provider-integration-guide.md).

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
- [Provider Integration Guide](../docs/notification-provider-integration-guide.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Notification.postman_collection.json)

# Subscription Service

Production-ready subscription and billing plan microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8093 |
| Base paths | `/api/v1/plans`, `/api/v1/subscriptions` |
| Gateway | `http://localhost:8080/api/subscriptions/api/v1/...` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (plans + subscriptions) |
| Messaging | Kafka (publishes lifecycle events, consumes `workflow-completed`) |

## Architecture

- **Plan catalog** — seeded FREE, BASIC, PREMIUM tiers with JSONB feature sets
- **Subscription lifecycle** — subscribe, status updates, cancel, renew with billing records
- **Transactional outbox** for `subscription-created`, `subscription-updated`, `subscription-cancelled`, `audit-created`
- **Local audit trail** via `subscription_audit` table
- **Idempotent subscribe** via `request_id` unique constraint and `Idempotency-Key` header

## API Endpoints

### Plans

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/plans` | Public | List active plans |
| GET | `/api/v1/plans/{id}` | Public | Get plan by ID |
| GET | `/api/v1/plans/code/{planCode}` | Public | Get plan by code |
| POST | `/api/v1/plans` | ADMIN | Create plan |
| PUT | `/api/v1/plans/{id}` | ADMIN | Update plan |
| DELETE | `/api/v1/plans/{id}` | ADMIN | Deactivate plan |

### Subscriptions

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/v1/subscriptions` | SELLER, ADMIN | Subscribe (`Idempotency-Key`) |
| GET | `/api/v1/subscriptions/{id}` | SELLER, ADMIN | Get subscription |
| GET | `/api/v1/subscriptions/seller/{sellerId}` | SELLER, ADMIN | List by seller |
| GET | `/api/v1/subscriptions/buyer/{buyerId}` | SELLER, ADMIN | List by buyer |
| PATCH | `/api/v1/subscriptions/{id}/status` | SELLER, ADMIN | Update status |
| DELETE | `/api/v1/subscriptions/{id}` | SELLER, ADMIN | Cancel subscription |
| POST | `/api/v1/subscriptions/{id}/renew` | SELLER, ADMIN | Renew subscription |

## Database Tables

- `subscription_plan`, `subscription`, `subscription_billing`, `subscription_audit`, `outbox_event`

Flyway: `V1__create_subscription_domain_tables.sql`

## Kafka Topics

**Published:** `subscription-created`, `subscription-updated`, `subscription-cancelled`, `audit-created`  
**Consumed:** `workflow-completed` (optional activation of PENDING subscriptions)  
**DLQ:** `subscription-dead-letter`

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd subscription-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl subscription-service -am
```

## Documentation

- [Subscription Architecture](../docs/subscription-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Subscription.postman_collection.json)

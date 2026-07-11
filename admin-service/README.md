# Admin Service

Production-ready platform administration microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8095 |
| Base path | `/api/v1/admin` |
| Gateway | `http://localhost:8080/api/admin/api/v1/admin` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (settings + feature flags) |
| Messaging | Kafka (`admin-config-changed`, `admin-feature-toggled`, `audit-created`) |

## Architecture

- **Platform settings** — key/value configuration with categories and soft-active flag
- **Feature flags** — toggle and rollout percentage control
- **Admin configs** — scoped JSONB configuration documents
- **Admin audit trail** — local immutable record of all admin mutations
- **Transactional outbox** for reliable Kafka event publishing
- **Dashboard** — aggregated counts from domain tables and platform metrics

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/settings` | List settings (optional `category` filter) |
| POST | `/api/v1/admin/settings` | Create setting |
| PUT | `/api/v1/admin/settings/{settingKey}` | Update setting |
| DELETE | `/api/v1/admin/settings/{settingKey}` | Delete setting |
| GET | `/api/v1/admin/feature-flags` | List feature flags |
| PATCH | `/api/v1/admin/feature-flags` | Bulk patch feature flags |
| GET | `/api/v1/admin/feature-flags/{flagKey}` | Get feature flag |
| PATCH | `/api/v1/admin/feature-flags/{flagKey}` | Patch feature flag |
| GET | `/api/v1/admin/configs` | List configs (optional `scope` filter) |
| POST | `/api/v1/admin/configs` | Create config |
| PUT | `/api/v1/admin/configs/{configKey}` | Update config |
| GET | `/api/v1/admin/dashboard` | Platform dashboard stats |

All `/api/v1/admin/**` endpoints require **ADMIN** role. Bootstrap and actuator endpoints are public.

## Database Tables

- `platform_setting`, `feature_flag`, `admin_config`, `admin_audit`, `platform_stat`, `outbox_event`

Flyway: `V1__create_admin_domain_tables.sql`

## Kafka Topics

**Published:** `admin-config-changed`, `admin-feature-toggled`, `audit-created`  
**DLQ:** `admin-dead-letter`

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd admin-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl admin-service -am
```

## Documentation

- [Admin Architecture](../docs/admin-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Admin.postman_collection.json)

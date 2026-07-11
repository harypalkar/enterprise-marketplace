# Report Service

Production-ready report generation microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8094 |
| Base path | `/api/v1/reports` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (jobs, results, definitions) |
| Messaging | Kafka (consumes domain events, publishes report lifecycle events) |

## Architecture

- **Report definitions** — seeded catalog (`SALES_SUMMARY`, `WORKFLOW_STATUS`, `INVENTORY_SNAPSHOT`)
- **Async report jobs** — PENDING → PROCESSING → COMPLETED/FAILED lifecycle
- **Scheduled generation engine** — processes pending jobs on a fixed interval
- **Kafka ingestion** from Workflow and Product services
- **Transactional outbox** for `report-generated`, `report-failed`, and `audit-created`
- **Domain audit trail** via `report_audit` table

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/reports/jobs` | Create report job (`Idempotency-Key`) |
| GET | `/api/v1/reports/jobs/{id}` | Get job by ID |
| GET | `/api/v1/reports/jobs` | List jobs with filters |
| GET | `/api/v1/reports/jobs/{id}/result` | Get completed report result |
| DELETE | `/api/v1/reports/jobs/{id}` | Cancel job (ADMIN only) |
| GET | `/api/v1/reports/definitions` | List active report definitions |
| GET | `/api/v1/reports/definitions/{code}` | Get definition by code |

## Database Tables

- `report_definition`, `report_job`, `report_result`, `report_audit`, `outbox_event`

Flyway: `V1__create_report_domain_tables.sql`

## Kafka Topics

**Consumed:** `workflow-completed`, `product-created`  
**Published:** `report-generated`, `report-failed`, `audit-created`  
**DLQ:** `report-dead-letter`

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd report-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl report-service -am
```

## Documentation

- [Report Architecture](../docs/report-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Report.postman_collection.json)

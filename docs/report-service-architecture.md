# Report Service Architecture

## ER Diagram

```mermaid
erDiagram
    REPORT_DEFINITION ||--o{ REPORT_JOB : defines
    REPORT_JOB ||--o| REPORT_RESULT : produces
    REPORT_JOB ||--o{ REPORT_AUDIT : audited_by
    REPORT_JOB ||--o{ OUTBOX_EVENT : publishes

    REPORT_DEFINITION {
        uuid id PK
        string report_code UK
        string name
        string report_type
        text query_template
        jsonb parameters_schema
        boolean active
    }

    REPORT_JOB {
        uuid id PK
        string request_id UK
        string report_code FK
        string requested_by
        string status
        jsonb parameters
        timestamptz started_at
        timestamptz completed_at
    }

    REPORT_RESULT {
        uuid id PK
        uuid job_id FK
        jsonb result_data
        int row_count
        string file_url
    }

    REPORT_AUDIT {
        uuid id PK
        uuid job_id FK
        string operation
        jsonb before_state
        jsonb after_state
    }
```

## Sequence Diagram — Report Generation

```mermaid
sequenceDiagram
    participant Client
    participant RS as Report Service
    participant DB as PostgreSQL
    participant Engine as Generation Engine
    participant Outbox
    participant Kafka

    Client->>RS: POST /reports/jobs
    RS->>DB: INSERT report_job (PENDING)
    RS->>DB: INSERT report_audit
    RS-->>Client: 201 Created

    loop Every process-interval-ms
        Engine->>DB: SELECT PENDING jobs
        Engine->>DB: UPDATE status PROCESSING
        Engine->>DB: INSERT report_result
        Engine->>DB: UPDATE status COMPLETED
        Engine->>Outbox: report-generated + audit-created
    end

    Outbox->>Kafka: publish events
    Client->>RS: GET /reports/jobs/{id}/result
    RS-->>Client: JSON result data
```

## Sequence Diagram — Kafka Event Ingestion

```mermaid
sequenceDiagram
    participant WS as Workflow Service
    participant PS as Product Service
    participant Kafka
    participant RS as Report Service
    participant DB as PostgreSQL

    WS->>Kafka: workflow-completed
    Kafka->>RS: consume event
    RS->>DB: INSERT report_audit (EVENT_RECEIVED)
    RS->>RS: buffer event for report context
    RS->>DB: optional auto-create WORKFLOW_STATUS job

    PS->>Kafka: product-created
    Kafka->>RS: consume event
    RS->>DB: INSERT report_audit (EVENT_RECEIVED)
    RS->>RS: buffer event for inventory snapshot
    RS->>DB: optional auto-create INVENTORY_SNAPSHOT job
```

## Integration

| Source Service | Topic | Report Impact |
|----------------|-------|---------------|
| Workflow Service | `workflow-completed` | Buffers workflow metrics; may auto-trigger `WORKFLOW_STATUS` |
| Product Service | `product-created` | Buffers product events; may auto-trigger `INVENTORY_SNAPSHOT` |

Downstream consumers of `report-generated` and `audit-created` integrate via transactional outbox delivery.

## Seeded Report Definitions

| Code | Type | Description |
|------|------|-------------|
| `SALES_SUMMARY` | ANALYTICS | Revenue and order breakdown by date range |
| `WORKFLOW_STATUS` | OPERATIONAL | Workflow counts grouped by status |
| `INVENTORY_SNAPSHOT` | SNAPSHOT | Point-in-time inventory levels |

## Security

| Operation | Roles |
|-----------|-------|
| GET (read/list) | ADMIN, SELLER |
| POST (create job) | ADMIN, SELLER |
| DELETE (cancel) | ADMIN only |

Report jobs are processed asynchronously. Results are available only when job status is `COMPLETED`.

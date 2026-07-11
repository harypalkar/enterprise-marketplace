# Audit Service Architecture

## ER Diagram

```mermaid
erDiagram
    AUDIT_RECORD ||--o{ AUDIT_EVENT_LOG : ingested_from
    AUDIT_RECORD ||--o| AUDIT_TIMELINE : sequenced_in
    AUDIT_RECORD ||--o{ OUTBOX_EVENT : publishes

    AUDIT_RECORD {
        uuid id PK
        string event_key UK
        string request_id
        string correlation_id
        string source_service
        string operation
        jsonb before_state
        jsonb after_state
        string status
    }

    AUDIT_EVENT_LOG {
        uuid id PK
        uuid audit_record_id FK
        string event_source
        jsonb payload
        boolean processed
    }

    AUDIT_TIMELINE {
        uuid id PK
        string correlation_id
        uuid audit_record_id FK
        bigint sequence_number
    }
```

## Sequence Diagram — Kafka Ingestion

```mermaid
sequenceDiagram
    participant Svc as Domain Service
    participant Kafka
    participant AS as Audit Service
    participant DB as PostgreSQL
    participant Outbox

    Svc->>Kafka: audit-created (via outbox)
    Kafka->>AS: consume event
    AS->>DB: INSERT audit_event_log
    AS->>DB: INSERT audit_record (idempotent eventKey)
    AS->>DB: INSERT audit_timeline
    AS->>Outbox: audit-indexed
    Outbox->>Kafka: publish indexed event
```

## Integration

| Source Service | Topic | Payload Fields |
|----------------|-------|----------------|
| Workflow Service | `audit-created` | workflowId, requestId, correlationId, operation, status, actor |
| Notification Service | `audit-created` | notificationId, requestId, operation, status |
| Product Service | `audit-created` | productId, requestId, operation |

All services publish audit events via transactional outbox. Audit Service is the single source of truth for cross-service audit queries.

## Security

| Operation | Roles |
|-----------|-------|
| GET (search/read) | ADMIN, SELLER |
| POST (create) | ADMIN, SELLER |
| DELETE (archive) | ADMIN only |

Audit records are immutable — no UPDATE endpoint. Archive is a soft-delete for compliance retention policies.

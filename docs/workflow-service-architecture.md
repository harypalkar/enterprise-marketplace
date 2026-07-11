# Workflow Service Architecture

## Overview

The Workflow Service tracks the complete lifecycle of every business transaction across the Enterprise Marketplace Platform. Every microservice creates or updates workflow records via REST API or Kafka events.

## ER Diagram

```mermaid
erDiagram
    WORKFLOW ||--o{ WORKFLOW_HISTORY : has
    WORKFLOW ||--o{ WORKFLOW_EVENT : receives
    WORKFLOW ||--o{ WORKFLOW_AUDIT : audited_by
    WORKFLOW ||--o{ OUTBOX_EVENT : publishes
    WORKFLOW_TRANSITION ||--|| WORKFLOW : governs

    WORKFLOW {
        uuid id PK
        string request_id UK
        string correlation_id
        string aggregate_type
        uuid aggregate_id
        string operation_type
        string status
        string previous_status
        boolean active
        jsonb metadata
    }

    WORKFLOW_HISTORY {
        uuid id PK
        uuid workflow_id FK
        string from_status
        string to_status
        string transition_reason
        timestamp created_at
    }

    WORKFLOW_TRANSITION {
        uuid id PK
        string from_status
        string to_status
        boolean active
    }

    WORKFLOW_EVENT {
        uuid id PK
        uuid workflow_id FK
        string event_type
        string event_source
        jsonb payload
    }

    WORKFLOW_AUDIT {
        uuid id PK
        uuid workflow_id FK
        string operation
        string before_status
        string after_status
        jsonb before_state
        jsonb after_state
    }

    OUTBOX_EVENT {
        uuid id PK
        string aggregate_type
        uuid aggregate_id
        string event_type
        string topic
        jsonb payload
        string status
    }
```

## State Diagram

```mermaid
stateDiagram-v2
    [*] --> INITIAL
    INITIAL --> RECEIVED
    RECEIVED --> TECHNICAL_VALIDATION
    TECHNICAL_VALIDATION --> BUSINESS_VALIDATION
    TECHNICAL_VALIDATION --> FAILED
    BUSINESS_VALIDATION --> REDIS_VALIDATION
    BUSINESS_VALIDATION --> FAILED
    REDIS_VALIDATION --> DATABASE_SAVED
    REDIS_VALIDATION --> FAILED
    DATABASE_SAVED --> OUTBOX_CREATED
    OUTBOX_CREATED --> EVENT_PUBLISHED
    EVENT_PUBLISHED --> SEARCH_UPDATED
    SEARCH_UPDATED --> NOTIFICATION_SENT
    NOTIFICATION_SENT --> AI_COMPLETED
    NOTIFICATION_SENT --> COMPLETED
    AI_COMPLETED --> COMPLETED
    FAILED --> RETRY
    RETRY --> RECEIVED
    COMPLETED --> AMENDED
    AMENDED --> RECEIVED
    FAILED --> ROLLBACK
    ROLLBACK --> CANCELLED
    INITIAL --> CANCELLED
    RECEIVED --> CANCELLED
    COMPLETED --> [*]
    CANCELLED --> [*]
```

## Sequence Diagram — Create Workflow

```mermaid
sequenceDiagram
    participant Client
    participant API as WorkflowController
    participant Svc as WorkflowServiceImpl
    participant Val as TransitionValidator
    participant DB as PostgreSQL
    participant Redis
    participant Outbox as OutboxEvent
    participant Scheduler as OutboxPublisher
    participant Kafka

    Client->>API: POST /api/v1/workflows
    API->>Svc: createWorkflow()
    Svc->>Val: validateRequestIdUnique()
    Svc->>DB: INSERT workflow
    Svc->>DB: INSERT workflow_history
    Svc->>DB: INSERT workflow_audit
    Svc->>Outbox: INSERT outbox_event
    Svc->>Redis: cache workflow
    API-->>Client: 201 Created

    Scheduler->>Outbox: read PENDING events
    Scheduler->>Kafka: publish workflow-created
    Scheduler->>Outbox: mark PUBLISHED
```

## Kafka Event Flow

```mermaid
flowchart LR
    subgraph Producers
        WS[Workflow Service]
        PS[Product Service]
    end

    subgraph Kafka Topics
        WC[workflow-created]
        WU[workflow-updated]
        WComp[workflow-completed]
        WF[workflow-failed]
        WCan[workflow-cancelled]
        AC[audit-created]
        NC[notification-created]
        PC[product-created]
        PU[product-updated]
    end

    subgraph Consumers
        WS2[Workflow Service]
        AS[Audit Service]
        NS[Notification Service]
        SS[Search Service]
        AIS[AI Service]
    end

    WS --> WC & WU & WComp & WF & WCan & AC & NC
    PS --> PC & PU & WU
    PC & PU & WU --> WS2
    AC --> AS
    NC --> NS
    WU --> SS
    WComp --> AIS
```

## Integration Points

| Service | Integration | Direction |
|---------|-------------|-----------|
| Product Service | `product-created`, `product-updated` | Kafka → Workflow |
| Audit Service | `audit-created` | Workflow → Kafka |
| Notification Service | `notification-created` | Workflow → Kafka |
| Search Service | `workflow-updated`, `workflow-completed` | Workflow → Kafka |
| AI Service | `workflow-completed` | Workflow → Kafka |

All integration is event-driven via Kafka. No cross-service database access.

## Database Diagram

```
workflow_service (Neon PostgreSQL)
├── workflow              — active workflow records
├── workflow_history      — status transition log
├── workflow_transition   — allowed transition rules (seed data)
├── workflow_event        — inbound Kafka event log
├── workflow_audit        — audit trail with before/after state
└── outbox_event          — transactional outbox for Kafka
```

## Redis Cache

| Key Pattern | Purpose | TTL |
|-------------|---------|-----|
| `workflow:{id}` | Workflow response cache | 3600s |
| `workflow:transition-rules` | Allowed status transitions | 3600s |

## Outbox Pattern

1. Workflow mutation writes `outbox_event` in same DB transaction
2. `OutboxPublisherService` polls PENDING events every 5s
3. Events published to Kafka with correlation/request headers
4. Failed publishes retry up to 5 times, then move to `workflow-dead-letter`

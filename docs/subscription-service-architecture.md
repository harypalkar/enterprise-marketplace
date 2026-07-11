# Subscription Service Architecture

## ER Diagram

```mermaid
erDiagram
    SUBSCRIPTION_PLAN ||--o{ SUBSCRIPTION : offers
    SUBSCRIPTION ||--o{ SUBSCRIPTION_BILLING : billed_by
    SUBSCRIPTION ||--o{ SUBSCRIPTION_AUDIT : tracked_in
    SUBSCRIPTION ||--o{ OUTBOX_EVENT : publishes

    SUBSCRIPTION_PLAN {
        uuid id PK
        string plan_code UK
        string name
        string tier
        decimal price
        string currency
        string billing_cycle
        jsonb features
        boolean active
    }

    SUBSCRIPTION {
        uuid id PK
        string request_id UK
        uuid seller_id
        uuid buyer_id
        uuid plan_id FK
        string status
        date start_date
        date end_date
        boolean auto_renew
        boolean active
    }

    SUBSCRIPTION_BILLING {
        uuid id PK
        uuid subscription_id FK
        decimal amount
        string currency
        date billing_date
        string status
    }

    SUBSCRIPTION_AUDIT {
        uuid id PK
        uuid subscription_id FK
        string operation
        string before_status
        string after_status
        jsonb before_state
        jsonb after_state
    }
```

## Sequence Diagram — Subscribe Flow

```mermaid
sequenceDiagram
    participant Client
    participant SS as Subscription Service
    participant DB as PostgreSQL
    participant Outbox
    participant Kafka

    Client->>SS: POST /api/v1/subscriptions
    SS->>DB: INSERT subscription
    SS->>DB: INSERT subscription_billing
    SS->>DB: INSERT subscription_audit
    SS->>Outbox: subscription-created + audit-created
    Outbox->>Kafka: publish events
    SS-->>Client: 201 Created
```

## Sequence Diagram — Workflow Activation

```mermaid
sequenceDiagram
    participant WS as Workflow Service
    participant Kafka
    participant SS as Subscription Service
    participant DB as PostgreSQL

    WS->>Kafka: workflow-completed
    Kafka->>SS: consume event
    SS->>DB: find subscription by requestId
    SS->>DB: UPDATE status PENDING → ACTIVE
    SS->>DB: INSERT subscription_audit
    SS->>Outbox: subscription-updated
```

## Integration

| Target Service | Topic | Purpose |
|----------------|-------|---------|
| Audit Service | `audit-created` | Cross-service audit trail |
| Notification Service | `subscription-created` | Welcome / billing notifications |
| Workflow Service | `workflow-completed` (inbound) | Activate pending subscriptions |

## Security

| Operation | Roles |
|-----------|-------|
| GET plans | Public |
| Plan mutations | ADMIN |
| Subscription read | SELLER, ADMIN |
| Subscription mutations | SELLER, ADMIN |

## Seeded Plans

| Code | Tier | Price | Billing |
|------|------|-------|---------|
| FREE | FREE | $0.00 | NONE |
| BASIC | BASIC | $9.99 | MONTHLY |
| PREMIUM | PREMIUM | $29.99 | MONTHLY |

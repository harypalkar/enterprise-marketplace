# Notification Service Architecture

## ER Diagram

```mermaid
erDiagram
    NOTIFICATION_TEMPLATE ||--o{ NOTIFICATION : renders
    NOTIFICATION ||--o{ NOTIFICATION_HISTORY : tracks
    NOTIFICATION ||--o{ NOTIFICATION_RETRY : retries
    NOTIFICATION ||--o{ NOTIFICATION_DELIVERY : attempts
    NOTIFICATION ||--o| NOTIFICATION_INBOX : in_app
    NOTIFICATION ||--o{ NOTIFICATION_AUDIT : audited_by
    NOTIFICATION ||--o{ OUTBOX_EVENT : publishes
    NOTIFICATION_CHANNEL ||--o{ NOTIFICATION : configures

    NOTIFICATION {
        uuid id PK
        string request_id UK
        string recipient_id
        string channel
        string status
        text body
        timestamptz expires_at
    }

    NOTIFICATION_TEMPLATE {
        uuid id PK
        string template_code
        string channel
        string content_type
        text body_template
    }

    NOTIFICATION_HISTORY {
        uuid id PK
        uuid notification_id FK
        string status
        string event_type
    }

    NOTIFICATION_CHANNEL {
        uuid id PK
        string channel UK
        string provider
        int rate_limit_per_hour
    }

    NOTIFICATION_RETRY {
        uuid id PK
        uuid notification_id FK
        int attempt_number
        string status
    }
```

## Sequence Diagram — Kafka to Delivery

```mermaid
sequenceDiagram
    participant PS as Product Service
    participant Kafka
    participant NS as Notification Service
    participant DB as PostgreSQL
    participant Provider as Channel Provider
    participant Redis
    participant Outbox

    PS->>Kafka: product-created
    Kafka->>NS: consume event
    NS->>Redis: check rate limit + preferences
    NS->>DB: INSERT notification (CREATED)
    NS->>DB: INSERT history + audit + outbox
    NS->>Provider: dispatch (EMAIL/SMS/PUSH/WEBHOOK/IN_APP)
    Provider->>DB: INSERT delivery + inbox
    NS->>DB: UPDATE status SENT/DELIVERED/RETRYING
    Outbox->>Kafka: notification-sent / notification-failed / notification-retry
```

## Kafka Event Flow

```mermaid
flowchart LR
    PS[Product Service] --> PC[product-created]
    PS --> PU[product-updated]
    WS[Workflow Service] --> WC[workflow-completed]
    WS --> WF[workflow-failed]
    SS[Seller Service] --> SA[seller-approved]
    BS[Buyer Service] --> BR[buyer-registered]
    IS[Inventory Service] --> IL[inventory-low]
    SUB[Subscription Service] --> SE[subscription-expired]
    PC & PU & WC & WF & SA & BR & IL & SE --> NS[Notification Service]
    NS --> NSent[notification-sent]
    NS --> NFail[notification-failed]
    NS --> NRetry[notification-retry]
    NS --> AC[audit-created]
    NS --> DLQ[notification-dead-letter]
```

## Channel Providers

| Channel | Provider | Delivery Mechanism |
|---------|----------|-------------------|
| EMAIL | SMTP / SES | JavaMailSender or AWS SES SDK |
| SMS | Twilio / HTTP | Twilio SDK or REST gateway |
| PUSH | FCM / HTTP | Firebase Admin SDK or REST gateway |
| WEBHOOK | REST | HTTP POST callback |
| IN_APP | Database | `notification_inbox` table |

## Package Structure

```
controller/     REST APIs
service/        Domain services + schedulers
service.impl/   NotificationServiceImpl
provider/       Channel provider implementations
template/       Template rendering engine
channel/        Channel dispatcher
kafka/          Consumers and publishers
outbox/         Transactional outbox scheduler
audit/          Audit + history
redis/          Cache port (templates, channels, rate limits, preferences)
security/       JWT Keycloak (ADMIN, SELLER, BUYER)
```

## Status Lifecycle

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> QUEUED
    CREATED --> PROCESSING
    QUEUED --> PROCESSING
    PROCESSING --> SENT
    PROCESSING --> DELIVERED
    PROCESSING --> FAILED
    PROCESSING --> RETRYING
    RETRYING --> PROCESSING
    FAILED --> RETRYING
    SENT --> DELIVERED
    CREATED --> EXPIRED
    QUEUED --> EXPIRED
    RETRYING --> EXPIRED
    CREATED --> CANCELLED
    FAILED --> CANCELLED
```

# Notification Service Architecture

## ER Diagram

```mermaid
erDiagram
    NOTIFICATION_TEMPLATE ||--o{ NOTIFICATION : renders
    NOTIFICATION ||--o{ NOTIFICATION_DELIVERY : attempts
    NOTIFICATION ||--o| NOTIFICATION_INBOX : in_app
    NOTIFICATION ||--o{ NOTIFICATION_AUDIT : audited_by
    NOTIFICATION ||--o{ OUTBOX_EVENT : publishes

    NOTIFICATION {
        uuid id PK
        string request_id UK
        string recipient_id
        string channel
        string status
        text body
    }

    NOTIFICATION_TEMPLATE {
        uuid id PK
        string template_code
        string channel
        text body_template
    }

    NOTIFICATION_DELIVERY {
        uuid id PK
        uuid notification_id FK
        int attempt_number
        string status
    }

    NOTIFICATION_INBOX {
        uuid id PK
        uuid notification_id FK
        string recipient_id
        boolean read_flag
    }
```

## Sequence Diagram — Kafka to Delivery

```mermaid
sequenceDiagram
    participant WF as Workflow Service
    participant Kafka
    participant NS as Notification Service
    participant DB as PostgreSQL
    participant Channel as Channel Handler
    participant Outbox

    WF->>Kafka: notification-created
    Kafka->>NS: consume event
    NS->>DB: INSERT notification (PENDING)
    NS->>DB: INSERT audit + outbox
    NS->>Channel: dispatch (IN_APP/EMAIL/SMS)
    Channel->>DB: INSERT delivery + inbox
    NS->>DB: UPDATE status SENT
    Outbox->>Kafka: notification-sent
```

## Kafka Event Flow

```mermaid
flowchart LR
    WS[Workflow Service] --> NC[notification-created]
    WS --> WC[workflow-completed]
    WS --> WF[workflow-failed]
    NC & WC & WF --> NS[Notification Service]
    NS --> NSent[notification-sent]
    NS --> NFail[notification-failed]
    NS --> AC[audit-created]
```

## Channel Handlers

| Channel | Handler | Delivery Mechanism |
|---------|---------|-------------------|
| IN_APP | InAppChannelHandler | Persists to `notification_inbox` |
| EMAIL | EmailChannelHandler | JavaMailSender (SMTP) |
| SMS | SmsChannelHandler | HTTP gateway (`SMS_GATEWAY_URL`) |
| PUSH | PushChannelHandler | HTTP gateway (`PUSH_GATEWAY_URL`) |
| WEBHOOK | WebhookChannelHandler | POST to `recipientAddress` |

## Integration

| Service | Direction | Topic |
|---------|-----------|-------|
| Workflow Service | → Notification | `notification-created`, `workflow-completed`, `workflow-failed` |
| Audit Service | Notification → | `audit-created` |

All integration is event-driven via Kafka. No cross-service database access.

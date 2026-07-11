# Admin Service Architecture

## ER Diagram

```mermaid
erDiagram
    PLATFORM_SETTING ||--o{ ADMIN_AUDIT : tracked_by
    FEATURE_FLAG ||--o{ ADMIN_AUDIT : tracked_by
    ADMIN_CONFIG ||--o{ ADMIN_AUDIT : tracked_by
    PLATFORM_SETTING ||--o{ OUTBOX_EVENT : publishes
    FEATURE_FLAG ||--o{ OUTBOX_EVENT : publishes
    ADMIN_CONFIG ||--o{ OUTBOX_EVENT : publishes

    PLATFORM_SETTING {
        uuid id PK
        string setting_key UK
        string setting_value
        string category
        boolean active
    }

    FEATURE_FLAG {
        uuid id PK
        string flag_key UK
        boolean enabled
        int rollout_percentage
    }

    ADMIN_CONFIG {
        uuid id PK
        string config_key UK
        jsonb config_value
        string scope
        boolean active
    }

    ADMIN_AUDIT {
        uuid id PK
        string action
        string entity_type
        string entity_key
        string actor
        jsonb before_state
        jsonb after_state
    }

    PLATFORM_STAT {
        uuid id PK
        string metric_key UK
        bigint metric_value
        string category
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

## Sequence Diagram — Setting Update

```mermaid
sequenceDiagram
    participant Admin as Admin User
    participant API as Admin Service
    participant DB as PostgreSQL
    participant Redis
    participant Outbox
    participant Kafka

    Admin->>API: PUT /api/v1/admin/settings/{key}
    API->>DB: UPDATE platform_setting
    API->>DB: INSERT admin_audit
    API->>Outbox: admin-config-changed + audit-created
    API->>Redis: Evict + refresh cache
    API-->>Admin: 200 OK
    Outbox->>Kafka: publish events
```

## Integration

| Event | Topic | Consumers |
|-------|-------|-----------|
| Config changed | `admin-config-changed` | Gateway, domain services |
| Feature toggled | `admin-feature-toggled` | AI Service, Product Service |
| Audit created | `audit-created` | Audit Service |

All mutations write to `admin_audit` locally and enqueue `audit-created` for the central audit trail.

## Dashboard

The `/api/v1/admin/dashboard` endpoint aggregates:

- **Domain summaries** — total/active counts for settings, feature flags, and configs
- **Admin audit total** — count of local admin audit records
- **Platform metrics** — seeded `platform_stat` rows (subscriptions, reports, users, products, sellers, buyers)

Metrics are stored in the admin database and can be updated by future sync jobs from domain services.

## Security

| Operation | Roles |
|-----------|-------|
| All `/api/v1/admin/**` | ADMIN only |
| Bootstrap `/api/v1/bootstrap/**` | Public |
| Actuator `/actuator/**` | Public |

JWT validation uses Keycloak realm roles with `ROLE_` prefix mapping.

## Caching

Redis caches:

- Individual settings by key
- Full settings list
- Individual feature flags by key
- Full feature flags list

Cache TTL defaults to 3600 seconds (`ADMIN_CACHE_TTL_SECONDS`). Mutations evict affected keys.

## Outbox Pattern

Transactional outbox ensures Kafka events are published reliably:

1. Business transaction commits setting/flag/config + audit + outbox row
2. Scheduled publisher polls `PENDING` outbox events every 5 seconds
3. Failed publishes retry up to 5 times, then move to `admin-dead-letter`

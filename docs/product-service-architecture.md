# Product Service — Architecture

## ER Diagram

```mermaid
erDiagram
    PRODUCT ||--o{ PRODUCT_PRICE : has
    PRODUCT ||--|| PRODUCT_INVENTORY : has
    PRODUCT ||--o{ PRODUCT_ATTRIBUTE : has
    PRODUCT ||--o{ PRODUCT_IMAGE : has
    PRODUCT ||--o{ PRODUCT_DOCUMENT : has
    PRODUCT ||--|| PRODUCT_WORKFLOW : tracks
    PRODUCT ||--o{ PRODUCT_AUDIT : logs
    PRODUCT ||--o{ OUTBOX_EVENT : emits

    PRODUCT {
        uuid id PK
        string sku UK
        string name
        uuid seller_id
        uuid category_id
        decimal unit_price
        string status
    }
    PRODUCT_PRICE {
        uuid id PK
        uuid product_id FK
        decimal unit_price
        string currency
        timestamptz valid_from
    }
    PRODUCT_INVENTORY {
        uuid id PK
        uuid product_id FK
        int quantity_available
        int quantity_reserved
    }
    PRODUCT_WORKFLOW {
        uuid id PK
        uuid product_id FK
        string status
    }
    PRODUCT_AUDIT {
        uuid id PK
        uuid product_id FK
        string operation
        jsonb before_state
        jsonb after_state
    }
    OUTBOX_EVENT {
        uuid id PK
        uuid aggregate_id
        string topic
        string status
        jsonb payload
    }
```

## Create Product — Sequence Diagram

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Controller
    participant ValidationPipeline
    participant Redis
    participant Service
    participant DB as PostgreSQL
    participant OutboxWorker
    participant Kafka

    Client->>Gateway: POST /api/products/api/v1/products
    Gateway->>Controller: POST /api/v1/products
    Controller->>ValidationPipeline: validate(envelope)
    ValidationPipeline->>ValidationPipeline: JWT (Security filter)
    ValidationPipeline->>ValidationPipeline: JSON Schema
    ValidationPipeline->>ValidationPipeline: Bean Validation
    ValidationPipeline->>Redis: validate seller/category/currency
    ValidationPipeline->>ValidationPipeline: business + SKU checks
    ValidationPipeline-->>Controller: OK
    Controller->>Service: createProduct()
    Service->>DB: BEGIN TRANSACTION
    Service->>DB: INSERT product, price, inventory, workflow, audit
    Service->>DB: INSERT outbox_event (PENDING)
    Service->>DB: COMMIT
    Service-->>Controller: ProductDetailResponse
    Controller-->>Client: 201 Created

    loop every 5s
        OutboxWorker->>DB: SELECT pending outbox events
        OutboxWorker->>Kafka: publish product-created
        OutboxWorker->>DB: UPDATE status PUBLISHED
    end
```

## Validation Pipeline

| Step | Component | Description |
|------|-----------|-------------|
| 1 | Spring Security | JWT validation (Keycloak) |
| 2 | JsonSchemaValidationStep | Classpath JSON Schema |
| 3 | BeanValidationStep | Jakarta `@Valid` |
| 4 | ReferenceValidationStep | Redis cache-aside (seller, category, currency, HSN, GST) |
| 5 | BusinessValidationStep | Price, inventory, status rules |
| 6 | DuplicateSkuValidationStep | SKU uniqueness |
| 7 | IdempotencyAspect | `Idempotency-Key` header |

## Workflow States

```
INITIAL → VALIDATING → BUSINESS_VALIDATED → PERSISTED → OUTBOX_CREATED
  → PUBLISHED → INDEXED → COMPLETED
```

Failure path: `FAILED` | Amendment: `AMENDED` | Cancel: `CANCELLED`

## Package Structure

```
productservice/
├── controller/       REST API
├── service/          Service interfaces
├── service/impl/     Business orchestration
├── validation/       Validation pipeline
├── repository/       Spring Data JPA
├── entity/           JPA entities
├── dto/canonical/    Request/response envelope
├── mapper/           MapStruct
├── kafka/            Producer & topic config
├── outbox/           Background publisher
├── config/           OpenAPI, scheduling
├── security/         JWT (via infrastructure.config)
├── enums/            Domain enums
└── constants/        Kafka topics, Redis keys
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `NEON_DB_URL` | PostgreSQL JDBC URL |
| `REDIS_HOST` | Redis host |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers |
| `KEYCLOAK_ISSUER_URI` | JWT issuer |
| `MARKETPLACE_SECURITY_ENABLED` | Enable/disable JWT |
| `OUTBOX_PUBLISH_INTERVAL_MS` | Outbox poll interval |

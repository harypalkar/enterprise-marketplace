# Search Service Architecture

## End-to-End Sequence

```mermaid
sequenceDiagram
    participant Seller
    participant Product as Product Service
    participant PG as PostgreSQL
    participant Outbox
    participant Kafka
    participant Search as Search Service
    participant ES as Elasticsearch
    participant Buyer

    Seller->>Product: POST /api/v1/products
    Product->>PG: INSERT product
    Product->>Outbox: search-index payload
    Outbox->>Kafka: search-index
    Kafka->>Search: consume event
    Search->>ES: index document
    Search->>Kafka: search-indexed
    Buyer->>Search: GET /api/v1/search/products?q=...
    Search->>ES: query
    Search-->>Buyer: search results
```

## ER Diagram (Metadata DB)

```mermaid
erDiagram
    SEARCH_SYNC_LOG ||--o{ SEARCH_AUDIT : tracks
    OUTBOX_EVENT ||--o{ SEARCH_SYNC_LOG : publishes

    SEARCH_SYNC_LOG {
        uuid id PK
        uuid product_id
        string operation
        string status
    }

    SEARCH_AUDIT {
        uuid id PK
        uuid product_id
        string operation
        string query_text
    }

    OUTBOX_EVENT {
        uuid id PK
        string topic
        jsonb payload
    }
```

## Elasticsearch Document

Index: `marketplace-products`

| Field | Type | Purpose |
|-------|------|---------|
| productId | keyword | Document ID |
| name | text | Full-text search |
| description | text | Full-text search |
| sku | keyword | Exact match |
| sellerId | keyword | Filter |
| categoryId | keyword | Filter |
| status | keyword | Filter |
| unitPrice | double | Range filter |

## Kafka Flow

```mermaid
flowchart LR
    PS[Product Service] --> SI[search-index]
    PS --> PC[product-created]
    PS --> PU[product-updated]
    PS --> PD[product-deleted]
    SI & PC & PU & PD --> SS[Search Service]
    SS --> ES[(Elasticsearch)]
    SS --> SIX[search-indexed]
    SS --> SF[search-failed]
```

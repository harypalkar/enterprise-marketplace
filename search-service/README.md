# Search Service

Production-ready Elasticsearch product search microservice for the Enterprise Marketplace Platform.

## End-to-End Flow

```
Product Service (CRUD)
    → Outbox: search-index event
    → Kafka: search-index topic
    → Search Service consumer
    → Elasticsearch: marketplace-products index
    → Buyer: GET /api/v1/search/products
```

## Overview

| Item | Value |
|------|-------|
| Port | 8090 |
| Base path | `/api/v1/search` |
| Gateway | `http://localhost:8080/api/search/api/v1/search/products` |
| Index | `marketplace-products` |
| Metadata DB | Neon PostgreSQL (sync log, audit, outbox) |
| Cache | Redis (search query results) |
| Messaging | Kafka (consumes product/search events, publishes search-indexed) |

## API Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/search/products` | Buyer/Seller/Admin | Full-text search with filters |
| GET | `/api/v1/search/products/{id}` | Buyer/Seller/Admin | Get indexed product |
| POST | `/api/v1/search/products/{id}/reindex` | Seller/Admin | Manual reindex |

### Search Parameters

- `q` — full-text query (name, description, sku)
- `sellerId`, `categoryId`, `status`
- `minPrice`, `maxPrice`
- `page`, `size`, `sort`

## Kafka Topics

**Consumed:** `search-index`, `product-created`, `product-updated`, `product-deleted`

**Published:** `search-indexed`, `search-failed`

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd search-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install -pl search-service,product-service -am
```

## Documentation

- [Search Architecture](../docs/search-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-Search.postman_collection.json)

# Product Service

Production-ready reference microservice for the Enterprise Marketplace Platform.

## Overview

| Item | Value |
|------|-------|
| Port | 8082 |
| Base path | `/api/v1/products` |
| Gateway | `http://localhost:8080/api/products/api/v1/products` |
| Database | Neon PostgreSQL (Flyway) |
| Cache | Redis (reference data validation) |
| Messaging | Kafka (transactional outbox) |

## Architecture

- **Hexagonal / clean architecture** with explicit packages: `controller`, `service`, `validation`, `repository`, `entity`, `dto`, `mapper`, `kafka`, `outbox`, `workflow`, `audit`
- **Canonical request envelope** for create/update (header, requestInfo, seller, product, pricing, inventory, attributes, media, workflow, metadata)
- **Validation pipeline**: JWT → JSON Schema → Bean Validation → Redis reference data → business rules → duplicate SKU → idempotency
- **Transactional outbox** for reliable Kafka publishing

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/products` | Create product (canonical envelope, `Idempotency-Key`) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products` | List products (paginated) |
| GET | `/api/v1/products/search` | Search products |
| PUT | `/api/v1/products/{id}` | Full update |
| PATCH | `/api/v1/products/{id}` | Partial update |
| DELETE | `/api/v1/products/{id}` | Archive (soft delete) |

## Database Tables

- `product`, `product_price`, `product_inventory`, `product_attribute`, `product_image`, `product_document`
- `product_workflow`, `product_audit`, `outbox_event`

Flyway: `V1__create_product_table.sql`, `V2__create_product_domain_tables.sql`

## Kafka Topics

- `product-created`, `product-updated`, `product-deleted`
- `workflow-updated`, `notification-created`, `search-index`, `audit-created`
- `product-dead-letter` (DLQ)

## Run Locally

### With Docker infrastructure

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd docker
docker compose up -d
cd ../product-service
mvn spring-boot:run
```

### Standalone (H2, no Kafka/Redis)

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Build & Test

```powershell
mvn clean install
```

## Docker

```bash
docker build -f docker/Dockerfile.service --build-arg SERVICE_MODULE=product-service -t marketplace/product-service .
docker run -p 8082:8082 -e SPRING_PROFILES_ACTIVE=prod marketplace/product-service
```

## Documentation

- [Architecture & diagrams](../docs/product-service-architecture.md)
- [Postman collection](../docs/postman/Enterprise-Marketplace-Product.postman_collection.json)

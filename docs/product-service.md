# Product Service

Production reference microservice for the Enterprise Marketplace Platform.

## Overview

The Product Service (`product-service`, port **8082**) is the **reference implementation** for all domain microservices. It implements hexagonal/clean architecture with:

- Enterprise **canonical request envelope**
- 7-step **validation pipeline** (JWT → JSON Schema → Bean → Redis → Business → SKU → Idempotency)
- **Normalized database schema** (9 tables)
- **Transactional outbox** + Kafka event publishing
- **Workflow tracking** and **audit trail**

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/products` | Create (canonical envelope + Idempotency-Key) |
| GET | `/api/v1/products/{id}` | Get by ID |
| GET | `/api/v1/products` | List (paginated) |
| GET | `/api/v1/products/search` | Search |
| PUT | `/api/v1/products/{id}` | Full update |
| PATCH | `/api/v1/products/{id}` | Partial update |
| DELETE | `/api/v1/products/{id}` | Archive |

## Gateway Route

```
http://localhost:8080/api/products/api/v1/products
```

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd product-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

## Documentation

- [Architecture, ER & Sequence Diagrams](product-service-architecture.md)
- [Postman Collection](postman/Enterprise-Marketplace-Product.postman_collection.json)
- [Service README](../product-service/README.md)

## Build

```powershell
mvn clean install
```

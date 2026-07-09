# Enterprise Marketplace â€” API Test Report

**Generated:** 2026-07-10 00:19:46 +05:30  
**Environment:** Local (standalone H2, security disabled)  
**JDK:** 21  

## Summary

| Metric | Value |
|--------|-------|
| Total API calls | 58 |
| Passed | 57 |
| Failed | 1 |
| Pass rate | 98.3% |
| Avg response time | 137ms |
| P95 response time | 285ms |

## Service Availability

| Service | Port | Health |
|---------|------|--------|
| Gateway | 8080 | UP |
| Identity | 8081 | DOWN (not running) |
| Product | 8082 | UP |
| Seller | 8083 | UP |
| Buyer | 8084 | UP |
| Category | 8085 | UP |
| Inventory | 8086 | UP |
| Pricing | 8087 | UP |
| Search | 8090 | UP |
| AI | 8091 | UP (Ollama may be unavailable) |

## Results by Service

### ai (3 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 503 | 26ms | PASS | `/actuator/health` |
| GET | 200 | 25ms | PASS | `/api/v1/bootstrap/health` |
| GET | 200 | 32ms | PASS | `/api/v1/infrastructure/health/ollama` |

### buyer (6 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 107ms | PASS | `/actuator/health` |
| GET | 200 | 26ms | PASS | `/api/v1/bootstrap/health` |
| POST | 201 | 112ms | PASS | `/api/v1/buyers` |
| GET | 200 | 35ms | PASS | `/api/v1/buyers/009f2077-8d56-4fff-bccb-65b1b5423a2c` |
| PATCH | 200 | 37ms | PASS | `/api/v1/buyers/009f2077-8d56-4fff-bccb-65b1b5423a2c/status` |
| GET | 200 | 72ms | PASS | `/api/v1/buyers?keyword=API` |

### category (7 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 70ms | PASS | `/actuator/health` |
| GET | 200 | 22ms | PASS | `/api/v1/bootstrap/health` |
| POST | 201 | 110ms | PASS | `/api/v1/categories` |
| PUT | 200 | 376ms | PASS | `/api/v1/categories/0a7ec058-9f2d-432d-aa08-b97948c8d998` |
| GET | 200 | 41ms | PASS | `/api/v1/categories/0a7ec058-9f2d-432d-aa08-b97948c8d998` |
| GET | 200 | 36ms | PASS | `/api/v1/categories/slug/api-test-category-325511` |
| GET | 200 | 99ms | PASS | `/api/v1/categories?keyword=API` |

### gateway (2 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 118ms | PASS | `/actuator/health` |
| GET | 200 | 33ms | PASS | `/api/v1/bootstrap/health` |

### gateway-buyer ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 55ms | PASS | `/api/buyers/api/v1/buyers` |

### gateway-category ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 52ms | PASS | `/api/categories/api/v1/categories` |

### gateway-inventory ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 71ms | PASS | `/api/inventory/api/v1/inventory` |

### gateway-pricing ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 70ms | PASS | `/api/pricing/api/v1/pricing` |

### gateway-product ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 52ms | PASS | `/api/products/api/v1/products` |

### gateway-search ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 49ms | PASS | `/api/search/api/v1/bootstrap/health` |

### gateway-seller ( passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 80ms | PASS | `/api/sellers/api/v1/sellers` |

### identity (0 passed,  failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 0 | 4116ms | FAIL | `/actuator/health` |

### inventory (8 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 34ms | PASS | `/actuator/health` |
| GET | 200 | 25ms | PASS | `/api/v1/bootstrap/health` |
| POST | 201 | 193ms | PASS | `/api/v1/inventory` |
| PUT | 200 | 35ms | PASS | `/api/v1/inventory/fe987ceb-7687-4ded-b6eb-8d56b28e6e32` |
| GET | 200 | 64ms | PASS | `/api/v1/inventory/fe987ceb-7687-4ded-b6eb-8d56b28e6e32` |
| PATCH | 200 | 38ms | PASS | `/api/v1/inventory/fe987ceb-7687-4ded-b6eb-8d56b28e6e32/release` |
| PATCH | 200 | 68ms | PASS | `/api/v1/inventory/fe987ceb-7687-4ded-b6eb-8d56b28e6e32/reserve` |
| GET | 200 | 60ms | PASS | `/api/v1/inventory?productId=6ea168e9-e00c-4acd-af64-ee8c7727482f` |

### pricing (7 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 46ms | PASS | `/actuator/health` |
| GET | 200 | 30ms | PASS | `/api/v1/bootstrap/health` |
| POST | 201 | 285ms | PASS | `/api/v1/pricing` |
| PUT | 200 | 53ms | PASS | `/api/v1/pricing/6e41b5bb-e158-4557-a61b-98ba2452aaa4` |
| GET | 200 | 46ms | PASS | `/api/v1/pricing/6e41b5bb-e158-4557-a61b-98ba2452aaa4` |
| PATCH | 200 | 43ms | PASS | `/api/v1/pricing/6e41b5bb-e158-4557-a61b-98ba2452aaa4/status` |
| GET | 200 | 47ms | PASS | `/api/v1/pricing?productId=6ea168e9-e00c-4acd-af64-ee8c7727482f` |

### product (8 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 36ms | PASS | `/actuator/health` |
| GET | 200 | 23ms | PASS | `/api/v1/bootstrap/health` |
| POST | 201 | 141ms | PASS | `/api/v1/products` |
| PUT | 200 | 32ms | PASS | `/api/v1/products/6ea168e9-e00c-4acd-af64-ee8c7727482f` |
| GET | 200 | 41ms | PASS | `/api/v1/products/6ea168e9-e00c-4acd-af64-ee8c7727482f` |
| PATCH | 200 | 28ms | PASS | `/api/v1/products/6ea168e9-e00c-4acd-af64-ee8c7727482f/status` |
| GET | 200 | 30ms | PASS | `/api/v1/products/sku/SKU-API-70287` |
| GET | 200 | 36ms | PASS | `/api/v1/products?keyword=API` |

### search (3 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 46ms | PASS | `/actuator/health` |
| GET | 200 | 28ms | PASS | `/api/v1/bootstrap/health` |
| GET | 404 | 106ms | PASS | `/api/v1/infrastructure/health/elasticsearch` |

### seller (6 passed, 0 failed)

| Method | Status | Time | Result | Endpoint |
|--------|--------|------|--------|----------|
| GET | 200 | 36ms | PASS | `/actuator/health` |
| GET | 200 | 44ms | PASS | `/api/v1/bootstrap/health` |
| POST | 201 | 165ms | PASS | `/api/v1/sellers` |
| GET | 200 | 38ms | PASS | `/api/v1/sellers/3070054b-0a97-42ed-8dcb-41d12206a7b4` |
| PATCH | 200 | 42ms | PASS | `/api/v1/sellers/3070054b-0a97-42ed-8dcb-41d12206a7b4/status` |
| GET | 200 | 66ms | PASS | `/api/v1/sellers?keyword=API` |

## Gateway URL Pattern

`
http://localhost:8080/api/{service}/api/v1/{resource}
`

Example: http://localhost:8080/api/sellers/api/v1/sellers

## Notes

- Mutating requests require Idempotency-Key header.
- Identity service requires Docker (PostgreSQL, Redis, Kafka).
- AI Ollama health returns 503 when Docker Ollama is not running (expected locally).
- Scaffold services (workflow, notification, audit, subscription, report, admin) not tested â€” not implemented yet.


# Domain Services

Core B2B domain microservices following hexagonal architecture (same pattern as [Product Service](product-service.md)).

## Services

| Service | Port | Base Path | Gateway Prefix |
|---------|------|-----------|----------------|
| Product | 8082 | `/api/v1/products` | `/api/products/**` |
| Seller | 8083 | `/api/v1/sellers` | `/api/sellers/**` |
| Buyer | 8084 | `/api/v1/buyers` | `/api/buyers/**` |
| Category | 8085 | `/api/v1/categories` | `/api/categories/**` |
| Inventory | 8086 | `/api/v1/inventory` | `/api/inventory/**` |
| Pricing | 8087 | `/api/v1/pricing` | `/api/pricing/**` |

## Common Patterns

- **Architecture:** domain → application → infrastructure → bootstrap
- **Database:** Flyway migrations, Neon PostgreSQL per service, HikariCP
- **Security:** JWT via Keycloak; disable with `marketplace.security.enabled=false`
- **Idempotency:** `Idempotency-Key` header on mutating POST/PUT/PATCH endpoints
- **Auditing:** JPA `created_at` / `updated_at` via `common-library` auto-configuration
- **Tests:** unit tests + Testcontainers integration tests

## Local Standalone Run (no Docker/PostgreSQL)

Use the `local,standalone` profile for in-memory H2 smoke tests:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd seller-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

Repeat for `buyer-service`, `category-service`, `inventory-service`, `pricing-service`.

## API Summary

### Seller Service (8083)

- CRUD + search by status/keyword
- GSTIN/PAN validation (Indian B2B)
- Status lifecycle: `PENDING → ACTIVE → SUSPENDED → ARCHIVED`

### Buyer Service (8084)

- CRUD + search by status, location, keyword
- Status lifecycle: `PENDING → ACTIVE → SUSPENDED → ARCHIVED`

### Category Service (8085)

- Hierarchical categories (parent/child)
- Lookup by slug, search by status/parent/keyword
- Status: `ACTIVE`, `INACTIVE`

### Inventory Service (8086)

- Stock per product/seller
- Reserve and release quantity endpoints
- Status: `IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`

### Pricing Service (8087)

- Product pricing per seller (unit price, currency, MOQ tiers)
- Status lifecycle: `DRAFT → ACTIVE → INACTIVE`

## Gateway Examples

```
GET  http://localhost:8080/api/sellers/v1/sellers
POST http://localhost:8080/api/buyers/v1/buyers
GET  http://localhost:8080/api/categories/v1/categories
GET  http://localhost:8080/api/inventory/v1/inventory
GET  http://localhost:8080/api/pricing/v1/pricing
```

## Build

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
mvn clean install
```

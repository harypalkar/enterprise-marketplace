# Logging Standard

## Objectives

- Structured, searchable logs across all microservices
- End-to-end request traceability via correlation and request IDs
- Consistent format for ELK ingestion
- No sensitive data in logs

## Log Format

### Pattern (Logback)

```
%5p [${spring.application.name},%X{correlationId:-},%X{requestId:-}] %logger{36} - %msg
```

### Example Output

```
 INFO [product-service,a1b2c3d4-e5f6-7890-abcd-ef1234567890,f9e8d7c6-b5a4-3210-fedc-ba0987654321] c.e.m.p.b.c.BootstrapController - Service bootstrap health check
```

## MDC Keys

| Key | Source | Description |
|-----|--------|-------------|
| `correlationId` | `X-Correlation-Id` header | End-to-end trace across services |
| `requestId` | `X-Request-Id` header | Single request identifier |
| `tenantId` | `X-Tenant-Id` header | Multi-tenant context |
| `userId` | `X-User-Id` header | Authenticated user |
| `serviceName` | Application startup | Originating service name |
| `idempotencyKey` | `Idempotency-Key` header | Idempotent operation tracking |

## Log Levels

| Level | Usage |
|-------|-------|
| `ERROR` | Unrecoverable failures, exceptions requiring attention |
| `WARN` | Recoverable issues, business rule violations, deprecated usage |
| `INFO` | Significant business events, service lifecycle, request summaries |
| `DEBUG` | Detailed flow information (local/dev only) |
| `TRACE` | Very verbose diagnostics (never in production) |

## Profile-Based Levels

| Profile | Root Level | Application Level |
|---------|------------|-------------------|
| `local` | INFO | DEBUG |
| `dev` | INFO | DEBUG |
| `qa` | INFO | INFO |
| `prod` | WARN | INFO |

## Structured Event Logging

Use `LoggingUtility.logEvent()` for significant business events:

```java
LoggingUtility.logEvent("PRODUCT_CREATED", Map.of(
    "productId", productId.toString(),
    "sellerId", sellerId.toString()
));
```

## Rules

1. **Always** include correlation context — use `LoggingUtility` helpers or ensure MDC is populated
2. **Never** log passwords, tokens, API keys, or full credit card numbers
3. **Mask** PII in logs (email, phone) when logging is necessary: `j***@example.com`
4. **Avoid** logging full request/response bodies in production
5. **Use** parameterized logging: `log.info("Processing order {}", orderId)` — not string concatenation
6. **Log** at service boundaries: incoming requests, outgoing calls, event publish/consume
7. **Include** duration for external calls: `log.info("Kafka publish completed in {}ms", duration)`

## Error Logging

```java
// Correct
LoggingUtility.error("Failed to process order " + orderId, exception);

// Incorrect — exposes internal details to log consumers without context
log.error(exception.getMessage());
```

## ELK Integration (Future Milestone)

- Filebeat ships JSON-formatted logs to Elasticsearch
- Index pattern: `marketplace-{service}-{env}-*`
- Kibana dashboards for error rate, latency, and correlation trace lookup

## OpenTelemetry Correlation

Trace IDs from OpenTelemetry are complementary to `X-Correlation-Id`. The gateway generates correlation IDs; OTel spans link via W3C `traceparent` header propagation.

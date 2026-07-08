package com.enterprise.marketplace.common.constant;

/**
 * HTTP header and MDC constants used across all microservices.
 */
public final class HttpHeaders {

    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String USER_ID = "X-User-Id";

    private HttpHeaders() {
        throw new UnsupportedOperationException("Utility class");
    }
}

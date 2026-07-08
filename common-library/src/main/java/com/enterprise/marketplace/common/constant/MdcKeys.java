package com.enterprise.marketplace.common.constant;

/**
 * Mapped Diagnostic Context keys for structured logging.
 */
public final class MdcKeys {

    public static final String CORRELATION_ID = "correlationId";
    public static final String REQUEST_ID = "requestId";
    public static final String TENANT_ID = "tenantId";
    public static final String USER_ID = "userId";
    public static final String SERVICE_NAME = "serviceName";
    public static final String IDEMPOTENCY_KEY = "idempotencyKey";

    private MdcKeys() {
        throw new UnsupportedOperationException("Utility class");
    }
}

package com.enterprise.marketplace.gatewayservice.handler;

/**
 * Header constants for gateway handlers (avoids circular dependency with filters).
 */
final class GatewayHeaderConstants {

    static final String CORRELATION_ID = "X-Correlation-Id";
    static final String REQUEST_ID = "X-Request-Id";

    private GatewayHeaderConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}

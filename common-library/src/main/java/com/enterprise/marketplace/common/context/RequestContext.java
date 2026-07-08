package com.enterprise.marketplace.common.context;

import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.constant.MdcKeys;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Thread-local request context holding correlation, request, tenant, and user identifiers.
 */
public final class RequestContext {

    private static final ThreadLocal<ContextHolder> CONTEXT = ThreadLocal.withInitial(ContextHolder::new);

    private RequestContext() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getCorrelationId() {
        return CONTEXT.get().correlationId;
    }

    public static void setCorrelationId(String correlationId) {
        CONTEXT.get().correlationId = correlationId;
        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
    }

    public static String getRequestId() {
        return CONTEXT.get().requestId;
    }

    public static void setRequestId(String requestId) {
        CONTEXT.get().requestId = requestId;
        MDC.put(MdcKeys.REQUEST_ID, requestId);
    }

    public static Optional<String> getTenantId() {
        return Optional.ofNullable(CONTEXT.get().tenantId);
    }

    public static void setTenantId(String tenantId) {
        CONTEXT.get().tenantId = tenantId;
        if (tenantId != null) {
            MDC.put(MdcKeys.TENANT_ID, tenantId);
        } else {
            MDC.remove(MdcKeys.TENANT_ID);
        }
    }

    public static Optional<String> getUserId() {
        return Optional.ofNullable(CONTEXT.get().userId);
    }

    public static void setUserId(String userId) {
        CONTEXT.get().userId = userId;
        if (userId != null) {
            MDC.put(MdcKeys.USER_ID, userId);
        } else {
            MDC.remove(MdcKeys.USER_ID);
        }
    }

    public static Optional<String> getIdempotencyKey() {
        return Optional.ofNullable(CONTEXT.get().idempotencyKey);
    }

    public static void setIdempotencyKey(String idempotencyKey) {
        CONTEXT.get().idempotencyKey = idempotencyKey;
        if (idempotencyKey != null) {
            MDC.put(MdcKeys.IDEMPOTENCY_KEY, idempotencyKey);
        } else {
            MDC.remove(MdcKeys.IDEMPOTENCY_KEY);
        }
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public static void clear() {
        CONTEXT.remove();
        MDC.remove(MdcKeys.CORRELATION_ID);
        MDC.remove(MdcKeys.REQUEST_ID);
        MDC.remove(MdcKeys.TENANT_ID);
        MDC.remove(MdcKeys.USER_ID);
        MDC.remove(MdcKeys.IDEMPOTENCY_KEY);
    }

    private static final class ContextHolder {
        private String correlationId;
        private String requestId;
        private String tenantId;
        private String userId;
        private String idempotencyKey;
    }
}

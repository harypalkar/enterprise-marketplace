package com.enterprise.marketplace.auditservice.constants;

import java.util.UUID;

public final class AuditCacheKeys {

    public static final String AUDIT_PREFIX = "audit:";
    public static final String CORRELATION_TIMELINE_PREFIX = "audit:timeline:";

    private AuditCacheKeys() {}

    public static String auditKey(UUID auditId) {
        return AUDIT_PREFIX + auditId;
    }

    public static String correlationTimelineKey(String correlationId) {
        return CORRELATION_TIMELINE_PREFIX + correlationId;
    }
}

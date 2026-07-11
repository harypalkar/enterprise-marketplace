package com.enterprise.marketplace.auditservice.util;

import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import org.springframework.util.StringUtils;

public final class AuditEventKeyGenerator {

    private AuditEventKeyGenerator() {}

    public static String generate(String sourceService, String requestId, AuditOperation operation) {
        if (!StringUtils.hasText(sourceService) || !StringUtils.hasText(requestId) || operation == null) {
            throw new IllegalArgumentException("sourceService, requestId, and operation are required for event key");
        }
        String key = sourceService.trim() + ":" + requestId.trim() + ":" + operation.name();
        if (key.length() > 128) {
            return key.substring(0, 128);
        }
        return key;
    }
}

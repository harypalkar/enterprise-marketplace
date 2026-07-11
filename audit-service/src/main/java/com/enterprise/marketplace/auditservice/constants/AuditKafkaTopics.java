package com.enterprise.marketplace.auditservice.constants;

public final class AuditKafkaTopics {

    public static final String AUDIT_CREATED = "audit-created";
    public static final String AUDIT_INDEXED = "audit-indexed";
    public static final String AUDIT_ARCHIVED = "audit-archived";
    public static final String DEAD_LETTER = "audit-dead-letter";

    private AuditKafkaTopics() {}
}

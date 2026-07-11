package com.enterprise.marketplace.adminservice.constants;

public final class AdminKafkaTopics {

    public static final String ADMIN_CONFIG_CHANGED = "admin-config-changed";
    public static final String ADMIN_FEATURE_TOGGLED = "admin-feature-toggled";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String DEAD_LETTER = "admin-dead-letter";

    private AdminKafkaTopics() {}
}

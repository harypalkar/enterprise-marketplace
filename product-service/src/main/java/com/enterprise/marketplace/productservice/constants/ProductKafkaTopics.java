package com.enterprise.marketplace.productservice.constants;

public final class ProductKafkaTopics {

    public static final String PRODUCT_CREATED = "product-created";
    public static final String PRODUCT_UPDATED = "product-updated";
    public static final String PRODUCT_DELETED = "product-deleted";
    public static final String WORKFLOW_UPDATED = "workflow-updated";
    public static final String NOTIFICATION_CREATED = "notification-created";
    public static final String SEARCH_INDEX = "search-index";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String DEAD_LETTER = "product-dead-letter";

    private ProductKafkaTopics() {}
}

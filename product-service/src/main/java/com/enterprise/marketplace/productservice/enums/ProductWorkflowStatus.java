package com.enterprise.marketplace.productservice.enums;

public enum ProductWorkflowStatus {
    INITIAL,
    VALIDATING,
    BUSINESS_VALIDATED,
    PERSISTED,
    OUTBOX_CREATED,
    PUBLISHED,
    INDEXED,
    COMPLETED,
    FAILED,
    AMENDED,
    CANCELLED
}

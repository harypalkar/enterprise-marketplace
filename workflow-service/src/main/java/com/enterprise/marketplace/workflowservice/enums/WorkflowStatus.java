package com.enterprise.marketplace.workflowservice.enums;

public enum WorkflowStatus {
    INITIAL,
    RECEIVED,
    TECHNICAL_VALIDATION,
    BUSINESS_VALIDATION,
    REDIS_VALIDATION,
    DATABASE_SAVED,
    OUTBOX_CREATED,
    EVENT_PUBLISHED,
    SEARCH_UPDATED,
    NOTIFICATION_SENT,
    AI_COMPLETED,
    COMPLETED,
    FAILED,
    RETRY,
    CANCELLED,
    AMENDED,
    ROLLBACK
}

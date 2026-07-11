package com.enterprise.marketplace.aiservice.enums;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD_LETTER
}

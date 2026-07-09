package com.enterprise.marketplace.productservice.enums;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
    DEAD_LETTER
}

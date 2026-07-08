package com.enterprise.marketplace.common.idempotency;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stored idempotency record for replay detection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecord {

    public enum Status {
        IN_PROGRESS,
        COMPLETED
    }

    private String key;
    private String requestHash;
    private Status status;
    private int httpStatus;
    private String responseBody;
    private Instant createdAt;
    private Instant expiresAt;
}

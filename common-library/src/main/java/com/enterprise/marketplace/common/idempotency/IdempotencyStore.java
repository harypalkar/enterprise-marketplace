package com.enterprise.marketplace.common.idempotency;

import java.util.Optional;

/**
 * Port for idempotency key storage. Implementations may use Redis or database.
 */
public interface IdempotencyStore {

    Optional<IdempotencyRecord> findByKey(String key);

    boolean tryAcquire(String key, String requestHash, long ttlSeconds);

    void complete(String key, int httpStatus, String responseBody);

    void release(String key);
}

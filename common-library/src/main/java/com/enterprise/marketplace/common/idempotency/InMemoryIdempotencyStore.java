package com.enterprise.marketplace.common.idempotency;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
/**
 * In-memory idempotency store for bootstrap and local development.
 * Replace with Redis-backed implementation in production services.
 */
public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Map<String, IdempotencyRecord> store = new ConcurrentHashMap<>();

    @Override
    public Optional<IdempotencyRecord> findByKey(String key) {
        cleanupExpired();
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public boolean tryAcquire(String key, String requestHash, long ttlSeconds) {
        cleanupExpired();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);

        IdempotencyRecord existing = store.get(key);
        if (existing != null) {
            if (!existing.getRequestHash().equals(requestHash)) {
                return false;
            }
            return false;
        }

        IdempotencyRecord record = IdempotencyRecord.builder()
                .key(key)
                .requestHash(requestHash)
                .status(IdempotencyRecord.Status.IN_PROGRESS)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        return store.putIfAbsent(key, record) == null;
    }

    @Override
    public void complete(String key, int httpStatus, String responseBody) {
        IdempotencyRecord record = store.get(key);
        if (record != null) {
            record.setStatus(IdempotencyRecord.Status.COMPLETED);
            record.setHttpStatus(httpStatus);
            record.setResponseBody(responseBody);
        }
    }

    @Override
    public void release(String key) {
        store.remove(key);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
    }
}

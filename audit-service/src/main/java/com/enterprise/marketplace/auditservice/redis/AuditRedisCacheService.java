package com.enterprise.marketplace.auditservice.redis;

import com.enterprise.marketplace.auditservice.constants.AuditCacheKeys;
import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.AuditTimelineResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditRedisCacheService implements AuditCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.redis.audit-cache-ttl-seconds:3600}")
    private long auditCacheTtlSeconds;

    @Override
    public void cacheAudit(AuditResponse response) {
        try {
            String key = AuditCacheKeys.auditKey(response.getId());
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(response), Duration.ofSeconds(auditCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache audit id={}", response.getId(), ex);
        }
    }

    @Override
    public void evictAudit(UUID auditId) {
        redisTemplate.delete(AuditCacheKeys.auditKey(auditId));
    }

    @Override
    public void cacheTimeline(AuditTimelineResponse timeline) {
        try {
            String key = AuditCacheKeys.correlationTimelineKey(timeline.getCorrelationId());
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(timeline), Duration.ofSeconds(auditCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache timeline correlationId={}", timeline.getCorrelationId(), ex);
        }
    }

    @Override
    public Optional<AuditResponse> getAudit(UUID auditId) {
        try {
            String cached = redisTemplate.opsForValue().get(AuditCacheKeys.auditKey(auditId));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, AuditResponse.class));
        } catch (Exception ex) {
            log.debug("Audit cache miss or parse error for id={}", auditId, ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuditTimelineResponse> getTimeline(String correlationId) {
        try {
            String cached = redisTemplate.opsForValue().get(AuditCacheKeys.correlationTimelineKey(correlationId));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, AuditTimelineResponse.class));
        } catch (Exception ex) {
            log.debug("Timeline cache miss or parse error for correlationId={}", correlationId, ex);
            return Optional.empty();
        }
    }
}

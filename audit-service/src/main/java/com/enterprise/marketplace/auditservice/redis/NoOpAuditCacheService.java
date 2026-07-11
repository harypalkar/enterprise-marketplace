package com.enterprise.marketplace.auditservice.redis;

import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.AuditTimelineResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "false")
public class NoOpAuditCacheService implements AuditCachePort {

    @Override
    public void cacheAudit(AuditResponse response) {
        // no-op when redis disabled
    }

    @Override
    public void evictAudit(UUID auditId) {
        // no-op when redis disabled
    }

    @Override
    public void cacheTimeline(AuditTimelineResponse timeline) {
        // no-op when redis disabled
    }

    @Override
    public Optional<AuditResponse> getAudit(UUID auditId) {
        return Optional.empty();
    }

    @Override
    public Optional<AuditTimelineResponse> getTimeline(String correlationId) {
        return Optional.empty();
    }
}

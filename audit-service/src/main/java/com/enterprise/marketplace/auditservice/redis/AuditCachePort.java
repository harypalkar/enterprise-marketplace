package com.enterprise.marketplace.auditservice.redis;

import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.AuditTimelineResponse;
import java.util.Optional;
import java.util.UUID;

public interface AuditCachePort {

    void cacheAudit(AuditResponse response);

    void evictAudit(UUID auditId);

    void cacheTimeline(AuditTimelineResponse timeline);

    Optional<AuditResponse> getAudit(UUID auditId);

    Optional<AuditTimelineResponse> getTimeline(String correlationId);
}

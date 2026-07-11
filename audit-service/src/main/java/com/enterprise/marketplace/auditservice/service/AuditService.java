package com.enterprise.marketplace.auditservice.service;

import com.enterprise.marketplace.auditservice.dto.AuditPageResponse;
import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.AuditSearchRequest;
import com.enterprise.marketplace.auditservice.dto.AuditTimelineResponse;
import com.enterprise.marketplace.auditservice.dto.CreateAuditRequest;
import java.util.UUID;

public interface AuditService {

    AuditResponse createAudit(CreateAuditRequest request);

    AuditResponse getAudit(UUID auditId);

    AuditResponse getByRequestId(String requestId);

    AuditTimelineResponse getByCorrelationId(String correlationId);

    AuditPageResponse getByAggregate(String aggregateType, UUID aggregateId, int page, int size);

    AuditPageResponse getByActor(String actor, int page, int size);

    AuditPageResponse getBySourceService(String sourceService, int page, int size);

    AuditPageResponse searchAudits(AuditSearchRequest request);

    void archiveAudit(UUID auditId);

    void processFromKafkaEvent(String payload, String eventSource);
}

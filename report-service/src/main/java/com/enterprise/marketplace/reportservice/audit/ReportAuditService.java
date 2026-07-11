package com.enterprise.marketplace.reportservice.audit;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.reportservice.entity.ReportJobEntity;
import com.enterprise.marketplace.reportservice.enums.ReportAuditOperation;
import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
import com.enterprise.marketplace.reportservice.entity.ReportAuditEntity;
import com.enterprise.marketplace.reportservice.repository.ReportAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportAuditService {

    private final ReportAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void recordAudit(
            ReportJobEntity job,
            ReportAuditOperation operation,
            ReportJobStatus beforeStatus,
            ReportJobStatus afterStatus,
            Object beforeState,
            Object afterState,
            String actor) {
        ReportAuditEntity audit = new ReportAuditEntity();
        audit.setJobId(job != null ? job.getId() : null);
        audit.setOperation(operation);
        audit.setActor(actor);
        audit.setCorrelationId(RequestContext.getCorrelationId());
        audit.setRequestId(job != null ? job.getRequestId() : RequestContext.getRequestId());
        audit.setBeforeStatus(beforeStatus);
        audit.setAfterStatus(afterStatus);
        audit.setBeforeState(serialize(beforeState));
        audit.setAfterState(serialize(afterState));
        auditRepository.save(audit);
        log.info(
                "Report audit recorded jobId={} operation={} before={} after={}",
                job != null ? job.getId() : null,
                operation,
                beforeStatus,
                afterStatus);
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            log.warn("Failed to serialize report audit state", ex);
            return null;
        }
    }
}

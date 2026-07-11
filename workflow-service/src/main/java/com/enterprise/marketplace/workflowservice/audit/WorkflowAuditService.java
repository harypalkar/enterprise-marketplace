package com.enterprise.marketplace.workflowservice.audit;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.workflowservice.entity.WorkflowAuditEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.enums.AuditOperation;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.repository.WorkflowAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowAuditService {

    private final WorkflowAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void recordAudit(
            WorkflowEntity workflow,
            AuditOperation operation,
            WorkflowStatus beforeStatus,
            WorkflowStatus afterStatus,
            Object beforeState,
            Object afterState,
            String actor) {
        WorkflowAuditEntity audit = new WorkflowAuditEntity();
        audit.setWorkflowId(workflow.getId());
        audit.setOperation(operation);
        audit.setActor(actor);
        audit.setCorrelationId(RequestContext.getCorrelationId());
        audit.setRequestId(workflow.getRequestId());
        audit.setBeforeStatus(beforeStatus);
        audit.setAfterStatus(afterStatus);
        audit.setBeforeState(serialize(beforeState));
        audit.setAfterState(serialize(afterState));
        auditRepository.save(audit);
        log.info(
                "Workflow audit recorded workflowId={} operation={} before={} after={}",
                workflow.getId(),
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
            log.warn("Failed to serialize audit state", ex);
            return null;
        }
    }
}

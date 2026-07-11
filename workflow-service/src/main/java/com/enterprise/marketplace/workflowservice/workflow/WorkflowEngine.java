package com.enterprise.marketplace.workflowservice.workflow;

import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowHistoryEntity;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.common.context.RequestContext;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WorkflowEngine {

    public void applyTransition(
            WorkflowEntity workflow, WorkflowStatus targetStatus, String reason, String actor) {
        WorkflowStatus currentStatus = workflow.getStatus();
        workflow.setPreviousStatus(currentStatus);
        workflow.setStatus(targetStatus);
        if (StringUtils.hasText(reason)) {
            workflow.setMessage(reason);
        }
    }

    public WorkflowHistoryEntity buildHistoryRecord(
            WorkflowEntity workflow, WorkflowStatus fromStatus, WorkflowStatus toStatus, String reason, String actor) {
        WorkflowHistoryEntity history = new WorkflowHistoryEntity();
        history.setWorkflowId(workflow.getId());
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setTransitionReason(reason);
        history.setTransitionedBy(actor != null ? actor : workflow.getInitiatedBy());
        history.setCorrelationId(RequestContext.getCorrelationId());
        history.setRequestId(workflow.getRequestId());
        return history;
    }

    public boolean isTerminalStatus(WorkflowStatus status) {
        return status == WorkflowStatus.COMPLETED
                || status == WorkflowStatus.CANCELLED
                || status == WorkflowStatus.ROLLBACK;
    }

    public String resolveActor(String explicitActor) {
        if (StringUtils.hasText(explicitActor)) {
            return explicitActor;
        }
        return RequestContext.getUserId().filter(StringUtils::hasText).orElse("system");
    }
}

package com.enterprise.marketplace.workflowservice.util;

import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import java.util.HashMap;
import java.util.Map;

public final class WorkflowPayloadBuilder {

    private WorkflowPayloadBuilder() {}

    public static Map<String, Object> buildEventPayload(WorkflowEntity workflow) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", workflow.getId().toString());
        payload.put("requestId", workflow.getRequestId());
        payload.put("correlationId", workflow.getCorrelationId());
        payload.put("aggregateType", workflow.getAggregateType().name());
        payload.put("aggregateId", workflow.getAggregateId().toString());
        payload.put("operationType", workflow.getOperationType().name());
        payload.put("status", workflow.getStatus().name());
        if (workflow.getPreviousStatus() != null) {
            payload.put("previousStatus", workflow.getPreviousStatus().name());
        }
        payload.put("sourceSystem", workflow.getSourceSystem());
        payload.put("initiatedBy", workflow.getInitiatedBy());
        payload.put("message", workflow.getMessage());
        return payload;
    }
}

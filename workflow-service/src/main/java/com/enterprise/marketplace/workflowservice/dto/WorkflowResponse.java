package com.enterprise.marketplace.workflowservice.dto;

import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {

    private UUID id;
    private String requestId;
    private String correlationId;
    private AggregateType aggregateType;
    private UUID aggregateId;
    private WorkflowOperationType operationType;
    private WorkflowStatus status;
    private WorkflowStatus previousStatus;
    private String tenantId;
    private String sourceSystem;
    private String initiatedBy;
    private String message;
    private Map<String, Object> metadata;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<WorkflowHistoryResponse> history;
}

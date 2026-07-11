package com.enterprise.marketplace.workflowservice.dto;

import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowHistoryResponse {

    private UUID id;
    private WorkflowStatus fromStatus;
    private WorkflowStatus toStatus;
    private String transitionReason;
    private String transitionedBy;
    private String correlationId;
    private String requestId;
    private Instant createdAt;
}

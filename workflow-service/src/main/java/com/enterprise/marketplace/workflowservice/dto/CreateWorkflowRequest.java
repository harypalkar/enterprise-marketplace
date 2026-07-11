package com.enterprise.marketplace.workflowservice.dto;

import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateWorkflowRequest {

    @NotBlank
    @Size(max = 64)
    private String requestId;

    @Size(max = 64)
    private String correlationId;

    @NotNull
    private AggregateType aggregateType;

    @NotNull
    private UUID aggregateId;

    @NotNull
    private WorkflowOperationType operationType;

    @Size(max = 64)
    private String tenantId;

    @Size(max = 64)
    private String sourceSystem;

    @Size(max = 128)
    private String initiatedBy;

    @Size(max = 2000)
    private String message;

    private Map<String, Object> metadata;

    private WorkflowStatus initialStatus;
}

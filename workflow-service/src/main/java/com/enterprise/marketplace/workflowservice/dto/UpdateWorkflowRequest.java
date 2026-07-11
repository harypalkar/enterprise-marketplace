package com.enterprise.marketplace.workflowservice.dto;

import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkflowRequest {

    @Size(max = 64)
    private String tenantId;

    @Size(max = 64)
    private String sourceSystem;

    @Size(max = 128)
    private String initiatedBy;

    @Size(max = 2000)
    private String message;

    private Map<String, Object> metadata;
}

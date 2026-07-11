package com.enterprise.marketplace.workflowservice.dto;

import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull
    private WorkflowStatus targetStatus;

    @Size(max = 2000)
    private String message;

    @Size(max = 2000)
    private String reason;
}

package com.enterprise.marketplace.reportservice.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CreateReportJobRequest {

    @NotBlank
    @Size(max = 64)
    private String requestId;

    @NotBlank
    @Size(max = 64)
    private String reportCode;

    @Size(max = 128)
    private String requestedBy;

    @Size(max = 64)
    private String correlationId;

    private Map<String, Object> parameters;
}

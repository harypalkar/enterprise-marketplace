package com.enterprise.marketplace.reportservice.dto;

import com.enterprise.marketplace.reportservice.enums.ReportType;
import java.time.Instant;
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
public class ReportDefinitionResponse {

    private UUID id;
    private String reportCode;
    private String name;
    private ReportType reportType;
    private String queryTemplate;
    private Map<String, Object> parametersSchema;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}

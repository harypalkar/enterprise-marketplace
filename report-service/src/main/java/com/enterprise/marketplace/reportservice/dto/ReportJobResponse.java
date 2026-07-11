package com.enterprise.marketplace.reportservice.dto;

import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
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
public class ReportJobResponse {

    private UUID id;
    private String requestId;
    private String reportCode;
    private String requestedBy;
    private ReportJobStatus status;
    private Map<String, Object> parameters;
    private Instant startedAt;
    private Instant completedAt;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}

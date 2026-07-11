package com.enterprise.marketplace.reportservice.dto;

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
public class ReportResultResponse {

    private UUID id;
    private UUID jobId;
    private Map<String, Object> resultData;
    private Integer rowCount;
    private String fileUrl;
    private Instant createdAt;
}

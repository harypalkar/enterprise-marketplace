package com.enterprise.marketplace.auditservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTimelineResponse {

    private String correlationId;
    private List<AuditResponse> entries;
    private int totalEntries;
}

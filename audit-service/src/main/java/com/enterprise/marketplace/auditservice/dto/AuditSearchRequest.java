package com.enterprise.marketplace.auditservice.dto;

import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSearchRequest {

    private AuditOperation operation;
    private String sourceService;
    private String actor;
    private Instant fromDate;
    private Instant toDate;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}

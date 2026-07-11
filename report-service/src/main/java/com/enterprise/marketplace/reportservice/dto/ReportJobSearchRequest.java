package com.enterprise.marketplace.reportservice.dto;

import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportJobSearchRequest {

    private String reportCode;
    private ReportJobStatus status;
    private String requestedBy;
    private int page;
    private int size;
}

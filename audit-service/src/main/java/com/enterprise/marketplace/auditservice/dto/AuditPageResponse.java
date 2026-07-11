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
public class AuditPageResponse {

    private List<AuditResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

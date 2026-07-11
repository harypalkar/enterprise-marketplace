package com.enterprise.marketplace.reportservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportJobPageResponse {

    private List<ReportJobResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

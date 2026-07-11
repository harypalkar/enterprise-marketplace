package com.enterprise.marketplace.adminservice.dto;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Instant generatedAt;
    private DomainSummary settings;
    private DomainSummary featureFlags;
    private DomainSummary configs;
    private long adminAuditTotal;
    private Map<String, Long> platformMetrics;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainSummary {

        private long total;
        private long active;
    }
}

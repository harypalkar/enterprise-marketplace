package com.enterprise.marketplace.reportservice.redis;

import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;
import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "false")
public class NoOpReportCacheService implements ReportCachePort {

    @Override
    public void cacheJob(ReportJobResponse job) {}

    @Override
    public void evictJob(UUID jobId) {}

    @Override
    public Optional<ReportJobResponse> getJob(UUID jobId) {
        return Optional.empty();
    }

    @Override
    public void cacheResult(ReportResultResponse result) {}

    @Override
    public void evictResult(UUID jobId) {}

    @Override
    public Optional<ReportResultResponse> getResult(UUID jobId) {
        return Optional.empty();
    }

    @Override
    public void cacheDefinition(ReportDefinitionResponse definition) {}

    @Override
    public void cacheDefinitions(List<ReportDefinitionResponse> definitions) {}

    @Override
    public void evictDefinitions() {}

    @Override
    public Optional<ReportDefinitionResponse> getDefinition(String reportCode) {
        return Optional.empty();
    }

    @Override
    public Optional<List<ReportDefinitionResponse>> getAllDefinitions() {
        return Optional.empty();
    }
}

package com.enterprise.marketplace.reportservice.redis;

import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;
import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportCachePort {

    void cacheJob(ReportJobResponse job);

    void evictJob(UUID jobId);

    Optional<ReportJobResponse> getJob(UUID jobId);

    void cacheResult(ReportResultResponse result);

    void evictResult(UUID jobId);

    Optional<ReportResultResponse> getResult(UUID jobId);

    void cacheDefinition(ReportDefinitionResponse definition);

    void cacheDefinitions(List<ReportDefinitionResponse> definitions);

    void evictDefinitions();

    Optional<ReportDefinitionResponse> getDefinition(String reportCode);

    Optional<List<ReportDefinitionResponse>> getAllDefinitions();
}

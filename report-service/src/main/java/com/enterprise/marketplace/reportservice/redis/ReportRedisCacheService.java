package com.enterprise.marketplace.reportservice.redis;

import com.enterprise.marketplace.reportservice.constants.ReportCacheKeys;
import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;
import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReportRedisCacheService implements ReportCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.redis.report-cache-ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Override
    public void cacheJob(ReportJobResponse job) {
        write(ReportCacheKeys.jobKey(job.getId()), job);
    }

    @Override
    public void evictJob(UUID jobId) {
        redisTemplate.delete(ReportCacheKeys.jobKey(jobId));
    }

    @Override
    public Optional<ReportJobResponse> getJob(UUID jobId) {
        return read(ReportCacheKeys.jobKey(jobId), ReportJobResponse.class);
    }

    @Override
    public void cacheResult(ReportResultResponse result) {
        write(ReportCacheKeys.resultKey(result.getJobId()), result);
    }

    @Override
    public void evictResult(UUID jobId) {
        redisTemplate.delete(ReportCacheKeys.resultKey(jobId));
    }

    @Override
    public Optional<ReportResultResponse> getResult(UUID jobId) {
        return read(ReportCacheKeys.resultKey(jobId), ReportResultResponse.class);
    }

    @Override
    public void cacheDefinition(ReportDefinitionResponse definition) {
        write(ReportCacheKeys.definitionKey(definition.getReportCode()), definition);
    }

    @Override
    public void cacheDefinitions(List<ReportDefinitionResponse> definitions) {
        write(ReportCacheKeys.DEFINITIONS_ALL, definitions);
    }

    @Override
    public void evictDefinitions() {
        redisTemplate.delete(ReportCacheKeys.DEFINITIONS_ALL);
    }

    @Override
    public Optional<ReportDefinitionResponse> getDefinition(String reportCode) {
        return read(ReportCacheKeys.definitionKey(reportCode), ReportDefinitionResponse.class);
    }

    @Override
    public Optional<List<ReportDefinitionResponse>> getAllDefinitions() {
        try {
            String cached = redisTemplate.opsForValue().get(ReportCacheKeys.DEFINITIONS_ALL);
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, new TypeReference<>() {}));
        } catch (Exception ex) {
            log.debug("Definition list cache miss", ex);
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(value), Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache key={}", key, ex);
        }
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, type));
        } catch (Exception ex) {
            log.debug("Cache miss or parse error for key={}", key, ex);
            return Optional.empty();
        }
    }
}

package com.enterprise.marketplace.searchservice.redis;

import com.enterprise.marketplace.searchservice.dto.ProductSearchPageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
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
public class SearchRedisCacheService implements SearchCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.redis.search-cache-ttl-seconds:300}")
    private long searchCacheTtlSeconds;

    @Override
    public void cacheSearchResult(String cacheKey, ProductSearchPageResponse response) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(cacheKey, objectMapper.writeValueAsString(response), Duration.ofSeconds(searchCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache search result key={}", cacheKey, ex);
        }
    }

    @Override
    public Optional<ProductSearchPageResponse> getSearchResult(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, ProductSearchPageResponse.class));
        } catch (Exception ex) {
            log.debug("Search cache miss key={}", cacheKey, ex);
            return Optional.empty();
        }
    }
}

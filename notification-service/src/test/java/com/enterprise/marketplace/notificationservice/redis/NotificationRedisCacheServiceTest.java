package com.enterprise.marketplace.notificationservice.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.notificationservice.constants.NotificationCacheKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationRedisCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private NotificationRedisCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new NotificationRedisCacheService(redisTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(cacheService, "templateCacheTtlSeconds", 3600L);
        ReflectionTestUtils.setField(cacheService, "notificationCacheTtlSeconds", 1800L);
        ReflectionTestUtils.setField(cacheService, "channelCacheTtlSeconds", 3600L);
        ReflectionTestUtils.setField(cacheService, "preferenceCacheTtlSeconds", 86400L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldDetectRateLimitExceeded() {
        when(valueOperations.get(NotificationCacheKeys.rateLimitKey("user-1", "EMAIL"))).thenReturn("100");
        assertThat(cacheService.isRateLimitExceeded("user-1", "EMAIL", 100)).isTrue();
    }

    @Test
    void shouldIncrementRateLimitCounter() {
        when(valueOperations.increment(NotificationCacheKeys.rateLimitKey("user-1", "SMS"))).thenReturn(1L);
        cacheService.incrementRateLimit("user-1", "SMS");
        verify(valueOperations).increment(NotificationCacheKeys.rateLimitKey("user-1", "SMS"));
        verify(redisTemplate).expire(anyString(), org.mockito.ArgumentMatchers.eq(java.time.Duration.ofHours(1)));
    }
}

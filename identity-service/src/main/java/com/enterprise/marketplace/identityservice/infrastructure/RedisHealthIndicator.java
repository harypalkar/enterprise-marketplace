package com.enterprise.marketplace.identityservice.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnBean(RedisConnectionFactory.class)
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            redisConnectionFactory.getConnection().ping();
            return Health.up().withDetail("component", "redis").build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("component", "redis").build();
        }
    }
}

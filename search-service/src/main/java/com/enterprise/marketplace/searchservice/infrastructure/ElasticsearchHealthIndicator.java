package com.enterprise.marketplace.searchservice.infrastructure;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(ElasticsearchClient.class)
@RequiredArgsConstructor
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Health health() {
        try {
            boolean result = elasticsearchClient.ping().value();
            if (result) {
                return Health.up().withDetail("component", "elasticsearch").build();
            }
            return Health.down().withDetail("component", "elasticsearch").withDetail("ping", false).build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("component", "elasticsearch").build();
        }
    }
}

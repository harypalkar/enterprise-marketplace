package com.enterprise.marketplace.searchservice.config;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.searchservice.infrastructure.ElasticsearchHealthIndicator;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
@ConditionalOnBean(ElasticsearchClient.class)
@RequiredArgsConstructor
public class SearchInfrastructureHealthConfiguration {

    private final ElasticsearchHealthIndicator elasticsearchHealthIndicator;

    @Bean
    SearchInfrastructureHealthEndpoints searchInfrastructureHealthEndpoints() {
        return new SearchInfrastructureHealthEndpoints(elasticsearchHealthIndicator);
    }

    @RequestMapping("/api/v1/infrastructure")
    static class SearchInfrastructureHealthEndpoints {

        private final ElasticsearchHealthIndicator elasticsearchHealthIndicator;

        SearchInfrastructureHealthEndpoints(ElasticsearchHealthIndicator elasticsearchHealthIndicator) {
            this.elasticsearchHealthIndicator = elasticsearchHealthIndicator;
        }

        @GetMapping("/health/elasticsearch")
        @ResponseBody
        ResponseEntity<ApiResponse<Map<String, Object>>> elasticsearchHealth() {
            Health health = elasticsearchHealthIndicator.health();
            Map<String, Object> payload = Map.of(
                    "status", health.getStatus().getCode(),
                    "details", health.getDetails());
            return ResponseEntity.ok(ApiResponse.success(payload, "Elasticsearch health"));
        }
    }
}

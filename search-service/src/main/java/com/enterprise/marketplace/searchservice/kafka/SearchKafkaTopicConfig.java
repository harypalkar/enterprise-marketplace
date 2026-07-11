package com.enterprise.marketplace.searchservice.kafka;

import com.enterprise.marketplace.searchservice.constants.SearchKafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(KafkaAdmin.class)
public class SearchKafkaTopicConfig {

    @Bean
    public NewTopic searchIndexTopic() {
        return TopicBuilder.name(SearchKafkaTopics.SEARCH_INDEX).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic searchIndexedTopic() {
        return TopicBuilder.name(SearchKafkaTopics.SEARCH_INDEXED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic searchFailedTopic() {
        return TopicBuilder.name(SearchKafkaTopics.SEARCH_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(SearchKafkaTopics.PRODUCT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(SearchKafkaTopics.PRODUCT_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productDeletedTopic() {
        return TopicBuilder.name(SearchKafkaTopics.PRODUCT_DELETED).partitions(3).replicas(1).build();
    }
}

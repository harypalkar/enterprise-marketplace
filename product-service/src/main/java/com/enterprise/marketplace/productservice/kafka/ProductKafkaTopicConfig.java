package com.enterprise.marketplace.productservice.kafka;

import com.enterprise.marketplace.productservice.constants.ProductKafkaTopics;
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
public class ProductKafkaTopicConfig {

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(ProductKafkaTopics.PRODUCT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(ProductKafkaTopics.PRODUCT_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productDeletedTopic() {
        return TopicBuilder.name(ProductKafkaTopics.PRODUCT_DELETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowUpdatedTopic() {
        return TopicBuilder.name(ProductKafkaTopics.WORKFLOW_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationCreatedTopic() {
        return TopicBuilder.name(ProductKafkaTopics.NOTIFICATION_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic searchIndexTopic() {
        return TopicBuilder.name(ProductKafkaTopics.SEARCH_INDEX).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(ProductKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productDeadLetterTopic() {
        return TopicBuilder.name(ProductKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}

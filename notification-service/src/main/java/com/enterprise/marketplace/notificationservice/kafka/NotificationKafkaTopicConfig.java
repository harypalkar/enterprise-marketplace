package com.enterprise.marketplace.notificationservice.kafka;

import com.enterprise.marketplace.notificationservice.constants.NotificationKafkaTopics;
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
public class NotificationKafkaTopicConfig {

    @Bean
    public NewTopic notificationCreatedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.NOTIFICATION_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationSentTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.NOTIFICATION_SENT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationFailedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.NOTIFICATION_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationDeliveredTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.NOTIFICATION_DELIVERED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowCompletedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.WORKFLOW_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowFailedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.WORKFLOW_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationRetryTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.NOTIFICATION_RETRY).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.PRODUCT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.PRODUCT_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sellerApprovedTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.SELLER_APPROVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic buyerRegisteredTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.BUYER_REGISTERED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryLowTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.INVENTORY_LOW).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionExpiredTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.SUBSCRIPTION_EXPIRED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationDeadLetterTopic() {
        return TopicBuilder.name(NotificationKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}

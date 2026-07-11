package com.enterprise.marketplace.reportservice.kafka;

import com.enterprise.marketplace.reportservice.constants.ReportKafkaTopics;
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
public class ReportKafkaTopicConfig {

    @Bean
    public NewTopic reportGeneratedTopic() {
        return TopicBuilder.name(ReportKafkaTopics.REPORT_GENERATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic reportFailedTopic() {
        return TopicBuilder.name(ReportKafkaTopics.REPORT_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(ReportKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowCompletedTopic() {
        return TopicBuilder.name(ReportKafkaTopics.WORKFLOW_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(ReportKafkaTopics.PRODUCT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic reportDeadLetterTopic() {
        return TopicBuilder.name(ReportKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}

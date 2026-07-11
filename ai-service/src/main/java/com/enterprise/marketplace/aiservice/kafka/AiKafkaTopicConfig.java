package com.enterprise.marketplace.aiservice.kafka;

import com.enterprise.marketplace.aiservice.constants.AiKafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiKafkaTopicConfig {

    @Bean
    NewTopic aiDescriptionGeneratedTopic() {
        return TopicBuilder.name(AiKafkaTopics.AI_DESCRIPTION_GENERATED).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic aiChatCompletedTopic() {
        return TopicBuilder.name(AiKafkaTopics.AI_CHAT_COMPLETED).partitions(3).replicas(1).build();
    }
}

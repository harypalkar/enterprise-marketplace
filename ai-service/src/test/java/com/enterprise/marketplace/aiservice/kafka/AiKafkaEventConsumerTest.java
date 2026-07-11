package com.enterprise.marketplace.aiservice.kafka;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.aiservice.service.AiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiKafkaEventConsumerTest {

    @Mock
    private AiService aiService;

    @InjectMocks
    private AiKafkaEventConsumer consumer;

    @Test
    void onProductCreatedDelegatesToService() {
        doNothing().when(aiService).processKafkaEvent("{}", "product-created");
        consumer.onProductCreated("{}");
        verify(aiService).processKafkaEvent("{}", "product-created");
    }

    @Test
    void onSearchIndexDelegatesToService() {
        doNothing().when(aiService).processKafkaEvent("{}", "search-index");
        consumer.onSearchIndex("{}");
        verify(aiService).processKafkaEvent("{}", "search-index");
    }
}

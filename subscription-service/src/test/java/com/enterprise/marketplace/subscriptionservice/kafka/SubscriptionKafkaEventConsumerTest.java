package com.enterprise.marketplace.subscriptionservice.kafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.subscriptionservice.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionKafkaEventConsumerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionKafkaEventConsumer consumer;

    @Test
    void shouldProcessWorkflowCompletedEvent() {
        String payload = "{\"requestId\":\"req-1\",\"workflowId\":\"00000000-0000-0000-0000-000000000001\"}";
        doNothing().when(subscriptionService).processWorkflowCompleted(eq(payload));

        consumer.onWorkflowCompleted(payload);

        verify(subscriptionService).processWorkflowCompleted(eq(payload));
    }
}

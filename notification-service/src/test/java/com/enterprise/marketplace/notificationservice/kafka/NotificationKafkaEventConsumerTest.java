package com.enterprise.marketplace.notificationservice.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationKafkaEventConsumer consumer;

    @Test
    void shouldProcessNotificationCreatedEvent() throws Exception {
        String payload = objectMapper.writeValueAsString(java.util.Map.of(
                "requestId", "kafka-req-1",
                "recipientId", "user-1",
                "notificationType", "WORKFLOW_COMPLETED",
                "channel", "IN_APP"));

        doNothing().when(notificationService).processFromKafkaEvent(eq(payload), eq("notification-created"));

        consumer.onNotificationCreated(payload);

        verify(notificationService).processFromKafkaEvent(eq(payload), eq("notification-created"));
    }

    @Test
    void shouldInvokeServiceForProductCreatedEvent() {
        doNothing().when(notificationService).processFromKafkaEvent(eq("{}"), eq("product-created"));
        consumer.onProductCreated("{}");
        verify(notificationService).processFromKafkaEvent(eq("{}"), eq("product-created"));
    }

    @Test
    void shouldInvokeServiceForSubscriptionExpiredEvent() {
        doNothing().when(notificationService).processFromKafkaEvent(eq("{}"), eq("subscription-expired"));
        consumer.onSubscriptionExpired("{}");
        verify(notificationService).processFromKafkaEvent(eq("{}"), eq("subscription-expired"));
    }
}

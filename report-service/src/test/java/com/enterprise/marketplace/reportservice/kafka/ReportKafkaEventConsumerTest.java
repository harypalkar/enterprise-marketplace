package com.enterprise.marketplace.reportservice.kafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.reportservice.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportKafkaEventConsumerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportKafkaEventConsumer consumer;

    @Test
    void shouldProcessWorkflowCompletedEvent() {
        String payload = "{\"requestId\":\"req-1\",\"workflowId\":\"00000000-0000-0000-0000-000000000001\",\"status\":\"COMPLETED\"}";
        doNothing().when(reportService).processExternalEvent(eq(payload), eq("workflow-completed"));

        consumer.onWorkflowCompleted(payload);

        verify(reportService).processExternalEvent(eq(payload), eq("workflow-completed"));
    }

    @Test
    void shouldProcessProductCreatedEvent() {
        String payload = "{\"requestId\":\"req-2\",\"productId\":\"00000000-0000-0000-0000-000000000002\"}";
        doNothing().when(reportService).processExternalEvent(eq(payload), eq("product-created"));

        consumer.onProductCreated(payload);

        verify(reportService).processExternalEvent(eq(payload), eq("product-created"));
    }
}

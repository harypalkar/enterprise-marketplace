package com.enterprise.marketplace.auditservice.kafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.auditservice.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditKafkaEventConsumerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditKafkaEventConsumer consumer;

    @Test
    void shouldProcessAuditCreatedEvent() {
        String payload = "{\"requestId\":\"req-1\",\"sourceService\":\"workflow-service\",\"operation\":\"CREATE\"}";
        doNothing().when(auditService).processFromKafkaEvent(eq(payload), eq("audit-created"));

        consumer.onAuditCreated(payload);

        verify(auditService).processFromKafkaEvent(eq(payload), eq("audit-created"));
    }
}

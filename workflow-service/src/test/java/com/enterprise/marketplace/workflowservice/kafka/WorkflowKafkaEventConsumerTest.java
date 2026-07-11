package com.enterprise.marketplace.workflowservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowKafkaEventConsumerTest {

    @Mock
    private WorkflowService workflowService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WorkflowKafkaEventConsumer consumer;

    @Test
    void shouldAdvanceWorkflowOnProductCreatedEvent() throws Exception {
        String payload = objectMapper.writeValueAsString(java.util.Map.of(
                "requestId", "kafka-req-1",
                "status", "DATABASE_SAVED"));

        when(workflowService.advanceWorkflowFromEvent(
                        eq("kafka-req-1"), eq(WorkflowStatus.DATABASE_SAVED), eq("product-created"), eq(payload)))
                .thenReturn(WorkflowResponse.builder()
                        .id(UUID.randomUUID())
                        .requestId("kafka-req-1")
                        .aggregateType(AggregateType.PRODUCT)
                        .aggregateId(UUID.randomUUID())
                        .operationType(WorkflowOperationType.CREATE)
                        .status(WorkflowStatus.DATABASE_SAVED)
                        .build());

        consumer.onProductCreated(payload);

        verify(workflowService)
                .advanceWorkflowFromEvent(
                        eq("kafka-req-1"), eq(WorkflowStatus.DATABASE_SAVED), eq("product-created"), eq(payload));
    }

    @Test
    void shouldSkipEventWithoutRequestId() {
        consumer.onProductCreated("{\"aggregateId\":\"123\"}");
        verify(workflowService, org.mockito.Mockito.never()).advanceWorkflowFromEvent(any(), any(), any(), any());
    }
}

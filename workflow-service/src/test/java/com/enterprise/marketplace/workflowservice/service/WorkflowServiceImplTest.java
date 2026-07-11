package com.enterprise.marketplace.workflowservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.workflowservice.audit.WorkflowAuditService;
import com.enterprise.marketplace.workflowservice.dto.CreateWorkflowRequest;
import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.mapper.WorkflowMapper;
import com.enterprise.marketplace.workflowservice.redis.WorkflowCachePort;
import com.enterprise.marketplace.workflowservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowEventRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowHistoryRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowRepository;
import com.enterprise.marketplace.workflowservice.service.impl.WorkflowServiceImpl;
import com.enterprise.marketplace.workflowservice.validation.WorkflowStatusTransitionValidator;
import com.enterprise.marketplace.workflowservice.workflow.WorkflowEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private WorkflowHistoryRepository historyRepository;
    @Mock
    private WorkflowEventRepository eventRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private WorkflowMapper workflowMapper;
    @Spy
    private WorkflowEngine workflowEngine = new WorkflowEngine();
    @Mock
    private WorkflowStatusTransitionValidator transitionValidator;
    @Mock
    private WorkflowAuditService auditService;
    @Mock
    private WorkflowCachePort cachePort;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WorkflowServiceImpl workflowService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowService, "outboxMaxRetries", 5);
    }

    @Test
    void shouldPersistWorkflowOnCreate() {
        CreateWorkflowRequest request = CreateWorkflowRequest.builder()
                .requestId("req-create-1")
                .aggregateType(AggregateType.PRODUCT)
                .aggregateId(UUID.randomUUID())
                .operationType(WorkflowOperationType.CREATE)
                .initiatedBy("tester")
                .build();

        when(workflowRepository.save(any(WorkflowEntity.class))).thenAnswer(invocation -> {
            WorkflowEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        workflowService.createWorkflow(request);

        verify(transitionValidator).validateRequestIdUnique("req-create-1");
        verify(workflowRepository).save(any(WorkflowEntity.class));
        verify(historyRepository).save(any());
        verify(outboxEventRepository, org.mockito.Mockito.times(2)).save(any());
        verify(cachePort).cacheWorkflow(any());
    }
}

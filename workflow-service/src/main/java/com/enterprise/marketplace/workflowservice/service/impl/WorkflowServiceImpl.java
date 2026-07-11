package com.enterprise.marketplace.workflowservice.service.impl;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.workflowservice.audit.WorkflowAuditService;
import com.enterprise.marketplace.workflowservice.constants.WorkflowKafkaTopics;
import com.enterprise.marketplace.workflowservice.dto.CreateWorkflowRequest;
import com.enterprise.marketplace.workflowservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.workflowservice.dto.UpdateWorkflowRequest;
import com.enterprise.marketplace.workflowservice.dto.WorkflowPageResponse;
import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowEventEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowHistoryEntity;
import com.enterprise.marketplace.workflowservice.enums.AuditOperation;
import com.enterprise.marketplace.workflowservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.mapper.WorkflowMapper;
import com.enterprise.marketplace.workflowservice.redis.WorkflowCachePort;
import com.enterprise.marketplace.workflowservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowEventRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowHistoryRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowRepository;
import com.enterprise.marketplace.workflowservice.service.WorkflowService;
import com.enterprise.marketplace.workflowservice.validation.WorkflowStatusTransitionValidator;
import com.enterprise.marketplace.workflowservice.workflow.WorkflowEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final WorkflowEventRepository eventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final WorkflowMapper workflowMapper;
    private final WorkflowEngine workflowEngine;
    private final WorkflowStatusTransitionValidator transitionValidator;
    private final WorkflowAuditService auditService;
    private final WorkflowCachePort cachePort;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.outbox.max-retries:5}")
    private int outboxMaxRetries;

    @Override
    @Transactional
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        transitionValidator.validateRequestIdUnique(request.getRequestId());
        applyRequestContext(request);

        WorkflowStatus initialStatus =
                request.getInitialStatus() != null ? request.getInitialStatus() : WorkflowStatus.INITIAL;

        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setRequestId(request.getRequestId());
        workflow.setCorrelationId(resolveCorrelationId(request.getCorrelationId()));
        workflow.setAggregateType(request.getAggregateType());
        workflow.setAggregateId(request.getAggregateId());
        workflow.setOperationType(request.getOperationType());
        workflow.setStatus(initialStatus);
        workflow.setTenantId(request.getTenantId());
        workflow.setSourceSystem(request.getSourceSystem());
        workflow.setInitiatedBy(request.getInitiatedBy());
        workflow.setMessage(request.getMessage());
        workflow.setMetadata(serializeMetadata(request.getMetadata()));
        workflow.setActive(true);

        WorkflowEntity saved = workflowRepository.save(workflow);

        WorkflowHistoryEntity history = workflowEngine.buildHistoryRecord(
                saved, null, initialStatus, "Workflow created", workflowEngine.resolveActor(request.getInitiatedBy()));
        historyRepository.save(history);

        auditService.recordAudit(
                saved,
                AuditOperation.CREATE,
                null,
                initialStatus,
                null,
                workflowMapper.toResponse(saved),
                workflowEngine.resolveActor(request.getInitiatedBy()));

        saveOutboxEvent(saved, "WORKFLOW_CREATED", WorkflowKafkaTopics.WORKFLOW_CREATED, buildEventPayload(saved));
        saveOutboxEvent(saved, "AUDIT_CREATED", WorkflowKafkaTopics.AUDIT_CREATED, buildAuditPayload(saved, AuditOperation.CREATE));

        WorkflowResponse response = workflowMapper.toResponse(saved, List.of(history));
        cachePort.cacheWorkflow(response);
        log.info("Workflow created id={} requestId={} status={}", saved.getId(), saved.getRequestId(), initialStatus);
        return response;
    }

    @Override
    public WorkflowResponse getWorkflow(UUID workflowId) {
        WorkflowEntity workflow = findActiveWorkflow(workflowId);
        List<WorkflowHistoryEntity> history = historyRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId);
        return workflowMapper.toResponse(workflow, history);
    }

    @Override
    public WorkflowResponse getWorkflowByRequestId(String requestId) {
        WorkflowEntity workflow = workflowRepository
                .findByRequestIdAndActiveTrue(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found for requestId " + requestId));
        return getWorkflow(workflow.getId());
    }

    @Override
    public WorkflowPageResponse getWorkflowsByStatus(WorkflowStatus status, int page, int size) {
        Page<WorkflowEntity> result = workflowRepository.findByStatusAndActiveTrue(
                status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<WorkflowResponse> content =
                result.getContent().stream().map(workflowMapper::toResponse).toList();
        return WorkflowPageResponse.builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public WorkflowResponse updateWorkflow(UUID workflowId, UpdateWorkflowRequest request) {
        WorkflowEntity workflow = findActiveWorkflow(workflowId);
        WorkflowResponse before = workflowMapper.toResponse(workflow);

        if (StringUtils.hasText(request.getTenantId())) {
            workflow.setTenantId(request.getTenantId());
        }
        if (StringUtils.hasText(request.getSourceSystem())) {
            workflow.setSourceSystem(request.getSourceSystem());
        }
        if (StringUtils.hasText(request.getInitiatedBy())) {
            workflow.setInitiatedBy(request.getInitiatedBy());
        }
        if (StringUtils.hasText(request.getMessage())) {
            workflow.setMessage(request.getMessage());
        }
        if (request.getMetadata() != null) {
            workflow.setMetadata(serializeMetadata(request.getMetadata()));
        }

        WorkflowEntity saved = workflowRepository.save(workflow);
        auditService.recordAudit(
                saved,
                AuditOperation.UPDATE,
                saved.getStatus(),
                saved.getStatus(),
                before,
                workflowMapper.toResponse(saved),
                workflowEngine.resolveActor(request.getInitiatedBy()));

        saveOutboxEvent(saved, "WORKFLOW_UPDATED", WorkflowKafkaTopics.WORKFLOW_UPDATED, buildEventPayload(saved));

        WorkflowResponse response = workflowMapper.toResponse(saved);
        cachePort.evictWorkflow(saved.getId());
        cachePort.cacheWorkflow(response);
        return response;
    }

    @Override
    @Transactional
    public WorkflowResponse updateStatus(UUID workflowId, StatusUpdateRequest request) {
        WorkflowEntity workflow = findActiveWorkflow(workflowId);
        return transitionWorkflow(
                workflow,
                request.getTargetStatus(),
                request.getReason() != null ? request.getReason() : request.getMessage(),
                workflowEngine.resolveActor(null));
    }

    @Override
    @Transactional
    public void deleteWorkflow(UUID workflowId) {
        WorkflowEntity workflow = findActiveWorkflow(workflowId);
        WorkflowResponse before = workflowMapper.toResponse(workflow);
        workflow.setActive(false);
        workflowRepository.save(workflow);

        auditService.recordAudit(
                workflow,
                AuditOperation.DELETE,
                workflow.getStatus(),
                workflow.getStatus(),
                before,
                null,
                workflowEngine.resolveActor(null));

        saveOutboxEvent(workflow, "WORKFLOW_CANCELLED", WorkflowKafkaTopics.WORKFLOW_CANCELLED, buildEventPayload(workflow));
        cachePort.evictWorkflow(workflowId);
        log.info("Workflow soft-deleted id={}", workflowId);
    }

    @Override
    @Transactional
    public WorkflowResponse advanceWorkflowFromEvent(
            String requestId, WorkflowStatus targetStatus, String eventSource, String payload) {
        WorkflowEntity workflow = workflowRepository
                .findByRequestIdAndActiveTrue(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found for requestId " + requestId));

        WorkflowEventEntity event = new WorkflowEventEntity();
        event.setWorkflowId(workflow.getId());
        event.setEventType(eventSource);
        event.setEventSource(eventSource);
        event.setPayload(payload);
        event.setCorrelationId(RequestContext.getCorrelationId());
        event.setRequestId(requestId);
        eventRepository.save(event);

        auditService.recordAudit(
                workflow,
                AuditOperation.EVENT_RECEIVED,
                workflow.getStatus(),
                targetStatus,
                null,
                payload,
                "kafka:" + eventSource);

        if (targetStatus != null && targetStatus != workflow.getStatus()) {
            return transitionWorkflow(workflow, targetStatus, "Advanced from " + eventSource, "kafka:" + eventSource);
        }
        return workflowMapper.toResponse(workflow);
    }

    private WorkflowResponse transitionWorkflow(
            WorkflowEntity workflow, WorkflowStatus targetStatus, String reason, String actor) {
        WorkflowStatus fromStatus = workflow.getStatus();
        transitionValidator.validateTransition(fromStatus, targetStatus);

        WorkflowResponse before = workflowMapper.toResponse(workflow);
        workflowEngine.applyTransition(workflow, targetStatus, reason, actor);
        WorkflowEntity saved = workflowRepository.save(workflow);

        WorkflowHistoryEntity history =
                workflowEngine.buildHistoryRecord(saved, fromStatus, targetStatus, reason, actor);
        historyRepository.save(history);

        auditService.recordAudit(
                saved,
                AuditOperation.STATUS_CHANGE,
                fromStatus,
                targetStatus,
                before,
                workflowMapper.toResponse(saved),
                actor);

        publishStatusOutboxEvents(saved, fromStatus, targetStatus);

        WorkflowResponse response =
                workflowMapper.toResponse(saved, historyRepository.findByWorkflowIdOrderByCreatedAtAsc(saved.getId()));
        cachePort.evictWorkflow(saved.getId());
        cachePort.cacheWorkflow(response);
        log.info(
                "Workflow transitioned id={} from={} to={}",
                saved.getId(),
                fromStatus,
                targetStatus);
        return response;
    }

    private void publishStatusOutboxEvents(
            WorkflowEntity workflow, WorkflowStatus fromStatus, WorkflowStatus targetStatus) {
        Map<String, Object> payload = buildEventPayload(workflow);
        payload.put("previousStatus", fromStatus.name());
        payload.put("status", targetStatus.name());

        saveOutboxEvent(workflow, "WORKFLOW_UPDATED", WorkflowKafkaTopics.WORKFLOW_UPDATED, payload);
        saveOutboxEvent(workflow, "AUDIT_CREATED", WorkflowKafkaTopics.AUDIT_CREATED, buildAuditPayload(workflow, AuditOperation.STATUS_CHANGE));

        if (targetStatus == WorkflowStatus.COMPLETED) {
            saveOutboxEvent(workflow, "WORKFLOW_COMPLETED", WorkflowKafkaTopics.WORKFLOW_COMPLETED, payload);
            saveOutboxEvent(
                    workflow,
                    "NOTIFICATION_CREATED",
                    WorkflowKafkaTopics.NOTIFICATION_CREATED,
                    buildNotificationPayload(workflow));
        } else if (targetStatus == WorkflowStatus.FAILED) {
            saveOutboxEvent(workflow, "WORKFLOW_FAILED", WorkflowKafkaTopics.WORKFLOW_FAILED, payload);
        } else if (targetStatus == WorkflowStatus.CANCELLED) {
            saveOutboxEvent(workflow, "WORKFLOW_CANCELLED", WorkflowKafkaTopics.WORKFLOW_CANCELLED, payload);
        }
    }

    private WorkflowEntity findActiveWorkflow(UUID workflowId) {
        return workflowRepository
                .findByIdAndActiveTrue(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + workflowId));
    }

    private void applyRequestContext(CreateWorkflowRequest request) {
        if (StringUtils.hasText(request.getCorrelationId())) {
            RequestContext.setCorrelationId(request.getCorrelationId());
        }
        RequestContext.getTenantId().ifPresent(t -> {
            if (!StringUtils.hasText(request.getTenantId())) {
                request.setTenantId(t);
            }
        });
    }

    private String resolveCorrelationId(String explicit) {
        if (StringUtils.hasText(explicit)) {
            return explicit;
        }
        String fromContext = RequestContext.getCorrelationId();
        return StringUtils.hasText(fromContext) ? fromContext : RequestContext.generateCorrelationId();
    }

    private void saveOutboxEvent(
            WorkflowEntity workflow, String eventType, String topic, Map<String, Object> payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType("Workflow");
        event.setAggregateId(workflow.getId());
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setPayload(serializePayload(payload));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(outboxMaxRetries);
        event.setCorrelationId(workflow.getCorrelationId());
        event.setRequestId(workflow.getRequestId());
        outboxEventRepository.save(event);
    }

    private Map<String, Object> buildEventPayload(WorkflowEntity workflow) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", workflow.getId().toString());
        payload.put("requestId", workflow.getRequestId());
        payload.put("correlationId", workflow.getCorrelationId());
        payload.put("aggregateType", workflow.getAggregateType().name());
        payload.put("aggregateId", workflow.getAggregateId().toString());
        payload.put("operationType", workflow.getOperationType().name());
        payload.put("status", workflow.getStatus().name());
        payload.put("previousStatus", workflow.getPreviousStatus() != null ? workflow.getPreviousStatus().name() : null);
        payload.put("sourceSystem", workflow.getSourceSystem());
        payload.put("initiatedBy", workflow.getInitiatedBy());
        payload.put("message", workflow.getMessage());
        return payload;
    }

    private Map<String, Object> buildAuditPayload(WorkflowEntity workflow, AuditOperation operation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", workflow.getId().toString());
        payload.put("requestId", workflow.getRequestId());
        payload.put("correlationId", workflow.getCorrelationId());
        payload.put("operation", operation.name());
        payload.put("status", workflow.getStatus().name());
        payload.put("actor", workflow.getInitiatedBy());
        return payload;
    }

    private Map<String, Object> buildNotificationPayload(WorkflowEntity workflow) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", workflow.getId().toString());
        payload.put("requestId", workflow.getRequestId());
        payload.put("aggregateType", workflow.getAggregateType().name());
        payload.put("aggregateId", workflow.getAggregateId().toString());
        payload.put("status", workflow.getStatus().name());
        payload.put("message", workflow.getMessage());
        return payload;
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid metadata JSON");
        }
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to serialize outbox payload");
        }
    }
}

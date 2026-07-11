package com.enterprise.marketplace.auditservice.service.impl;

import com.enterprise.marketplace.auditservice.config.AuditProperties;
import com.enterprise.marketplace.auditservice.constants.AuditKafkaTopics;
import com.enterprise.marketplace.auditservice.dto.AuditPageResponse;
import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.AuditSearchRequest;
import com.enterprise.marketplace.auditservice.dto.AuditTimelineResponse;
import com.enterprise.marketplace.auditservice.dto.CreateAuditRequest;
import com.enterprise.marketplace.auditservice.entity.AuditEventLogEntity;
import com.enterprise.marketplace.auditservice.entity.AuditRecordEntity;
import com.enterprise.marketplace.auditservice.entity.AuditTimelineEntity;
import com.enterprise.marketplace.auditservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import com.enterprise.marketplace.auditservice.enums.AuditStatus;
import com.enterprise.marketplace.auditservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.auditservice.mapper.AuditMapper;
import com.enterprise.marketplace.auditservice.redis.AuditCachePort;
import com.enterprise.marketplace.auditservice.repository.AuditEventLogRepository;
import com.enterprise.marketplace.auditservice.repository.AuditRecordRepository;
import com.enterprise.marketplace.auditservice.repository.AuditTimelineRepository;
import com.enterprise.marketplace.auditservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.auditservice.service.AuditService;
import com.enterprise.marketplace.auditservice.util.AuditEventKeyGenerator;
import com.enterprise.marketplace.auditservice.validation.AuditRequestValidator;
import com.enterprise.marketplace.common.constant.MarketplaceRoles;
import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditRecordRepository auditRecordRepository;
    private final AuditEventLogRepository auditEventLogRepository;
    private final AuditTimelineRepository auditTimelineRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final AuditMapper auditMapper;
    private final AuditRequestValidator requestValidator;
    private final AuditCachePort cachePort;
    private final AuditProperties auditProperties;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.outbox.max-retries:5}")
    private int outboxMaxRetries;

    @Value("${marketplace.security.enabled:true}")
    private boolean securityEnabled;

    @Override
    @Transactional
    public AuditResponse createAudit(CreateAuditRequest request) {
        requestValidator.validateCreateRequest(request);
        applyRequestContext(request);

        String eventKey = resolveEventKey(request);
        requestValidator.validateEventKeyUnique(eventKey);

        AuditRecordEntity record = buildAuditRecord(request, eventKey);
        record.setStatus(AuditStatus.RECORDED);
        record.setActive(true);

        AuditRecordEntity saved = auditRecordRepository.save(record);
        appendTimelineEntry(saved);
        saveOutboxEvent(saved, "AUDIT_INDEXED", AuditKafkaTopics.AUDIT_INDEXED, buildIndexedPayload(saved));

        saved.setStatus(AuditStatus.INDEXED);
        saved = auditRecordRepository.save(saved);

        AuditResponse response = auditMapper.toResponse(saved);
        cachePort.cacheAudit(response);
        refreshTimelineCache(saved.getCorrelationId());
        log.info(
                "Audit created id={} eventKey={} sourceService={} operation={}",
                saved.getId(),
                saved.getEventKey(),
                saved.getSourceService(),
                saved.getOperation());
        return response;
    }

    @Override
    public AuditResponse getAudit(UUID auditId) {
        return cachePort
                .getAudit(auditId)
                .orElseGet(() -> {
                    AuditRecordEntity record = findActiveRecord(auditId);
                    AuditResponse response = auditMapper.toResponse(record);
                    cachePort.cacheAudit(response);
                    return response;
                });
    }

    @Override
    public AuditResponse getByRequestId(String requestId) {
        AuditRecordEntity record = auditRecordRepository
                .findTopByRequestIdAndActiveTrueOrderByCreatedAtDesc(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit not found for requestId " + requestId));
        return auditMapper.toResponse(record);
    }

    @Override
    public AuditTimelineResponse getByCorrelationId(String correlationId) {
        return cachePort
                .getTimeline(correlationId)
                .orElseGet(() -> buildAndCacheTimeline(correlationId));
    }

    @Override
    public AuditPageResponse getByAggregate(String aggregateType, UUID aggregateId, int page, int size) {
        Page<AuditRecordEntity> result = auditRecordRepository.findByAggregateTypeAndAggregateIdAndActiveTrue(
                aggregateType, aggregateId, pageable(page, size));
        return toPageResponse(result);
    }

    @Override
    public AuditPageResponse getByActor(String actor, int page, int size) {
        Page<AuditRecordEntity> result =
                auditRecordRepository.findByActorAndActiveTrue(actor, pageable(page, size));
        return toPageResponse(result);
    }

    @Override
    public AuditPageResponse getBySourceService(String sourceService, int page, int size) {
        Page<AuditRecordEntity> result =
                auditRecordRepository.findBySourceServiceAndActiveTrue(sourceService, pageable(page, size));
        return toPageResponse(result);
    }

    @Override
    public AuditPageResponse searchAudits(AuditSearchRequest request) {
        int page = Math.max(request.getPage(), 0);
        int size = normalizePageSize(request.getSize());
        Page<AuditRecordEntity> result = auditRecordRepository.searchAudits(
                request.getOperation(),
                request.getSourceService(),
                request.getActor(),
                request.getFromDate(),
                request.getToDate(),
                pageable(page, size));
        return toPageResponse(result);
    }

    @Override
    @Transactional
    public void archiveAudit(UUID auditId) {
        ensureAdminRole();
        AuditRecordEntity record = findActiveRecord(auditId);
        record.setActive(false);
        record.setStatus(AuditStatus.ARCHIVED);
        AuditRecordEntity saved = auditRecordRepository.save(record);

        saveOutboxEvent(saved, "AUDIT_ARCHIVED", AuditKafkaTopics.AUDIT_ARCHIVED, buildArchivedPayload(saved));
        cachePort.evictAudit(auditId);
        refreshTimelineCache(saved.getCorrelationId());
        log.info("Audit archived id={} eventKey={}", auditId, saved.getEventKey());
    }

    @Override
    @Transactional
    public void processFromKafkaEvent(String payload, String eventSource) {
        AuditEventLogEntity eventLog = new AuditEventLogEntity();
        eventLog.setEventSource(eventSource);
        eventLog.setEventType("AUDIT_CREATED");
        eventLog.setPayload(payload);
        eventLog.setProcessed(false);
        auditEventLogRepository.save(eventLog);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String requestId = extractText(root, "requestId");
            eventLog.setRequestId(requestId);
            eventLog.setCorrelationId(extractText(root, "correlationId"));
            if (!StringUtils.hasText(requestId)) {
                markEventLogFailed(eventLog, "Missing requestId in kafka payload");
                return;
            }

            String sourceService = resolveSourceService(root, eventSource);
            AuditOperation operation = resolveOperation(root);
            String eventKey = AuditEventKeyGenerator.generate(sourceService, requestId, operation);

            if (auditRecordRepository.findByEventKey(eventKey).isPresent()) {
                log.debug("Audit already exists for eventKey={}, skipping {}", eventKey, eventSource);
                markEventLogProcessed(eventLog, null);
                return;
            }

            CreateAuditRequest request = buildCreateRequestFromKafka(root, sourceService, requestId, operation);
            AuditRecordEntity record = buildAuditRecord(request, eventKey);
            record.setStatus(AuditStatus.RECORDED);
            record.setActive(true);

            AuditRecordEntity saved = auditRecordRepository.save(record);
            appendTimelineEntry(saved);
            saveOutboxEvent(saved, "AUDIT_INDEXED", AuditKafkaTopics.AUDIT_INDEXED, buildIndexedPayload(saved));

            saved.setStatus(AuditStatus.INDEXED);
            saved = auditRecordRepository.save(saved);

            markEventLogProcessed(eventLog, saved.getId());

            AuditResponse response = auditMapper.toResponse(saved);
            cachePort.cacheAudit(response);
            refreshTimelineCache(saved.getCorrelationId());
            log.info(
                    "Audit ingested from kafka source={} id={} eventKey={}",
                    eventSource,
                    saved.getId(),
                    eventKey);
        } catch (MarketplaceException ex) {
            if (ex.getErrorCode() == ErrorCode.CONFLICT) {
                markEventLogProcessed(eventLog, null);
                return;
            }
            markEventLogFailed(eventLog, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            markEventLogFailed(eventLog, ex.getMessage());
            log.error("Failed to process kafka event {}", eventSource, ex);
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to process kafka event: " + eventSource);
        }
    }

    private AuditTimelineResponse buildAndCacheTimeline(String correlationId) {
        List<AuditTimelineEntity> timelineEntries =
                auditTimelineRepository.findByCorrelationIdOrderBySequenceNumberAsc(correlationId);
        if (timelineEntries.isEmpty()) {
            throw new ResourceNotFoundException("Audit timeline not found for correlationId " + correlationId);
        }

        List<AuditResponse> entries = new ArrayList<>();
        for (AuditTimelineEntity timelineEntry : timelineEntries) {
            auditRecordRepository
                    .findById(timelineEntry.getAuditRecordId())
                    .ifPresent(record -> entries.add(auditMapper.toResponse(record)));
        }

        AuditTimelineResponse timeline = AuditTimelineResponse.builder()
                .correlationId(correlationId)
                .entries(entries)
                .totalEntries(entries.size())
                .build();
        cachePort.cacheTimeline(timeline);
        return timeline;
    }

    private void refreshTimelineCache(String correlationId) {
        if (!StringUtils.hasText(correlationId)) {
            return;
        }
        try {
            buildAndCacheTimeline(correlationId);
        } catch (ResourceNotFoundException ex) {
            log.debug("No timeline to refresh for correlationId={}", correlationId);
        }
    }

    private AuditRecordEntity buildAuditRecord(CreateAuditRequest request, String eventKey) {
        AuditRecordEntity record = new AuditRecordEntity();
        record.setEventKey(eventKey);
        record.setRequestId(request.getRequestId());
        record.setCorrelationId(resolveCorrelationId(request.getCorrelationId()));
        record.setSourceService(request.getSourceService());
        record.setAggregateType(request.getAggregateType());
        record.setAggregateId(request.getAggregateId());
        record.setEntityType(request.getEntityType());
        record.setEntityId(request.getEntityId());
        record.setOperation(request.getOperation());
        record.setActor(resolveActor(request.getActor()));
        record.setBeforeState(serializeJson(request.getBeforeState()));
        record.setAfterState(serializeJson(request.getAfterState()));
        record.setMetadata(serializeJson(request.getMetadata()));
        record.setIpAddress(request.getIpAddress());
        record.setUserAgent(request.getUserAgent());
        return record;
    }

    private CreateAuditRequest buildCreateRequestFromKafka(
            JsonNode root, String sourceService, String requestId, AuditOperation operation) {
        return CreateAuditRequest.builder()
                .requestId(requestId)
                .correlationId(extractText(root, "correlationId"))
                .sourceService(sourceService)
                .aggregateType(extractText(root, "aggregateType"))
                .aggregateId(parseUuid(extractText(root, "aggregateId")))
                .entityType(resolveEntityType(root))
                .entityId(resolveEntityId(root))
                .operation(operation)
                .actor(resolveKafkaActor(root))
                .afterState(extractStateMap(root))
                .metadata(extractMetadataMap(root))
                .build();
    }

    private String resolveSourceService(JsonNode root, String eventSource) {
        String explicit = extractText(root, "sourceService");
        if (StringUtils.hasText(explicit)) {
            return explicit;
        }
        if (root.hasNonNull("workflowId")) {
            return "workflow-service";
        }
        if (root.hasNonNull("notificationId")) {
            return "notification-service";
        }
        if (root.hasNonNull("productId")) {
            return "product-service";
        }
        return "external-" + eventSource;
    }

    private AuditOperation resolveOperation(JsonNode root) {
        String operation = extractText(root, "operation");
        if (StringUtils.hasText(operation)) {
            return AuditOperation.valueOf(operation);
        }
        return AuditOperation.EVENT_RECEIVED;
    }

    private String resolveEntityType(JsonNode root) {
        if (root.hasNonNull("workflowId")) {
            return "Workflow";
        }
        if (root.hasNonNull("notificationId")) {
            return "Notification";
        }
        if (root.hasNonNull("productId")) {
            return "Product";
        }
        return extractText(root, "entityType");
    }

    private UUID resolveEntityId(JsonNode root) {
        UUID workflowId = parseUuid(extractText(root, "workflowId"));
        if (workflowId != null) {
            return workflowId;
        }
        UUID notificationId = parseUuid(extractText(root, "notificationId"));
        if (notificationId != null) {
            return notificationId;
        }
        UUID productId = parseUuid(extractText(root, "productId"));
        if (productId != null) {
            return productId;
        }
        return parseUuid(extractText(root, "entityId"));
    }

    private String resolveKafkaActor(JsonNode root) {
        String actor = extractText(root, "actor");
        if (StringUtils.hasText(actor)) {
            return actor;
        }
        actor = extractText(root, "initiatedBy");
        if (StringUtils.hasText(actor)) {
            return actor;
        }
        return "kafka";
    }

    private Map<String, Object> extractStateMap(JsonNode root) {
        Map<String, Object> state = new HashMap<>();
        root.fields().forEachRemaining(entry -> {
            if (entry.getValue() != null && entry.getValue().isValueNode()) {
                state.put(entry.getKey(), entry.getValue().asText());
            }
        });
        return state;
    }

    private Map<String, Object> extractMetadataMap(JsonNode root) {
        Map<String, Object> metadata = new HashMap<>();
        String status = extractText(root, "status");
        if (StringUtils.hasText(status)) {
            metadata.put("status", status);
        }
        String previousStatus = extractText(root, "previousStatus");
        if (StringUtils.hasText(previousStatus)) {
            metadata.put("previousStatus", previousStatus);
        }
        return metadata;
    }

    private void appendTimelineEntry(AuditRecordEntity record) {
        if (!StringUtils.hasText(record.getCorrelationId())) {
            return;
        }
        Long maxSequence = auditTimelineRepository.findMaxSequenceNumberByCorrelationId(record.getCorrelationId());
        AuditTimelineEntity timeline = new AuditTimelineEntity();
        timeline.setCorrelationId(record.getCorrelationId());
        timeline.setAuditRecordId(record.getId());
        timeline.setSequenceNumber(maxSequence + 1);
        auditTimelineRepository.save(timeline);
    }

    private void saveOutboxEvent(
            AuditRecordEntity record, String eventType, String topic, Map<String, Object> payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType("AuditRecord");
        event.setAggregateId(record.getId());
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setPayload(serializePayload(payload));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(outboxMaxRetries);
        event.setCorrelationId(record.getCorrelationId());
        event.setRequestId(record.getRequestId());
        outboxEventRepository.save(event);
    }

    private Map<String, Object> buildIndexedPayload(AuditRecordEntity record) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("auditId", record.getId().toString());
        payload.put("eventKey", record.getEventKey());
        payload.put("requestId", record.getRequestId());
        payload.put("correlationId", record.getCorrelationId());
        payload.put("sourceService", record.getSourceService());
        payload.put("aggregateType", record.getAggregateType());
        payload.put("aggregateId", record.getAggregateId() != null ? record.getAggregateId().toString() : null);
        payload.put("entityType", record.getEntityType());
        payload.put("entityId", record.getEntityId() != null ? record.getEntityId().toString() : null);
        payload.put("operation", record.getOperation().name());
        payload.put("actor", record.getActor());
        payload.put("status", record.getStatus().name());
        return payload;
    }

    private Map<String, Object> buildArchivedPayload(AuditRecordEntity record) {
        Map<String, Object> payload = buildIndexedPayload(record);
        payload.put("archivedAt", Instant.now().toString());
        return payload;
    }

    private AuditRecordEntity findActiveRecord(UUID auditId) {
        return auditRecordRepository
                .findByIdAndActiveTrue(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit not found: " + auditId));
    }

    private AuditPageResponse toPageResponse(Page<AuditRecordEntity> result) {
        List<AuditResponse> content = result.getContent().stream().map(auditMapper::toResponse).toList();
        return AuditPageResponse.builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(page, normalizePageSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return auditProperties.getDefaultPageSize();
        }
        return Math.min(size, auditProperties.getMaxPageSize());
    }

    private String resolveEventKey(CreateAuditRequest request) {
        if (StringUtils.hasText(request.getEventKey())) {
            return request.getEventKey().trim();
        }
        return AuditEventKeyGenerator.generate(
                request.getSourceService(), request.getRequestId(), request.getOperation());
    }

    private void applyRequestContext(CreateAuditRequest request) {
        if (StringUtils.hasText(request.getCorrelationId())) {
            RequestContext.setCorrelationId(request.getCorrelationId());
        }
    }

    private String resolveCorrelationId(String explicit) {
        if (StringUtils.hasText(explicit)) {
            return explicit;
        }
        String fromContext = RequestContext.getCorrelationId();
        return StringUtils.hasText(fromContext) ? fromContext : RequestContext.generateCorrelationId();
    }

    private String resolveActor(String explicit) {
        if (StringUtils.hasText(explicit)) {
            return explicit;
        }
        return RequestContext.getUserId().orElse("system");
    }

    private void ensureAdminRole() {
        if (!securityEnabled) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new MarketplaceException(ErrorCode.FORBIDDEN, "Archive requires ADMIN role");
        }
        String adminAuthority = MarketplaceRoles.ROLE_PREFIX + MarketplaceRoles.ADMIN;
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(adminAuthority::equals);
        if (!isAdmin) {
            throw new MarketplaceException(ErrorCode.FORBIDDEN, "Archive requires ADMIN role");
        }
    }

    private void markEventLogProcessed(AuditEventLogEntity eventLog, UUID auditRecordId) {
        eventLog.setProcessed(true);
        eventLog.setProcessedAt(Instant.now());
        eventLog.setAuditRecordId(auditRecordId);
        eventLog.setErrorMessage(null);
        auditEventLogRepository.save(eventLog);
    }

    private void markEventLogFailed(AuditEventLogEntity eventLog, String errorMessage) {
        eventLog.setProcessed(false);
        eventLog.setErrorMessage(truncate(errorMessage, 2000));
        auditEventLogRepository.save(eventLog);
    }

    private String serializeJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid JSON field value");
        }
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to serialize outbox payload");
        }
    }

    private String extractText(JsonNode node, String field) {
        if (node != null && node.hasNonNull(field)) {
            return node.get(field).asText();
        }
        return null;
    }

    private UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}

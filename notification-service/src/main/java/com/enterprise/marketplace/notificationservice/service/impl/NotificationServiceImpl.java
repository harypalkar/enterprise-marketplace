package com.enterprise.marketplace.notificationservice.service.impl;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.notificationservice.audit.NotificationAuditService;
import com.enterprise.marketplace.notificationservice.channel.ChannelDeliveryResult;
import com.enterprise.marketplace.notificationservice.channel.NotificationChannelDispatcher;
import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.constants.NotificationKafkaTopics;
import com.enterprise.marketplace.notificationservice.dto.CreateNotificationRequest;
import com.enterprise.marketplace.notificationservice.dto.InboxPageResponse;
import com.enterprise.marketplace.notificationservice.dto.NotificationPageResponse;
import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.notificationservice.dto.UpdateNotificationRequest;
import com.enterprise.marketplace.notificationservice.entity.NotificationDeliveryEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationInboxEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import com.enterprise.marketplace.notificationservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.notificationservice.enums.AuditOperation;
import com.enterprise.marketplace.notificationservice.enums.DeliveryStatus;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.enums.NotificationType;
import com.enterprise.marketplace.notificationservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.notificationservice.mapper.NotificationMapper;
import com.enterprise.marketplace.notificationservice.redis.NotificationCachePort;
import com.enterprise.marketplace.notificationservice.repository.NotificationDeliveryRepository;
import com.enterprise.marketplace.notificationservice.repository.NotificationInboxRepository;
import com.enterprise.marketplace.notificationservice.repository.NotificationRepository;
import com.enterprise.marketplace.notificationservice.repository.NotificationTemplateRepository;
import com.enterprise.marketplace.notificationservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.notificationservice.service.NotificationService;
import com.enterprise.marketplace.notificationservice.util.TemplateRenderer;
import com.enterprise.marketplace.notificationservice.validation.NotificationRequestValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final List<NotificationStatus> DISPATCHABLE_STATUSES =
            List.of(NotificationStatus.PENDING, NotificationStatus.QUEUED, NotificationStatus.RETRY);

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationInboxRepository inboxRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationRequestValidator requestValidator;
    private final NotificationAuditService auditService;
    private final NotificationCachePort cachePort;
    private final NotificationChannelDispatcher channelDispatcher;
    private final TemplateRenderer templateRenderer;
    private final NotificationProperties notificationProperties;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.outbox.max-retries:5}")
    private int outboxMaxRetries;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        requestValidator.validateCreateRequest(request);
        applyRequestContext(request);

        NotificationEntity notification = new NotificationEntity();
        notification.setRequestId(request.getRequestId());
        notification.setCorrelationId(resolveCorrelationId(request.getCorrelationId()));
        notification.setWorkflowId(request.getWorkflowId());
        notification.setAggregateType(request.getAggregateType());
        notification.setAggregateId(request.getAggregateId());
        notification.setNotificationType(request.getNotificationType());
        notification.setChannel(request.getChannel());
        notification.setRecipientId(request.getRecipientId());
        notification.setRecipientAddress(request.getRecipientAddress());
        notification.setSubject(request.getSubject());
        notification.setBody(StringUtils.hasText(request.getBody()) ? request.getBody() : "");
        notification.setTemplateCode(request.getTemplateCode());
        notification.setMetadata(serializeMetadata(request.getMetadata()));
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(0);
        notification.setMaxRetries(notificationProperties.getMaxDeliveryRetries());
        notification.setActive(true);

        applyTemplate(notification, request.getTemplateCode(), request.getTemplateVariables());
        requestValidator.validateChannelRequirements(
                notification.getChannel(),
                notification.getRecipientAddress(),
                notification.getBody(),
                notification.getTemplateCode());

        NotificationEntity saved = notificationRepository.save(notification);

        auditService.recordAudit(
                saved,
                AuditOperation.CREATE,
                null,
                NotificationStatus.PENDING,
                null,
                notificationMapper.toResponse(saved),
                resolveActor());

        saveOutboxEvent(saved, "NOTIFICATION_CREATED", NotificationKafkaTopics.NOTIFICATION_CREATED, buildEventPayload(saved));
        saveOutboxEvent(saved, "AUDIT_CREATED", NotificationKafkaTopics.AUDIT_CREATED, buildAuditPayload(saved, AuditOperation.CREATE));

        NotificationResponse response = notificationMapper.toResponse(saved);
        cachePort.cacheNotification(response);
        log.info(
                "Notification created id={} requestId={} channel={}",
                saved.getId(),
                saved.getRequestId(),
                saved.getChannel());
        return response;
    }

    @Override
    public NotificationResponse getNotification(UUID notificationId) {
        NotificationEntity notification = findActiveNotification(notificationId);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse getByRequestId(String requestId) {
        NotificationEntity notification = notificationRepository
                .findByRequestIdAndActiveTrue(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found for requestId " + requestId));
        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationPageResponse getByRecipientId(String recipientId, int page, int size) {
        Page<NotificationEntity> result = notificationRepository.findByRecipientIdAndActiveTrue(
                recipientId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return toPageResponse(result);
    }

    @Override
    public NotificationPageResponse getByStatus(NotificationStatus status, int page, int size) {
        Page<NotificationEntity> result = notificationRepository.findByStatusAndActiveTrue(
                status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return toPageResponse(result);
    }

    @Override
    public InboxPageResponse getInboxByRecipientId(String recipientId, int page, int size) {
        Page<NotificationInboxEntity> result = inboxRepository.findByRecipientIdOrderByCreatedAtDesc(
                recipientId, PageRequest.of(page, size));
        List<com.enterprise.marketplace.notificationservice.dto.InboxResponse> content =
                result.getContent().stream().map(notificationMapper::toInboxResponse).toList();
        return InboxPageResponse.builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse updateNotification(UUID notificationId, UpdateNotificationRequest request) {
        NotificationEntity notification = findActiveNotification(notificationId);
        NotificationResponse before = notificationMapper.toResponse(notification);

        if (request.getWorkflowId() != null) {
            notification.setWorkflowId(request.getWorkflowId());
        }
        if (StringUtils.hasText(request.getAggregateType())) {
            notification.setAggregateType(request.getAggregateType());
        }
        if (request.getAggregateId() != null) {
            notification.setAggregateId(request.getAggregateId());
        }
        if (request.getChannel() != null) {
            notification.setChannel(request.getChannel());
        }
        if (StringUtils.hasText(request.getRecipientId())) {
            notification.setRecipientId(request.getRecipientId());
        }
        if (StringUtils.hasText(request.getRecipientAddress())) {
            notification.setRecipientAddress(request.getRecipientAddress());
        }
        if (StringUtils.hasText(request.getSubject())) {
            notification.setSubject(request.getSubject());
        }
        if (StringUtils.hasText(request.getBody())) {
            notification.setBody(request.getBody());
        }
        if (StringUtils.hasText(request.getTemplateCode())) {
            notification.setTemplateCode(request.getTemplateCode());
        }
        if (request.getMetadata() != null) {
            notification.setMetadata(serializeMetadata(request.getMetadata()));
        }
        if (request.getTemplateCode() != null || request.getTemplateVariables() != null) {
            applyTemplate(
                    notification,
                    StringUtils.hasText(request.getTemplateCode())
                            ? request.getTemplateCode()
                            : notification.getTemplateCode(),
                    request.getTemplateVariables());
        }

        NotificationEntity saved = notificationRepository.save(notification);
        auditService.recordAudit(
                saved,
                AuditOperation.UPDATE,
                saved.getStatus(),
                saved.getStatus(),
                before,
                notificationMapper.toResponse(saved),
                resolveActor());

        saveOutboxEvent(saved, "NOTIFICATION_UPDATED", NotificationKafkaTopics.NOTIFICATION_CREATED, buildEventPayload(saved));

        NotificationResponse response = notificationMapper.toResponse(saved);
        cachePort.evictNotification(saved.getId());
        cachePort.cacheNotification(response);
        return response;
    }

    @Override
    @Transactional
    public NotificationResponse updateStatus(UUID notificationId, StatusUpdateRequest request) {
        NotificationEntity notification = findActiveNotification(notificationId);
        return transitionStatus(
                notification,
                request.getTargetStatus(),
                request.getMessage() != null ? request.getMessage() : "Status updated",
                resolveActor());
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        NotificationEntity notification = findActiveNotification(notificationId);
        NotificationResponse before = notificationMapper.toResponse(notification);
        notification.setActive(false);
        notificationRepository.save(notification);

        auditService.recordAudit(
                notification,
                AuditOperation.DELETE,
                notification.getStatus(),
                notification.getStatus(),
                before,
                null,
                resolveActor());

        saveOutboxEvent(
                notification,
                "NOTIFICATION_CANCELLED",
                NotificationKafkaTopics.NOTIFICATION_FAILED,
                buildEventPayload(notification));
        cachePort.evictNotification(notificationId);
        log.info("Notification soft-deleted id={}", notificationId);
    }

    @Override
    @Transactional
    public NotificationResponse retryNotification(UUID notificationId) {
        NotificationEntity notification = findActiveNotification(notificationId);
        requestValidator.validateRetryAllowed(
                notification.getStatus(), notification.getRetryCount(), notification.getMaxRetries());

        NotificationStatus fromStatus = notification.getStatus();
        NotificationResponse before = notificationMapper.toResponse(notification);
        notification.setPreviousStatus(fromStatus);
        notification.setStatus(NotificationStatus.RETRY);
        notification.setRetryCount(notification.getRetryCount() + 1);
        NotificationEntity saved = notificationRepository.save(notification);

        auditService.recordAudit(
                saved,
                AuditOperation.RETRY,
                fromStatus,
                NotificationStatus.RETRY,
                before,
                notificationMapper.toResponse(saved),
                resolveActor());

        saveOutboxEvent(saved, "NOTIFICATION_RETRY", NotificationKafkaTopics.NOTIFICATION_CREATED, buildEventPayload(saved));

        NotificationResponse response = notificationMapper.toResponse(saved);
        cachePort.evictNotification(saved.getId());
        cachePort.cacheNotification(response);
        return response;
    }

    @Override
    @Transactional
    public void processFromKafkaEvent(String payload, String eventSource) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String sourceRequestId = extractText(root, "requestId");
            if (!StringUtils.hasText(sourceRequestId)) {
                log.warn("Skipping {} event without requestId", eventSource);
                return;
            }

            String notificationRequestId = resolveKafkaNotificationRequestId(sourceRequestId, eventSource, root);
            if (notificationRepository.findByRequestIdAndActiveTrue(notificationRequestId).isPresent()) {
                log.debug("Notification already exists for requestId={}, skipping {}", notificationRequestId, eventSource);
                return;
            }

            CreateNotificationRequest request = buildCreateRequestFromKafka(root, eventSource, notificationRequestId);
            if (request == null) {
                return;
            }

            NotificationResponse created = createNotification(request);
            NotificationEntity notification = findActiveNotification(created.getId());
            auditService.recordAudit(
                    notification,
                    AuditOperation.EVENT_RECEIVED,
                    null,
                    notification.getStatus(),
                    null,
                    payload,
                    "kafka:" + eventSource);
        } catch (MarketplaceException ex) {
            if (ex.getErrorCode() == ErrorCode.CONFLICT) {
                log.debug("Duplicate notification from kafka event {}", eventSource);
                return;
            }
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to process kafka event {}", eventSource, ex);
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to process kafka event: " + eventSource);
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${marketplace.notification.dispatch-interval-ms:3000}")
    @Transactional
    public void dispatchPendingNotifications() {
        List<NotificationEntity> pending =
                notificationRepository.findTop50ByStatusInAndActiveTrueOrderByCreatedAtAsc(DISPATCHABLE_STATUSES);
        for (NotificationEntity notification : pending) {
            dispatchSingle(notification);
        }
    }

    private void dispatchSingle(NotificationEntity notification) {
        NotificationStatus fromStatus = notification.getStatus();
        notification.setPreviousStatus(fromStatus);
        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);

        int attemptNumber = notification.getRetryCount() + 1;
        ChannelDeliveryResult result = channelDispatcher.dispatch(notification);

        NotificationDeliveryEntity delivery = new NotificationDeliveryEntity();
        delivery.setNotificationId(notification.getId());
        delivery.setChannel(notification.getChannel());
        delivery.setAttemptNumber(attemptNumber);
        delivery.setStatus(result.success() ? DeliveryStatus.SUCCESS : DeliveryStatus.FAILED);
        delivery.setProviderResponse(truncate(result.response(), 2000));
        delivery.setErrorMessage(truncate(result.errorMessage(), 2000));
        deliveryRepository.save(delivery);

        NotificationStatus targetStatus;
        if (result.success()) {
            notification.setSentAt(Instant.now());
            if (notification.getChannel() == NotificationChannel.IN_APP) {
                notification.setDeliveredAt(Instant.now());
                targetStatus = NotificationStatus.DELIVERED;
            } else {
                targetStatus = NotificationStatus.SENT;
            }
        } else if (notification.getRetryCount() < notification.getMaxRetries()) {
            targetStatus = NotificationStatus.RETRY;
        } else {
            targetStatus = NotificationStatus.FAILED;
        }

        notification.setStatus(targetStatus);
        notificationRepository.save(notification);

        auditService.recordAudit(
                notification,
                AuditOperation.DISPATCH,
                fromStatus,
                targetStatus,
                null,
                notificationMapper.toResponse(notification),
                "scheduler");

        publishDispatchOutboxEvents(notification, targetStatus, result);

        cachePort.evictNotification(notification.getId());
        cachePort.cacheNotification(notificationMapper.toResponse(notification));
        log.info(
                "Notification dispatched id={} channel={} success={} status={}",
                notification.getId(),
                notification.getChannel(),
                result.success(),
                targetStatus);
    }

    private NotificationResponse transitionStatus(
            NotificationEntity notification, NotificationStatus targetStatus, String message, String actor) {
        NotificationStatus fromStatus = notification.getStatus();
        requestValidator.validateTransition(fromStatus, targetStatus);

        NotificationResponse before = notificationMapper.toResponse(notification);
        notification.setPreviousStatus(fromStatus);
        notification.setStatus(targetStatus);
        NotificationEntity saved = notificationRepository.save(notification);

        auditService.recordAudit(
                saved,
                AuditOperation.STATUS_CHANGE,
                fromStatus,
                targetStatus,
                before,
                notificationMapper.toResponse(saved),
                actor);

        publishStatusOutboxEvents(saved, fromStatus, targetStatus);

        NotificationResponse response = notificationMapper.toResponse(saved);
        cachePort.evictNotification(saved.getId());
        cachePort.cacheNotification(response);
        log.info(
                "Notification status changed id={} from={} to={} message={}",
                saved.getId(),
                fromStatus,
                targetStatus,
                message);
        return response;
    }

    private void publishStatusOutboxEvents(
            NotificationEntity notification, NotificationStatus fromStatus, NotificationStatus targetStatus) {
        Map<String, Object> payload = buildEventPayload(notification);
        payload.put("previousStatus", fromStatus.name());
        payload.put("status", targetStatus.name());

        saveOutboxEvent(notification, "NOTIFICATION_UPDATED", NotificationKafkaTopics.NOTIFICATION_CREATED, payload);
        saveOutboxEvent(
                notification,
                "AUDIT_CREATED",
                NotificationKafkaTopics.AUDIT_CREATED,
                buildAuditPayload(notification, AuditOperation.STATUS_CHANGE));

        if (targetStatus == NotificationStatus.SENT) {
            saveOutboxEvent(notification, "NOTIFICATION_SENT", NotificationKafkaTopics.NOTIFICATION_SENT, payload);
        } else if (targetStatus == NotificationStatus.DELIVERED) {
            saveOutboxEvent(notification, "NOTIFICATION_DELIVERED", NotificationKafkaTopics.NOTIFICATION_DELIVERED, payload);
        } else if (targetStatus == NotificationStatus.FAILED || targetStatus == NotificationStatus.CANCELLED) {
            saveOutboxEvent(notification, "NOTIFICATION_FAILED", NotificationKafkaTopics.NOTIFICATION_FAILED, payload);
        }
    }

    private void publishDispatchOutboxEvents(
            NotificationEntity notification, NotificationStatus targetStatus, ChannelDeliveryResult result) {
        Map<String, Object> payload = buildEventPayload(notification);
        payload.put("deliverySuccess", result.success());
        payload.put("deliveryResponse", result.response());
        payload.put("deliveryError", result.errorMessage());

        if (targetStatus == NotificationStatus.SENT) {
            saveOutboxEvent(notification, "NOTIFICATION_SENT", NotificationKafkaTopics.NOTIFICATION_SENT, payload);
        } else if (targetStatus == NotificationStatus.DELIVERED) {
            saveOutboxEvent(notification, "NOTIFICATION_DELIVERED", NotificationKafkaTopics.NOTIFICATION_DELIVERED, payload);
        } else if (targetStatus == NotificationStatus.FAILED || targetStatus == NotificationStatus.RETRY) {
            saveOutboxEvent(notification, "NOTIFICATION_FAILED", NotificationKafkaTopics.NOTIFICATION_FAILED, payload);
        }
    }

    private CreateNotificationRequest buildCreateRequestFromKafka(
            JsonNode root, String eventSource, String notificationRequestId) {
        if ("notification-created".equals(eventSource)) {
            return buildFromNotificationCreatedEvent(root, notificationRequestId);
        }
        if ("workflow-completed".equals(eventSource)) {
            return buildFromWorkflowEvent(root, notificationRequestId, NotificationType.WORKFLOW_COMPLETED);
        }
        if ("workflow-failed".equals(eventSource)) {
            return buildFromWorkflowEvent(root, notificationRequestId, NotificationType.WORKFLOW_FAILED);
        }
        log.warn("Unsupported kafka event source: {}", eventSource);
        return null;
    }

    private CreateNotificationRequest buildFromNotificationCreatedEvent(JsonNode root, String notificationRequestId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setRequestId(notificationRequestId);
        request.setCorrelationId(extractText(root, "correlationId"));
        request.setWorkflowId(parseUuid(extractText(root, "workflowId")));
        request.setAggregateType(extractText(root, "aggregateType"));
        request.setAggregateId(parseUuid(extractText(root, "aggregateId")));
        request.setRecipientId(extractText(root, "recipientId"));
        request.setRecipientAddress(extractText(root, "recipientAddress"));
        request.setSubject(extractText(root, "subject"));
        request.setBody(extractText(root, "body"));
        request.setTemplateCode(extractText(root, "templateCode"));

        String notificationType = extractText(root, "notificationType");
        if (StringUtils.hasText(notificationType)) {
            request.setNotificationType(NotificationType.valueOf(notificationType));
        } else {
            request.setNotificationType(NotificationType.CUSTOM);
        }

        String channel = extractText(root, "channel");
        if (StringUtils.hasText(channel)) {
            request.setChannel(NotificationChannel.valueOf(channel));
        } else {
            request.setChannel(NotificationChannel.IN_APP);
        }

        if (!StringUtils.hasText(request.getRecipientId())) {
            request.setRecipientId(extractText(root, "initiatedBy"));
        }
        if (!StringUtils.hasText(request.getRecipientId())) {
            request.setRecipientId("system");
        }

        request.setTemplateVariables(extractTemplateVariables(root));
        return request;
    }

    private CreateNotificationRequest buildFromWorkflowEvent(
            JsonNode root, String notificationRequestId, NotificationType notificationType) {
        Map<String, String> variables = extractTemplateVariables(root);
        return CreateNotificationRequest.builder()
                .requestId(notificationRequestId)
                .correlationId(extractText(root, "correlationId"))
                .workflowId(parseUuid(extractText(root, "workflowId")))
                .aggregateType(extractText(root, "aggregateType"))
                .aggregateId(parseUuid(extractText(root, "aggregateId")))
                .notificationType(notificationType)
                .channel(NotificationChannel.IN_APP)
                .recipientId(resolveRecipientId(root))
                .templateCode(notificationType.name())
                .templateVariables(variables)
                .build();
    }

    private String resolveRecipientId(JsonNode root) {
        String recipientId = extractText(root, "recipientId");
        if (StringUtils.hasText(recipientId)) {
            return recipientId;
        }
        recipientId = extractText(root, "initiatedBy");
        if (StringUtils.hasText(recipientId)) {
            return recipientId;
        }
        return "system";
    }

    private Map<String, String> extractTemplateVariables(JsonNode root) {
        Map<String, String> variables = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();
            if (value != null && value.isValueNode()) {
                variables.put(field.getKey(), value.asText());
            }
        }
        return variables;
    }

    private String resolveKafkaNotificationRequestId(String sourceRequestId, String eventSource, JsonNode root) {
        String explicitId = extractText(root, "notificationRequestId");
        if (StringUtils.hasText(explicitId)) {
            return explicitId;
        }
        if ("notification-created".equals(eventSource)) {
            return sourceRequestId;
        }
        return "notif-" + eventSource + "-" + sourceRequestId;
    }

    private void applyTemplate(
            NotificationEntity notification, String templateCode, Map<String, String> templateVariables) {
        if (!StringUtils.hasText(templateCode)) {
            return;
        }
        NotificationTemplateEntity template = resolveTemplate(templateCode, notification.getChannel());
        Map<String, String> variables =
                templateRenderer.mergeVariables(templateVariables, deserializeMetadataMap(notification.getMetadata()));

        if (!StringUtils.hasText(notification.getSubject())) {
            notification.setSubject(templateRenderer.render(template.getSubject(), variables));
        } else {
            notification.setSubject(templateRenderer.render(notification.getSubject(), variables));
        }

        if (!StringUtils.hasText(notification.getBody())) {
            notification.setBody(templateRenderer.render(template.getBodyTemplate(), variables));
        } else {
            notification.setBody(templateRenderer.render(notification.getBody(), variables));
        }
        notification.setTemplateCode(templateCode);
    }

    private NotificationTemplateEntity resolveTemplate(String templateCode, NotificationChannel channel) {
        return cachePort
                .getTemplate(templateCode, channel.name())
                .orElseGet(() -> templateRepository
                        .findByTemplateCodeAndChannelAndActiveTrue(templateCode, channel)
                        .map(template -> {
                            cachePort.cacheTemplate(template);
                            return template;
                        })
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Template not found: " + templateCode + " for channel " + channel)));
    }

    private NotificationEntity findActiveNotification(UUID notificationId) {
        return notificationRepository
                .findByIdAndActiveTrue(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
    }

    private NotificationPageResponse toPageResponse(Page<NotificationEntity> result) {
        List<NotificationResponse> content =
                result.getContent().stream().map(notificationMapper::toResponse).toList();
        return NotificationPageResponse.builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private void applyRequestContext(CreateNotificationRequest request) {
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

    private String resolveActor() {
        return RequestContext.getUserId().orElse("system");
    }

    private void saveOutboxEvent(
            NotificationEntity notification, String eventType, String topic, Map<String, Object> payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType("Notification");
        event.setAggregateId(notification.getId());
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setPayload(serializePayload(payload));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(outboxMaxRetries);
        event.setCorrelationId(notification.getCorrelationId());
        event.setRequestId(notification.getRequestId());
        outboxEventRepository.save(event);
    }

    private Map<String, Object> buildEventPayload(NotificationEntity notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getId().toString());
        payload.put("requestId", notification.getRequestId());
        payload.put("correlationId", notification.getCorrelationId());
        payload.put("workflowId", notification.getWorkflowId() != null ? notification.getWorkflowId().toString() : null);
        payload.put("aggregateType", notification.getAggregateType());
        payload.put("aggregateId", notification.getAggregateId() != null ? notification.getAggregateId().toString() : null);
        payload.put("notificationType", notification.getNotificationType().name());
        payload.put("channel", notification.getChannel().name());
        payload.put("recipientId", notification.getRecipientId());
        payload.put("recipientAddress", notification.getRecipientAddress());
        payload.put("subject", notification.getSubject());
        payload.put("body", notification.getBody());
        payload.put("status", notification.getStatus().name());
        payload.put("previousStatus", notification.getPreviousStatus() != null ? notification.getPreviousStatus().name() : null);
        payload.put("templateCode", notification.getTemplateCode());
        return payload;
    }

    private Map<String, Object> buildAuditPayload(NotificationEntity notification, AuditOperation operation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getId().toString());
        payload.put("requestId", notification.getRequestId());
        payload.put("correlationId", notification.getCorrelationId());
        payload.put("operation", operation.name());
        payload.put("status", notification.getStatus().name());
        payload.put("actor", resolveActor());
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

    private Map<String, Object> deserializeMetadataMap(String metadata) {
        if (!StringUtils.hasText(metadata)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadata, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of();
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

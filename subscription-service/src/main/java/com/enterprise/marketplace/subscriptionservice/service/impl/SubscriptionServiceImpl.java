package com.enterprise.marketplace.subscriptionservice.service.impl;



import com.enterprise.marketplace.common.context.RequestContext;

import com.enterprise.marketplace.common.exception.ErrorCode;

import com.enterprise.marketplace.common.exception.MarketplaceException;

import com.enterprise.marketplace.common.exception.ResourceNotFoundException;

import com.enterprise.marketplace.subscriptionservice.audit.SubscriptionAuditService;

import com.enterprise.marketplace.subscriptionservice.config.SubscriptionProperties;

import com.enterprise.marketplace.subscriptionservice.constants.SubscriptionKafkaTopics;

import com.enterprise.marketplace.subscriptionservice.dto.StatusUpdateRequest;

import com.enterprise.marketplace.subscriptionservice.dto.SubscribeRequest;

import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionPageResponse;

import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;

import com.enterprise.marketplace.subscriptionservice.entity.OutboxEventEntity;

import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionBillingEntity;

import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionEntity;

import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionPlanEntity;

import com.enterprise.marketplace.subscriptionservice.enums.AuditOperation;

import com.enterprise.marketplace.subscriptionservice.enums.BillingStatus;

import com.enterprise.marketplace.subscriptionservice.enums.OutboxEventStatus;

import com.enterprise.marketplace.subscriptionservice.enums.SubscriptionStatus;

import com.enterprise.marketplace.subscriptionservice.mapper.SubscriptionMapper;

import com.enterprise.marketplace.subscriptionservice.redis.SubscriptionCachePort;

import com.enterprise.marketplace.subscriptionservice.repository.OutboxEventRepository;

import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionBillingRepository;

import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionPlanRepository;

import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionRepository;

import com.enterprise.marketplace.subscriptionservice.service.SubscriptionService;

import com.enterprise.marketplace.subscriptionservice.util.SubscriptionPeriodCalculator;

import com.enterprise.marketplace.subscriptionservice.validation.SubscriptionRequestValidator;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

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

public class SubscriptionServiceImpl implements SubscriptionService {



    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionPlanRepository planRepository;

    private final SubscriptionBillingRepository billingRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final SubscriptionMapper subscriptionMapper;

    private final SubscriptionRequestValidator requestValidator;

    private final SubscriptionAuditService auditService;

    private final SubscriptionCachePort cachePort;

    private final SubscriptionProperties subscriptionProperties;

    private final ObjectMapper objectMapper;



    @Value("${marketplace.outbox.max-retries:5}")

    private int outboxMaxRetries;



    @Override

    @Transactional

    public SubscriptionResponse subscribe(SubscribeRequest request) {

        requestValidator.validateSubscribeRequest(request);

        applyRequestContext(request.getCorrelationId());



        SubscriptionPlanEntity plan = planRepository

                .findByPlanCodeAndActiveTrue(request.getPlanCode().trim().toUpperCase())

                .orElseThrow(() -> new MarketplaceException(

                        ErrorCode.VALIDATION_ERROR, "Active plan not found for planCode " + request.getPlanCode()));



        LocalDate startDate = LocalDate.now();

        LocalDate endDate = SubscriptionPeriodCalculator.calculateEndDate(startDate, plan.getBillingCycle());



        SubscriptionEntity subscription = new SubscriptionEntity();

        subscription.setRequestId(request.getRequestId());

        subscription.setSellerId(request.getSellerId());

        subscription.setBuyerId(request.getBuyerId());

        subscription.setPlan(plan);

        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscription.setStartDate(startDate);

        subscription.setEndDate(endDate);

        subscription.setAutoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : Boolean.FALSE);

        subscription.setActive(true);



        SubscriptionEntity saved = subscriptionRepository.save(subscription);

        createInitialBillingRecord(saved, plan);

        auditService.recordAudit(

                saved,

                AuditOperation.CREATE,

                null,

                saved.getStatus(),

                null,

                buildStateSnapshot(saved),

                resolveActor());



        saveOutboxEvent(saved, "SUBSCRIPTION_CREATED", SubscriptionKafkaTopics.SUBSCRIPTION_CREATED, buildEventPayload(saved));

        saveAuditOutboxEvent(saved, AuditOperation.CREATE);



        SubscriptionResponse response = subscriptionMapper.toSubscriptionResponse(saved);

        cachePort.cacheSubscription(response);

        log.info("Subscription created id={} requestId={} planCode={}", saved.getId(), saved.getRequestId(), plan.getPlanCode());

        return response;

    }



    @Override

    public SubscriptionResponse getSubscription(UUID subscriptionId) {

        return cachePort

                .getSubscription(subscriptionId)

                .orElseGet(() -> {

                    SubscriptionEntity subscription = findActiveSubscription(subscriptionId);

                    SubscriptionResponse response = subscriptionMapper.toSubscriptionResponse(subscription);

                    cachePort.cacheSubscription(response);

                    return response;

                });

    }



    @Override

    public SubscriptionPageResponse getBySeller(UUID sellerId, int page, int size) {

        Page<SubscriptionEntity> result =

                subscriptionRepository.findBySellerIdAndActiveTrue(sellerId, pageable(page, size));

        return toPageResponse(result);

    }



    @Override

    public SubscriptionPageResponse getByBuyer(UUID buyerId, int page, int size) {

        Page<SubscriptionEntity> result =

                subscriptionRepository.findByBuyerIdAndActiveTrue(buyerId, pageable(page, size));

        return toPageResponse(result);

    }



    @Override

    @Transactional

    public SubscriptionResponse updateStatus(UUID subscriptionId, StatusUpdateRequest request) {

        SubscriptionEntity subscription = findActiveSubscription(subscriptionId);

        SubscriptionStatus beforeStatus = subscription.getStatus();

        requestValidator.validateStatusTransition(beforeStatus, request.getStatus());



        subscription.setStatus(request.getStatus());

        SubscriptionEntity saved = subscriptionRepository.save(subscription);



        auditService.recordAudit(

                saved,

                AuditOperation.STATUS_CHANGE,

                beforeStatus,

                saved.getStatus(),

                Map.of("status", beforeStatus.name()),

                Map.of("status", saved.getStatus().name()),

                resolveActor());



        saveOutboxEvent(saved, "SUBSCRIPTION_UPDATED", SubscriptionKafkaTopics.SUBSCRIPTION_UPDATED, buildEventPayload(saved));

        saveAuditOutboxEvent(saved, AuditOperation.STATUS_CHANGE);



        cachePort.evictSubscription(subscriptionId);

        SubscriptionResponse response = subscriptionMapper.toSubscriptionResponse(saved);

        cachePort.cacheSubscription(response);

        log.info("Subscription status updated id={} from={} to={}", subscriptionId, beforeStatus, saved.getStatus());

        return response;

    }



    @Override

    @Transactional

    public SubscriptionResponse cancel(UUID subscriptionId) {

        SubscriptionEntity subscription = findActiveSubscription(subscriptionId);

        SubscriptionStatus beforeStatus = subscription.getStatus();

        if (beforeStatus == SubscriptionStatus.CANCELLED) {

            return subscriptionMapper.toSubscriptionResponse(subscription);

        }



        subscription.setStatus(SubscriptionStatus.CANCELLED);

        subscription.setAutoRenew(false);

        SubscriptionEntity saved = subscriptionRepository.save(subscription);



        auditService.recordAudit(

                saved,

                AuditOperation.CANCEL,

                beforeStatus,

                saved.getStatus(),

                Map.of("status", beforeStatus.name()),

                Map.of("status", saved.getStatus().name()),

                resolveActor());



        saveOutboxEvent(saved, "SUBSCRIPTION_CANCELLED", SubscriptionKafkaTopics.SUBSCRIPTION_CANCELLED, buildEventPayload(saved));

        saveAuditOutboxEvent(saved, AuditOperation.CANCEL);



        cachePort.evictSubscription(subscriptionId);

        SubscriptionResponse response = subscriptionMapper.toSubscriptionResponse(saved);

        cachePort.cacheSubscription(response);

        log.info("Subscription cancelled id={}", subscriptionId);

        return response;

    }



    @Override

    @Transactional

    public SubscriptionResponse renew(UUID subscriptionId) {

        SubscriptionEntity subscription = findActiveSubscription(subscriptionId);

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {

            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Cannot renew a cancelled subscription");

        }



        SubscriptionPlanEntity plan = subscription.getPlan();

        LocalDate renewStart = SubscriptionPeriodCalculator.resolveRenewStartDate(subscription.getEndDate());

        LocalDate newEndDate = SubscriptionPeriodCalculator.calculateEndDate(renewStart, plan.getBillingCycle());



        SubscriptionStatus beforeStatus = subscription.getStatus();

        subscription.setStartDate(renewStart);

        subscription.setEndDate(newEndDate);

        subscription.setStatus(SubscriptionStatus.ACTIVE);



        SubscriptionEntity saved = subscriptionRepository.save(subscription);

        createRenewalBillingRecord(saved, plan);



        auditService.recordAudit(

                saved,

                AuditOperation.RENEW,

                beforeStatus,

                saved.getStatus(),

                Map.of("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : null),

                Map.of("endDate", newEndDate != null ? newEndDate.toString() : null),

                resolveActor());



        saveOutboxEvent(saved, "SUBSCRIPTION_UPDATED", SubscriptionKafkaTopics.SUBSCRIPTION_UPDATED, buildEventPayload(saved));

        saveAuditOutboxEvent(saved, AuditOperation.RENEW);



        cachePort.evictSubscription(subscriptionId);

        SubscriptionResponse response = subscriptionMapper.toSubscriptionResponse(saved);

        cachePort.cacheSubscription(response);

        log.info("Subscription renewed id={} newEndDate={}", subscriptionId, newEndDate);

        return response;

    }



    @Override

    @Transactional

    public void processWorkflowCompleted(String payload) {

        try {

            JsonNode root = objectMapper.readTree(payload);

            String requestId = extractText(root, "requestId");

            if (!StringUtils.hasText(requestId)) {

                log.warn("workflow-completed event missing requestId, skipping");

                return;

            }



            subscriptionRepository.findByRequestIdAndActiveTrue(requestId).ifPresentOrElse(subscription -> {

                if (subscription.getStatus() == SubscriptionStatus.PENDING) {

                    SubscriptionStatus beforeStatus = subscription.getStatus();

                    subscription.setStatus(SubscriptionStatus.ACTIVE);

                    SubscriptionEntity saved = subscriptionRepository.save(subscription);



                    auditService.recordAudit(

                            saved,

                            AuditOperation.EVENT_RECEIVED,

                            beforeStatus,

                            saved.getStatus(),

                            null,

                            buildStateSnapshot(saved),

                            "workflow-completed");



                    saveOutboxEvent(

                            saved,

                            "SUBSCRIPTION_UPDATED",

                            SubscriptionKafkaTopics.SUBSCRIPTION_UPDATED,

                            buildEventPayload(saved));

                    cachePort.evictSubscription(saved.getId());

                    cachePort.cacheSubscription(subscriptionMapper.toSubscriptionResponse(saved));

                    log.info("Subscription activated from workflow-completed id={}", saved.getId());

                }

            }, () -> log.debug("No subscription found for workflow-completed requestId={}", requestId));

        } catch (Exception ex) {

            log.error("Failed to process workflow-completed event", ex);

            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to process workflow-completed event");

        }

    }



    private void createInitialBillingRecord(SubscriptionEntity subscription, SubscriptionPlanEntity plan) {

        if (plan.getPrice() == null || plan.getBillingCycle() == com.enterprise.marketplace.subscriptionservice.enums.BillingCycle.NONE) {

            return;

        }

        SubscriptionBillingEntity billing = new SubscriptionBillingEntity();

        billing.setSubscription(subscription);

        billing.setAmount(plan.getPrice());

        billing.setCurrency(plan.getCurrency());

        billing.setBillingDate(subscription.getStartDate());

        billing.setStatus(BillingStatus.PAID);

        billingRepository.save(billing);

    }



    private void createRenewalBillingRecord(SubscriptionEntity subscription, SubscriptionPlanEntity plan) {

        if (plan.getPrice() == null || plan.getBillingCycle() == com.enterprise.marketplace.subscriptionservice.enums.BillingCycle.NONE) {

            return;

        }

        SubscriptionBillingEntity billing = new SubscriptionBillingEntity();

        billing.setSubscription(subscription);

        billing.setAmount(plan.getPrice());

        billing.setCurrency(plan.getCurrency());

        billing.setBillingDate(subscription.getStartDate());

        billing.setStatus(BillingStatus.PENDING);

        billingRepository.save(billing);

    }



    private SubscriptionEntity findActiveSubscription(UUID subscriptionId) {

        return subscriptionRepository

                .findByIdAndActiveTrue(subscriptionId)

                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));

    }



    private SubscriptionPageResponse toPageResponse(Page<SubscriptionEntity> result) {

        List<SubscriptionResponse> content =

                result.getContent().stream().map(subscriptionMapper::toSubscriptionResponse).toList();

        return SubscriptionPageResponse.builder()

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

            return subscriptionProperties.getDefaultPageSize();

        }

        return Math.min(size, subscriptionProperties.getMaxPageSize());

    }



    private void applyRequestContext(String correlationId) {

        if (StringUtils.hasText(correlationId)) {

            RequestContext.setCorrelationId(correlationId);

        }

    }



    private String resolveActor() {

        return RequestContext.getUserId().orElse("system");

    }



    private Map<String, Object> buildStateSnapshot(SubscriptionEntity subscription) {

        Map<String, Object> state = new HashMap<>();

        state.put("subscriptionId", subscription.getId().toString());

        state.put("requestId", subscription.getRequestId());

        state.put("status", subscription.getStatus().name());

        state.put("planCode", subscription.getPlan().getPlanCode());

        return state;

    }



    private Map<String, Object> buildEventPayload(SubscriptionEntity subscription) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("subscriptionId", subscription.getId().toString());

        payload.put("requestId", subscription.getRequestId());

        payload.put("sellerId", subscription.getSellerId().toString());

        payload.put("buyerId", subscription.getBuyerId().toString());

        payload.put("planId", subscription.getPlan().getId().toString());

        payload.put("planCode", subscription.getPlan().getPlanCode());

        payload.put("status", subscription.getStatus().name());

        payload.put("startDate", subscription.getStartDate() != null ? subscription.getStartDate().toString() : null);

        payload.put("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : null);

        payload.put("autoRenew", subscription.getAutoRenew());

        payload.put("correlationId", RequestContext.getCorrelationId());

        return payload;

    }



    private void saveOutboxEvent(

            SubscriptionEntity subscription, String eventType, String topic, Map<String, Object> payload) {

        OutboxEventEntity event = new OutboxEventEntity();

        event.setAggregateType("Subscription");

        event.setAggregateId(subscription.getId());

        event.setEventType(eventType);

        event.setTopic(topic);

        event.setPayload(serializePayload(payload));

        event.setStatus(OutboxEventStatus.PENDING);

        event.setRetryCount(0);

        event.setMaxRetries(outboxMaxRetries);

        event.setCorrelationId(RequestContext.getCorrelationId());

        event.setRequestId(subscription.getRequestId());

        outboxEventRepository.save(event);

    }



    private void saveAuditOutboxEvent(SubscriptionEntity subscription, AuditOperation operation) {

        Map<String, Object> auditPayload = new HashMap<>();

        auditPayload.put("requestId", subscription.getRequestId());

        auditPayload.put("correlationId", RequestContext.getCorrelationId());

        auditPayload.put("sourceService", "subscription-service");

        auditPayload.put("aggregateType", "Subscription");

        auditPayload.put("aggregateId", subscription.getId().toString());

        auditPayload.put("entityType", "Subscription");

        auditPayload.put("entityId", subscription.getId().toString());

        auditPayload.put("operation", operation.name());

        auditPayload.put("actor", resolveActor());

        auditPayload.put("status", subscription.getStatus().name());



        OutboxEventEntity event = new OutboxEventEntity();

        event.setAggregateType("Subscription");

        event.setAggregateId(subscription.getId());

        event.setEventType("AUDIT_CREATED");

        event.setTopic(SubscriptionKafkaTopics.AUDIT_CREATED);

        event.setPayload(serializePayload(auditPayload));

        event.setStatus(OutboxEventStatus.PENDING);

        event.setRetryCount(0);

        event.setMaxRetries(outboxMaxRetries);

        event.setCorrelationId(RequestContext.getCorrelationId());

        event.setRequestId(subscription.getRequestId());

        outboxEventRepository.save(event);

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

}


package com.enterprise.marketplace.adminservice.service.impl;

import com.enterprise.marketplace.adminservice.constants.AdminKafkaTopics;
import com.enterprise.marketplace.adminservice.dto.BulkPatchFeatureFlagsRequest;
import com.enterprise.marketplace.adminservice.dto.ConfigResponse;
import com.enterprise.marketplace.adminservice.dto.CreateConfigRequest;
import com.enterprise.marketplace.adminservice.dto.CreateSettingRequest;
import com.enterprise.marketplace.adminservice.dto.DashboardResponse;
import com.enterprise.marketplace.adminservice.dto.FeatureFlagResponse;
import com.enterprise.marketplace.adminservice.dto.PatchFeatureFlagRequest;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import com.enterprise.marketplace.adminservice.dto.UpdateConfigRequest;
import com.enterprise.marketplace.adminservice.dto.UpdateSettingRequest;
import com.enterprise.marketplace.adminservice.entity.AdminAuditEntity;
import com.enterprise.marketplace.adminservice.entity.AdminConfigEntity;
import com.enterprise.marketplace.adminservice.entity.FeatureFlagEntity;
import com.enterprise.marketplace.adminservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.adminservice.entity.PlatformSettingEntity;
import com.enterprise.marketplace.adminservice.entity.PlatformStatEntity;
import com.enterprise.marketplace.adminservice.enums.AdminAction;
import com.enterprise.marketplace.adminservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.adminservice.mapper.AdminMapper;
import com.enterprise.marketplace.adminservice.redis.AdminCachePort;
import com.enterprise.marketplace.adminservice.repository.AdminAuditRepository;
import com.enterprise.marketplace.adminservice.repository.AdminConfigRepository;
import com.enterprise.marketplace.adminservice.repository.FeatureFlagRepository;
import com.enterprise.marketplace.adminservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.adminservice.repository.PlatformSettingRepository;
import com.enterprise.marketplace.adminservice.repository.PlatformStatRepository;
import com.enterprise.marketplace.adminservice.service.AdminService;
import com.enterprise.marketplace.adminservice.validation.AdminRequestValidator;
import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final String ENTITY_SETTING = "PlatformSetting";
    private static final String ENTITY_FEATURE_FLAG = "FeatureFlag";
    private static final String ENTITY_CONFIG = "AdminConfig";

    private final PlatformSettingRepository platformSettingRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final AdminConfigRepository adminConfigRepository;
    private final AdminAuditRepository adminAuditRepository;
    private final PlatformStatRepository platformStatRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final AdminMapper adminMapper;
    private final AdminRequestValidator requestValidator;
    private final AdminCachePort cachePort;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.outbox.max-retries:5}")
    private int outboxMaxRetries;

    @Override
    public List<SettingResponse> getSettings(String category) {
        if (!StringUtils.hasText(category)) {
            return cachePort.getAllSettings().orElseGet(this::loadAndCacheAllSettings);
        }
        return platformSettingRepository.findByCategoryOrderBySettingKeyAsc(category).stream()
                .map(adminMapper::toSettingResponse)
                .toList();
    }

    @Override
    @Transactional
    public SettingResponse createSetting(CreateSettingRequest request) {
        requestValidator.validateCreateSetting(request);

        PlatformSettingEntity entity = new PlatformSettingEntity();
        entity.setSettingKey(request.getSettingKey().trim());
        entity.setSettingValue(request.getSettingValue());
        entity.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory() : "GENERAL");
        entity.setDescription(request.getDescription());
        entity.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);

        PlatformSettingEntity saved = platformSettingRepository.save(entity);
        SettingResponse response = adminMapper.toSettingResponse(saved);

        recordAudit(AdminAction.CREATE, ENTITY_SETTING, saved.getSettingKey(), saved.getId(), null, response);
        publishConfigChanged("SETTING", saved.getSettingKey(), saved.getId(), response);
        publishAuditCreated("CREATE", ENTITY_SETTING, saved.getSettingKey(), saved.getId());

        cachePort.evictAllSettings();
        cachePort.cacheSetting(response);
        log.info("Platform setting created key={}", saved.getSettingKey());
        return response;
    }

    @Override
    @Transactional
    public SettingResponse updateSetting(String settingKey, UpdateSettingRequest request) {
        PlatformSettingEntity entity = findSetting(settingKey);
        SettingResponse before = adminMapper.toSettingResponse(entity);

        entity.setSettingValue(request.getSettingValue());
        if (StringUtils.hasText(request.getCategory())) {
            entity.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        PlatformSettingEntity saved = platformSettingRepository.save(entity);
        SettingResponse response = adminMapper.toSettingResponse(saved);

        recordAudit(AdminAction.UPDATE, ENTITY_SETTING, saved.getSettingKey(), saved.getId(), before, response);
        publishConfigChanged("SETTING", saved.getSettingKey(), saved.getId(), response);
        publishAuditCreated("UPDATE", ENTITY_SETTING, saved.getSettingKey(), saved.getId());

        cachePort.evictSetting(settingKey);
        cachePort.cacheSetting(response);
        log.info("Platform setting updated key={}", saved.getSettingKey());
        return response;
    }

    @Override
    @Transactional
    public void deleteSetting(String settingKey) {
        PlatformSettingEntity entity = findSetting(settingKey);
        SettingResponse before = adminMapper.toSettingResponse(entity);

        platformSettingRepository.delete(entity);

        recordAudit(AdminAction.DELETE, ENTITY_SETTING, settingKey, entity.getId(), before, null);
        publishConfigChanged("SETTING", settingKey, entity.getId(), Map.of("deleted", true, "settingKey", settingKey));
        publishAuditCreated("DELETE", ENTITY_SETTING, settingKey, entity.getId());

        cachePort.evictSetting(settingKey);
        log.info("Platform setting deleted key={}", settingKey);
    }

    @Override
    public List<FeatureFlagResponse> getFeatureFlags() {
        return cachePort.getAllFeatureFlags().orElseGet(this::loadAndCacheAllFeatureFlags);
    }

    @Override
    public FeatureFlagResponse getFeatureFlag(String flagKey) {
        return cachePort
                .getFeatureFlag(flagKey)
                .orElseGet(() -> {
                    FeatureFlagEntity entity = findFeatureFlag(flagKey);
                    FeatureFlagResponse response = adminMapper.toFeatureFlagResponse(entity);
                    cachePort.cacheFeatureFlag(response);
                    return response;
                });
    }

    @Override
    @Transactional
    public List<FeatureFlagResponse> patchFeatureFlags(BulkPatchFeatureFlagsRequest request) {
        requestValidator.validateBulkPatchFeatureFlags(request);
        return request.getUpdates().stream()
                .map(item -> {
                    PatchFeatureFlagRequest patch = PatchFeatureFlagRequest.builder()
                            .enabled(item.getEnabled())
                            .rolloutPercentage(item.getRolloutPercentage())
                            .build();
                    return patchFeatureFlag(item.getFlagKey(), patch);
                })
                .toList();
    }

    @Override
    @Transactional
    public FeatureFlagResponse patchFeatureFlag(String flagKey, PatchFeatureFlagRequest request) {
        requestValidator.validatePatchFeatureFlag(request);
        FeatureFlagEntity entity = findFeatureFlag(flagKey);
        FeatureFlagResponse before = adminMapper.toFeatureFlagResponse(entity);

        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getRolloutPercentage() != null) {
            entity.setRolloutPercentage(request.getRolloutPercentage());
        }

        FeatureFlagEntity saved = featureFlagRepository.save(entity);
        FeatureFlagResponse response = adminMapper.toFeatureFlagResponse(saved);

        recordAudit(AdminAction.PATCH, ENTITY_FEATURE_FLAG, saved.getFlagKey(), saved.getId(), before, response);
        publishFeatureToggled(saved);
        publishAuditCreated("PATCH", ENTITY_FEATURE_FLAG, saved.getFlagKey(), saved.getId());

        cachePort.evictFeatureFlag(flagKey);
        cachePort.cacheFeatureFlag(response);
        log.info("Feature flag patched key={} enabled={}", saved.getFlagKey(), saved.getEnabled());
        return response;
    }

    @Override
    public List<ConfigResponse> getConfigs(String scope) {
        List<AdminConfigEntity> entities = StringUtils.hasText(scope)
                ? adminConfigRepository.findByScopeOrderByConfigKeyAsc(scope)
                : adminConfigRepository.findAllByOrderByConfigKeyAsc();
        return entities.stream().map(adminMapper::toConfigResponse).toList();
    }

    @Override
    @Transactional
    public ConfigResponse createConfig(CreateConfigRequest request) {
        requestValidator.validateCreateConfig(request);

        AdminConfigEntity entity = new AdminConfigEntity();
        entity.setConfigKey(request.getConfigKey().trim());
        entity.setConfigValue(adminMapper.serializeJsonMap(request.getConfigValue()));
        entity.setScope(StringUtils.hasText(request.getScope()) ? request.getScope() : "GLOBAL");
        entity.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);

        AdminConfigEntity saved = adminConfigRepository.save(entity);
        ConfigResponse response = adminMapper.toConfigResponse(saved);

        recordAudit(AdminAction.CREATE, ENTITY_CONFIG, saved.getConfigKey(), saved.getId(), null, response);
        publishConfigChanged("CONFIG", saved.getConfigKey(), saved.getId(), response);
        publishAuditCreated("CREATE", ENTITY_CONFIG, saved.getConfigKey(), saved.getId());

        log.info("Admin config created key={}", saved.getConfigKey());
        return response;
    }

    @Override
    @Transactional
    public ConfigResponse updateConfig(String configKey, UpdateConfigRequest request) {
        AdminConfigEntity entity = findConfig(configKey);
        ConfigResponse before = adminMapper.toConfigResponse(entity);

        entity.setConfigValue(adminMapper.serializeJsonMap(request.getConfigValue()));
        if (StringUtils.hasText(request.getScope())) {
            entity.setScope(request.getScope());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        AdminConfigEntity saved = adminConfigRepository.save(entity);
        ConfigResponse response = adminMapper.toConfigResponse(saved);

        recordAudit(AdminAction.UPDATE, ENTITY_CONFIG, saved.getConfigKey(), saved.getId(), before, response);
        publishConfigChanged("CONFIG", saved.getConfigKey(), saved.getId(), response);
        publishAuditCreated("UPDATE", ENTITY_CONFIG, saved.getConfigKey(), saved.getId());

        log.info("Admin config updated key={}", saved.getConfigKey());
        return response;
    }

    @Override
    public DashboardResponse getDashboard() {
        Map<String, Long> platformMetrics = new LinkedHashMap<>();
        for (PlatformStatEntity stat : platformStatRepository.findAllByOrderByCategoryAscMetricKeyAsc()) {
            platformMetrics.put(stat.getMetricKey(), stat.getMetricValue());
        }

        return DashboardResponse.builder()
                .generatedAt(Instant.now())
                .settings(DashboardResponse.DomainSummary.builder()
                        .total(platformSettingRepository.count())
                        .active(platformSettingRepository.countByActiveTrue())
                        .build())
                .featureFlags(DashboardResponse.DomainSummary.builder()
                        .total(featureFlagRepository.count())
                        .active(featureFlagRepository.countByEnabledTrue())
                        .build())
                .configs(DashboardResponse.DomainSummary.builder()
                        .total(adminConfigRepository.count())
                        .active(adminConfigRepository.countByActiveTrue())
                        .build())
                .adminAuditTotal(adminAuditRepository.count())
                .platformMetrics(platformMetrics)
                .build();
    }

    private List<SettingResponse> loadAndCacheAllSettings() {
        List<SettingResponse> settings = platformSettingRepository.findAllByOrderBySettingKeyAsc().stream()
                .map(adminMapper::toSettingResponse)
                .toList();
        cachePort.cacheSettings(settings);
        return settings;
    }

    private List<FeatureFlagResponse> loadAndCacheAllFeatureFlags() {
        List<FeatureFlagResponse> flags = featureFlagRepository.findAllByOrderByFlagKeyAsc().stream()
                .map(adminMapper::toFeatureFlagResponse)
                .toList();
        cachePort.cacheFeatureFlags(flags);
        return flags;
    }

    private PlatformSettingEntity findSetting(String settingKey) {
        return platformSettingRepository
                .findBySettingKey(settingKey)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + settingKey));
    }

    private FeatureFlagEntity findFeatureFlag(String flagKey) {
        return featureFlagRepository
                .findByFlagKey(flagKey)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found: " + flagKey));
    }

    private AdminConfigEntity findConfig(String configKey) {
        return adminConfigRepository
                .findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found: " + configKey));
    }

    private void recordAudit(
            AdminAction action,
            String entityType,
            String entityKey,
            UUID entityId,
            Object before,
            Object after) {
        AdminAuditEntity audit = new AdminAuditEntity();
        audit.setAction(action);
        audit.setEntityType(entityType);
        audit.setEntityKey(entityKey);
        audit.setEntityId(entityId);
        audit.setActor(resolveActor());
        audit.setBeforeState(serializeState(before));
        audit.setAfterState(serializeState(after));
        audit.setCorrelationId(resolveCorrelationId());
        audit.setRequestId(RequestContext.getRequestId());
        adminAuditRepository.save(audit);
    }

    private void publishConfigChanged(String configType, String configKey, UUID aggregateId, Object payload) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("configType", configType);
        eventPayload.put("configKey", configKey);
        eventPayload.put("aggregateId", aggregateId.toString());
        eventPayload.put("payload", payload);
        eventPayload.put("sourceService", "admin-service");
        eventPayload.put("requestId", RequestContext.getRequestId());
        eventPayload.put("correlationId", resolveCorrelationId());
        eventPayload.put("timestamp", Instant.now().toString());
        saveOutboxEvent("AdminConfig", aggregateId, "ADMIN_CONFIG_CHANGED", AdminKafkaTopics.ADMIN_CONFIG_CHANGED, eventPayload);
    }

    private void publishFeatureToggled(FeatureFlagEntity flag) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("flagKey", flag.getFlagKey());
        eventPayload.put("enabled", flag.getEnabled());
        eventPayload.put("rolloutPercentage", flag.getRolloutPercentage());
        eventPayload.put("sourceService", "admin-service");
        eventPayload.put("requestId", RequestContext.getRequestId());
        eventPayload.put("correlationId", resolveCorrelationId());
        eventPayload.put("timestamp", Instant.now().toString());
        saveOutboxEvent(
                "FeatureFlag", flag.getId(), "ADMIN_FEATURE_TOGGLED", AdminKafkaTopics.ADMIN_FEATURE_TOGGLED, eventPayload);
    }

    private void publishAuditCreated(String operation, String entityType, String entityKey, UUID entityId) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("sourceService", "admin-service");
        eventPayload.put("requestId", RequestContext.getRequestId());
        eventPayload.put("correlationId", resolveCorrelationId());
        eventPayload.put("operation", operation);
        eventPayload.put("entityType", entityType);
        eventPayload.put("entityKey", entityKey);
        eventPayload.put("entityId", entityId.toString());
        eventPayload.put("actor", resolveActor());
        eventPayload.put("timestamp", Instant.now().toString());
        saveOutboxEvent("AdminAudit", entityId, "AUDIT_CREATED", AdminKafkaTopics.AUDIT_CREATED, eventPayload);
    }

    private void saveOutboxEvent(
            String aggregateType, UUID aggregateId, String eventType, String topic, Map<String, Object> payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setPayload(serializePayload(payload));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(outboxMaxRetries);
        event.setCorrelationId(resolveCorrelationId());
        event.setRequestId(RequestContext.getRequestId());
        outboxEventRepository.save(event);
    }

    private String resolveActor() {
        return RequestContext.getUserId().orElse("admin");
    }

    private String resolveCorrelationId() {
        String fromContext = RequestContext.getCorrelationId();
        return StringUtils.hasText(fromContext) ? fromContext : RequestContext.generateCorrelationId();
    }

    private String serializeState(Object state) {
        if (state == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(state);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to serialize audit state");
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

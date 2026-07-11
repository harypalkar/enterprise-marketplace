package com.enterprise.marketplace.adminservice.mapper;

import com.enterprise.marketplace.adminservice.dto.ConfigResponse;
import com.enterprise.marketplace.adminservice.dto.FeatureFlagResponse;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import com.enterprise.marketplace.adminservice.entity.AdminConfigEntity;
import com.enterprise.marketplace.adminservice.entity.FeatureFlagEntity;
import com.enterprise.marketplace.adminservice.entity.PlatformSettingEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminMapper {

    private final ObjectMapper objectMapper;

    public SettingResponse toSettingResponse(PlatformSettingEntity entity) {
        return SettingResponse.builder()
                .id(entity.getId())
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public FeatureFlagResponse toFeatureFlagResponse(FeatureFlagEntity entity) {
        return FeatureFlagResponse.builder()
                .id(entity.getId())
                .flagKey(entity.getFlagKey())
                .enabled(entity.getEnabled())
                .description(entity.getDescription())
                .rolloutPercentage(entity.getRolloutPercentage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ConfigResponse toConfigResponse(AdminConfigEntity entity) {
        return ConfigResponse.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(deserializeJsonMap(entity.getConfigValue()))
                .scope(entity.getScope())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Map<String, Object> deserializeJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    public String serializeJsonMap(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON config value");
        }
    }
}

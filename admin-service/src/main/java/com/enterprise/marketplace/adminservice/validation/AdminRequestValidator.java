package com.enterprise.marketplace.adminservice.validation;

import com.enterprise.marketplace.adminservice.dto.BulkPatchFeatureFlagsRequest;
import com.enterprise.marketplace.adminservice.dto.CreateConfigRequest;
import com.enterprise.marketplace.adminservice.dto.CreateSettingRequest;
import com.enterprise.marketplace.adminservice.dto.PatchFeatureFlagRequest;
import com.enterprise.marketplace.adminservice.repository.AdminConfigRepository;
import com.enterprise.marketplace.adminservice.repository.FeatureFlagRepository;
import com.enterprise.marketplace.adminservice.repository.PlatformSettingRepository;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminRequestValidator {

    private final PlatformSettingRepository platformSettingRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final AdminConfigRepository adminConfigRepository;

    public void validateCreateSetting(CreateSettingRequest request) {
        if (!StringUtils.hasText(request.getSettingKey())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "settingKey is required");
        }
        if (!StringUtils.hasText(request.getSettingValue())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "settingValue is required");
        }
        platformSettingRepository.findBySettingKey(request.getSettingKey().trim()).ifPresent(existing -> {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Setting already exists for key " + request.getSettingKey());
        });
    }

    public void validateCreateConfig(CreateConfigRequest request) {
        if (!StringUtils.hasText(request.getConfigKey())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "configKey is required");
        }
        if (request.getConfigValue() == null || request.getConfigValue().isEmpty()) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "configValue is required");
        }
        adminConfigRepository.findByConfigKey(request.getConfigKey().trim()).ifPresent(existing -> {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Config already exists for key " + request.getConfigKey());
        });
    }

    public void validatePatchFeatureFlag(PatchFeatureFlagRequest request) {
        if (request.getEnabled() == null
                && request.getRolloutPercentage() == null
                && !StringUtils.hasText(request.getDescription())) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR, "At least one field must be provided for patch");
        }
        if (request.getRolloutPercentage() != null
                && (request.getRolloutPercentage() < 0 || request.getRolloutPercentage() > 100)) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR, "rolloutPercentage must be between 0 and 100");
        }
    }

    public void validateBulkPatchFeatureFlags(BulkPatchFeatureFlagsRequest request) {
        for (BulkPatchFeatureFlagsRequest.FeatureFlagPatchItem item : request.getUpdates()) {
            if (!StringUtils.hasText(item.getFlagKey())) {
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "flagKey is required for each update");
            }
            if (item.getEnabled() == null && item.getRolloutPercentage() == null) {
                throw new MarketplaceException(
                        ErrorCode.VALIDATION_ERROR,
                        "At least enabled or rolloutPercentage must be provided for " + item.getFlagKey());
            }
            if (item.getRolloutPercentage() != null
                    && (item.getRolloutPercentage() < 0 || item.getRolloutPercentage() > 100)) {
                throw new MarketplaceException(
                        ErrorCode.VALIDATION_ERROR,
                        "rolloutPercentage must be between 0 and 100 for " + item.getFlagKey());
            }
            if (!featureFlagRepository.findByFlagKey(item.getFlagKey()).isPresent()) {
                throw new MarketplaceException(
                        ErrorCode.RESOURCE_NOT_FOUND, "Feature flag not found: " + item.getFlagKey());
            }
        }
    }
}

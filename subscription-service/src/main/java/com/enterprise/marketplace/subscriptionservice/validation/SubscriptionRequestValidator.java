package com.enterprise.marketplace.subscriptionservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.subscriptionservice.dto.CreatePlanRequest;
import com.enterprise.marketplace.subscriptionservice.dto.SubscribeRequest;
import com.enterprise.marketplace.subscriptionservice.enums.SubscriptionStatus;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionPlanRepository;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SubscriptionRequestValidator {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    public void validateCreatePlanRequest(CreatePlanRequest request) {
        if (planRepository.existsByPlanCode(request.getPlanCode())) {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Plan already exists for planCode " + request.getPlanCode());
        }
    }

    public void validateSubscribeRequest(SubscribeRequest request) {
        if (!StringUtils.hasText(request.getRequestId())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "requestId is required");
        }
        if (request.getSellerId() == null) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "sellerId is required");
        }
        if (request.getBuyerId() == null) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "buyerId is required");
        }
        if (!StringUtils.hasText(request.getPlanCode())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "planCode is required");
        }
        if (subscriptionRepository.existsByRequestId(request.getRequestId())) {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Subscription already exists for requestId " + request.getRequestId());
        }
        planRepository
                .findByPlanCodeAndActiveTrue(request.getPlanCode())
                .orElseThrow(() -> new MarketplaceException(
                        ErrorCode.VALIDATION_ERROR, "Active plan not found for planCode " + request.getPlanCode()));
    }

    public void validateStatusTransition(SubscriptionStatus current, SubscriptionStatus target) {
        if (current == target) {
            return;
        }
        if (current == SubscriptionStatus.CANCELLED || current == SubscriptionStatus.EXPIRED) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR,
                    "Cannot transition from " + current + " to " + target);
        }
    }
}

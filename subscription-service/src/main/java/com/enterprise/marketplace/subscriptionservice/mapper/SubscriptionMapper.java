package com.enterprise.marketplace.subscriptionservice.mapper;

import com.enterprise.marketplace.subscriptionservice.dto.PlanResponse;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;
import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionEntity;
import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionPlanEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {

    private final ObjectMapper objectMapper;

    public PlanResponse toPlanResponse(SubscriptionPlanEntity entity) {
        return PlanResponse.builder()
                .id(entity.getId())
                .planCode(entity.getPlanCode())
                .name(entity.getName())
                .tier(entity.getTier())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .billingCycle(entity.getBillingCycle())
                .features(deserializeJsonMap(entity.getFeatures()))
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SubscriptionResponse toSubscriptionResponse(SubscriptionEntity entity) {
        SubscriptionPlanEntity plan = entity.getPlan();
        return SubscriptionResponse.builder()
                .id(entity.getId())
                .requestId(entity.getRequestId())
                .sellerId(entity.getSellerId())
                .buyerId(entity.getBuyerId())
                .planId(plan.getId())
                .planCode(plan.getPlanCode())
                .planName(plan.getName())
                .planTier(plan.getTier())
                .billingCycle(plan.getBillingCycle())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .autoRenew(entity.getAutoRenew())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Map<String, Object> deserializeJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}

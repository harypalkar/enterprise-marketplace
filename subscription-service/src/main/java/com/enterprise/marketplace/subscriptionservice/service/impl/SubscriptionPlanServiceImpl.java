package com.enterprise.marketplace.subscriptionservice.service.impl;

import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.subscriptionservice.dto.CreatePlanRequest;
import com.enterprise.marketplace.subscriptionservice.dto.PlanListResponse;
import com.enterprise.marketplace.subscriptionservice.dto.PlanResponse;
import com.enterprise.marketplace.subscriptionservice.dto.UpdatePlanRequest;
import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionPlanEntity;
import com.enterprise.marketplace.subscriptionservice.mapper.SubscriptionMapper;
import com.enterprise.marketplace.subscriptionservice.redis.SubscriptionCachePort;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionPlanRepository;
import com.enterprise.marketplace.subscriptionservice.service.SubscriptionPlanService;
import com.enterprise.marketplace.subscriptionservice.validation.SubscriptionRequestValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionRequestValidator requestValidator;
    private final SubscriptionCachePort cachePort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        requestValidator.validateCreatePlanRequest(request);

        SubscriptionPlanEntity plan = new SubscriptionPlanEntity();
        plan.setPlanCode(request.getPlanCode().trim().toUpperCase());
        plan.setName(request.getName());
        plan.setTier(request.getTier());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency().toUpperCase());
        plan.setBillingCycle(request.getBillingCycle());
        plan.setFeatures(serializeFeatures(request.getFeatures()));
        plan.setActive(true);

        SubscriptionPlanEntity saved = planRepository.save(plan);
        PlanResponse response = subscriptionMapper.toPlanResponse(saved);
        cachePort.cachePlan(response);
        log.info("Subscription plan created id={} planCode={}", saved.getId(), saved.getPlanCode());
        return response;
    }

    @Override
    public PlanListResponse listPlans() {
        List<PlanResponse> plans = planRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(subscriptionMapper::toPlanResponse)
                .toList();
        return PlanListResponse.builder()
                .content(plans)
                .totalElements(plans.size())
                .build();
    }

    @Override
    public PlanResponse getPlan(UUID planId) {
        return cachePort
                .getPlan(planId)
                .orElseGet(() -> {
                    SubscriptionPlanEntity plan = findActivePlan(planId);
                    PlanResponse response = subscriptionMapper.toPlanResponse(plan);
                    cachePort.cachePlan(response);
                    return response;
                });
    }

    @Override
    public PlanResponse getPlanByCode(String planCode) {
        return cachePort
                .getPlanByCode(planCode)
                .orElseGet(() -> {
                    SubscriptionPlanEntity plan = planRepository
                            .findByPlanCodeAndActiveTrue(planCode.trim().toUpperCase())
                            .orElseThrow(() -> new ResourceNotFoundException("Plan not found for planCode " + planCode));
                    PlanResponse response = subscriptionMapper.toPlanResponse(plan);
                    cachePort.cachePlan(response);
                    return response;
                });
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(UUID planId, UpdatePlanRequest request) {
        SubscriptionPlanEntity plan = findActivePlan(planId);
        plan.setName(request.getName());
        plan.setTier(request.getTier());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency().toUpperCase());
        plan.setBillingCycle(request.getBillingCycle());
        plan.setFeatures(serializeFeatures(request.getFeatures()));
        plan.setActive(request.getActive());

        SubscriptionPlanEntity saved = planRepository.save(plan);
        cachePort.evictPlan(saved.getId(), saved.getPlanCode());
        PlanResponse response = subscriptionMapper.toPlanResponse(saved);
        cachePort.cachePlan(response);
        log.info("Subscription plan updated id={} planCode={}", saved.getId(), saved.getPlanCode());
        return response;
    }

    @Override
    @Transactional
    public void deletePlan(UUID planId) {
        SubscriptionPlanEntity plan = findActivePlan(planId);
        plan.setActive(false);
        planRepository.save(plan);
        cachePort.evictPlan(plan.getId(), plan.getPlanCode());
        log.info("Subscription plan deactivated id={} planCode={}", planId, plan.getPlanCode());
    }

    private SubscriptionPlanEntity findActivePlan(UUID planId) {
        return planRepository
                .findByIdAndActiveTrue(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
    }

    private String serializeFeatures(Map<String, Object> features) {
        if (features == null || features.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(features);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid features JSON");
        }
    }
}

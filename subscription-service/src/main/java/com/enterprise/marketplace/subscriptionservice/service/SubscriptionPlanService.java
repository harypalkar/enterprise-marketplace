package com.enterprise.marketplace.subscriptionservice.service;

import com.enterprise.marketplace.subscriptionservice.dto.CreatePlanRequest;
import com.enterprise.marketplace.subscriptionservice.dto.PlanListResponse;
import com.enterprise.marketplace.subscriptionservice.dto.PlanResponse;
import com.enterprise.marketplace.subscriptionservice.dto.UpdatePlanRequest;
import java.util.UUID;

public interface SubscriptionPlanService {

    PlanResponse createPlan(CreatePlanRequest request);

    PlanListResponse listPlans();

    PlanResponse getPlan(UUID planId);

    PlanResponse getPlanByCode(String planCode);

    PlanResponse updatePlan(UUID planId, UpdatePlanRequest request);

    void deletePlan(UUID planId);
}

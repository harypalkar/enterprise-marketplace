package com.enterprise.marketplace.subscriptionservice.redis;

import com.enterprise.marketplace.subscriptionservice.dto.PlanResponse;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "false")
public class NoOpSubscriptionCacheService implements SubscriptionCachePort {

    @Override
    public void cacheSubscription(SubscriptionResponse response) {
        // no-op when redis disabled
    }

    @Override
    public void evictSubscription(UUID subscriptionId) {
        // no-op when redis disabled
    }

    @Override
    public Optional<SubscriptionResponse> getSubscription(UUID subscriptionId) {
        return Optional.empty();
    }

    @Override
    public void cachePlan(PlanResponse response) {
        // no-op when redis disabled
    }

    @Override
    public void evictPlan(UUID planId, String planCode) {
        // no-op when redis disabled
    }

    @Override
    public Optional<PlanResponse> getPlan(UUID planId) {
        return Optional.empty();
    }

    @Override
    public Optional<PlanResponse> getPlanByCode(String planCode) {
        return Optional.empty();
    }
}

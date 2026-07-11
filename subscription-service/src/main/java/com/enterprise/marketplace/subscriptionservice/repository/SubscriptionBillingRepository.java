package com.enterprise.marketplace.subscriptionservice.repository;

import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionBillingEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionBillingRepository extends JpaRepository<SubscriptionBillingEntity, UUID> {

    List<SubscriptionBillingEntity> findBySubscriptionIdOrderByBillingDateDesc(UUID subscriptionId);
}

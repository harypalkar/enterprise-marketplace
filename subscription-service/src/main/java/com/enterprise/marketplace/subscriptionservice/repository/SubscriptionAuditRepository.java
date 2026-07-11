package com.enterprise.marketplace.subscriptionservice.repository;

import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionAuditRepository extends JpaRepository<SubscriptionAuditEntity, UUID> {}

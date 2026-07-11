package com.enterprise.marketplace.subscriptionservice.repository;

import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByIdAndActiveTrue(UUID id);

    Optional<SubscriptionEntity> findByRequestIdAndActiveTrue(String requestId);

    Page<SubscriptionEntity> findBySellerIdAndActiveTrue(UUID sellerId, Pageable pageable);

    Page<SubscriptionEntity> findByBuyerIdAndActiveTrue(UUID buyerId, Pageable pageable);

    boolean existsByRequestId(String requestId);
}

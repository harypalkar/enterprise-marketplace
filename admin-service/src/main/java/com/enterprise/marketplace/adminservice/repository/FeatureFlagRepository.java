package com.enterprise.marketplace.adminservice.repository;

import com.enterprise.marketplace.adminservice.entity.FeatureFlagEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, UUID> {

    Optional<FeatureFlagEntity> findByFlagKey(String flagKey);

    List<FeatureFlagEntity> findAllByOrderByFlagKeyAsc();

    long countByEnabledTrue();
}

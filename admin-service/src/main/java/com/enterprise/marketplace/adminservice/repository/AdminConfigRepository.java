package com.enterprise.marketplace.adminservice.repository;

import com.enterprise.marketplace.adminservice.entity.AdminConfigEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminConfigRepository extends JpaRepository<AdminConfigEntity, UUID> {

    Optional<AdminConfigEntity> findByConfigKey(String configKey);

    List<AdminConfigEntity> findByScopeOrderByConfigKeyAsc(String scope);

    List<AdminConfigEntity> findAllByOrderByConfigKeyAsc();

    long countByActiveTrue();
}

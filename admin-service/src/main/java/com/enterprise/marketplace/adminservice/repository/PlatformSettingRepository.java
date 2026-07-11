package com.enterprise.marketplace.adminservice.repository;

import com.enterprise.marketplace.adminservice.entity.PlatformSettingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSettingRepository extends JpaRepository<PlatformSettingEntity, UUID> {

    Optional<PlatformSettingEntity> findBySettingKey(String settingKey);

    List<PlatformSettingEntity> findByCategoryOrderBySettingKeyAsc(String category);

    List<PlatformSettingEntity> findAllByOrderBySettingKeyAsc();

    long countByActiveTrue();
}

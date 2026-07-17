package com.enterprise.marketplace.identityservice.infrastructure.persistence;

import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.UserProfileEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, UUID> {

    Optional<UserProfileEntity> findByUserId(UUID userId);
}

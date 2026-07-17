package com.enterprise.marketplace.identityservice.infrastructure.persistence;

import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.MobileUserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MobileUserJpaRepository extends JpaRepository<MobileUserEntity, UUID> {

    Optional<MobileUserEntity> findByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);

    boolean existsByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);
}

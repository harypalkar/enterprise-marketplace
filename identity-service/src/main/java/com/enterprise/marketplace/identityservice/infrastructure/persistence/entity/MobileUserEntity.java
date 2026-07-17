package com.enterprise.marketplace.identityservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.identityservice.domain.MobileUserStatus;
import com.enterprise.marketplace.identityservice.domain.OnboardingStep;
import com.enterprise.marketplace.identityservice.domain.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mobile_user")
@Getter
@Setter
public class MobileUserEntity extends BaseEntity {

    @Column(name = "country_code", nullable = false, length = 8)
    private String countryCode = "+91";

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 32)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_step", nullable = false, length = 32)
    private OnboardingStep onboardingStep = OnboardingStep.OTP_VERIFIED;

    @Column(name = "pin_hash", length = 255)
    private String pinHash;

    @Column(name = "pin_set_at")
    private Instant pinSetAt;

    @Column(name = "failed_pin_attempts", nullable = false)
    private int failedPinAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MobileUserStatus status = MobileUserStatus.ACTIVE;
}

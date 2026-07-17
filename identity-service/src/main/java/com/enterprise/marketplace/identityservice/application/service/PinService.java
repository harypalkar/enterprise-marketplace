package com.enterprise.marketplace.identityservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.application.dto.CreatePinRequest;
import com.enterprise.marketplace.identityservice.application.dto.PinAuthResponse;
import com.enterprise.marketplace.identityservice.application.dto.VerifyPinRequest;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.enterprise.marketplace.identityservice.domain.MobileUserStatus;
import com.enterprise.marketplace.identityservice.domain.OnboardingStep;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.MobileUserJpaRepository;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.MobileUserEntity;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AccessTokenData;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.VerificationTokenData;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PinService {

    private final AuthSessionStore authRedisStore;
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
    private final MobileUserJpaRepository mobileUserJpaRepository;
    private final MobileUserService mobileUserService;

    @Transactional
    public PinAuthResponse createPin(CreatePinRequest request) {
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "pin and confirmPin must match");
        }
        if (isSequentialPin(request.getPin())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Avoid sequential PIN patterns like 123456");
        }

        VerificationTokenData token = mobileUserService.requireVerificationToken(request.getVerificationToken());
        MobileUserEntity user = mobileUserJpaRepository
                .findByCountryCodeAndMobileNumber(token.getCountryCode(), token.getMobileNumber())
                .orElseThrow(() -> new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Complete user profile before creating PIN"));

        if (user.getUserType() == null) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Set user type before creating PIN");
        }

        user.setPinHash(passwordEncoder.encode(request.getPin()));
        user.setPinSetAt(Instant.now());
        user.setFailedPinAttempts(0);
        user.setLockedUntil(null);
        user.setStatus(MobileUserStatus.ACTIVE);
        user.setOnboardingStep(OnboardingStep.COMPLETE);
        mobileUserJpaRepository.save(user);

        token.setUserId(user.getId());
        token.setUserType(user.getUserType());
        token.setOnboardingStep(user.getOnboardingStep());
        authRedisStore.saveVerificationToken(token);

        String accessToken = authRedisStore.saveAccessToken(AccessTokenData.builder()
                .userId(user.getId())
                .userType(user.getUserType())
                .countryCode(user.getCountryCode())
                .mobileNumber(user.getMobileNumber())
                .build());

        return PinAuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .userType(user.getUserType())
                .countryCode(user.getCountryCode())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    @Transactional
    public PinAuthResponse verifyPin(VerifyPinRequest request) {
        MobileUserEntity user;
        if (request.getUserId() != null) {
            user = mobileUserJpaRepository
                    .findById(request.getUserId())
                    .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        } else if (StringUtils.hasText(request.getMobileNumber())) {
            String countryCode =
                    StringUtils.hasText(request.getCountryCode()) ? request.getCountryCode().trim() : "+91";
            if (!countryCode.startsWith("+")) {
                countryCode = "+" + countryCode;
            }
            user = mobileUserJpaRepository
                    .findByCountryCodeAndMobileNumber(countryCode, request.getMobileNumber().trim())
                    .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        } else {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "mobileNumber or userId is required");
        }

        if (user.getPinHash() == null) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "PIN not set for this user");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new MarketplaceException(ErrorCode.FORBIDDEN, "Account temporarily locked due to failed PIN attempts");
        }

        if (!passwordEncoder.matches(request.getPin(), user.getPinHash())) {
            int attempts = user.getFailedPinAttempts() + 1;
            user.setFailedPinAttempts(attempts);
            if (attempts >= authProperties.getPin().getMaxFailedAttempts()) {
                user.setLockedUntil(Instant.now().plusSeconds(authProperties.getPin().getLockDurationSeconds()));
                user.setStatus(MobileUserStatus.LOCKED);
            }
            mobileUserJpaRepository.save(user);
            throw new MarketplaceException(ErrorCode.UNAUTHORIZED, "Invalid PIN");
        }

        user.setFailedPinAttempts(0);
        user.setLockedUntil(null);
        user.setStatus(MobileUserStatus.ACTIVE);
        mobileUserJpaRepository.save(user);

        String accessToken = authRedisStore.saveAccessToken(AccessTokenData.builder()
                .userId(user.getId())
                .userType(user.getUserType())
                .countryCode(user.getCountryCode())
                .mobileNumber(user.getMobileNumber())
                .build());

        return PinAuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .userType(user.getUserType())
                .countryCode(user.getCountryCode())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    private boolean isSequentialPin(String pin) {
        boolean ascending = true;
        boolean descending = true;
        for (int i = 1; i < pin.length(); i++) {
            int prev = pin.charAt(i - 1) - '0';
            int curr = pin.charAt(i) - '0';
            if (curr != prev + 1) {
                ascending = false;
            }
            if (curr != prev - 1) {
                descending = false;
            }
        }
        return ascending || descending;
    }
}

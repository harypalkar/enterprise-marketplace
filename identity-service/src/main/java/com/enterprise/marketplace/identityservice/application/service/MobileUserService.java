package com.enterprise.marketplace.identityservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.application.dto.SetUserDetailsRequest;
import com.enterprise.marketplace.identityservice.application.dto.SetUserTypeRequest;
import com.enterprise.marketplace.identityservice.application.dto.UserDetailsResponse;
import com.enterprise.marketplace.identityservice.application.dto.UserTypeResponse;
import com.enterprise.marketplace.identityservice.domain.OnboardingStep;
import com.enterprise.marketplace.identityservice.domain.UserType;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.MobileUserJpaRepository;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.UserProfileJpaRepository;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.MobileUserEntity;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.UserProfileEntity;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.VerificationTokenData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MobileUserService {

    private final AuthSessionStore authRedisStore;
    private final MobileUserJpaRepository mobileUserJpaRepository;
    private final UserProfileJpaRepository userProfileJpaRepository;

    @Transactional
    public UserTypeResponse setUserType(SetUserTypeRequest request) {
        VerificationTokenData token = requireVerificationToken(request.getVerificationToken());

        MobileUserEntity user = findOrCreateUser(token);
        user.setUserType(request.getUserType());
        if (user.getOnboardingStep() == OnboardingStep.OTP_VERIFIED
                || user.getOnboardingStep() == null) {
            user.setOnboardingStep(OnboardingStep.TYPE_SET);
        }
        mobileUserJpaRepository.save(user);

        token.setUserId(user.getId());
        token.setUserType(user.getUserType());
        token.setOnboardingStep(user.getOnboardingStep());
        token.setNewUser(false);
        authRedisStore.saveVerificationToken(token);

        return UserTypeResponse.builder().userId(user.getId()).userType(user.getUserType()).build();
    }

    @Transactional
    public UserDetailsResponse setUserDetails(SetUserDetailsRequest request) {
        VerificationTokenData token = requireVerificationToken(request.getVerificationToken());
        MobileUserEntity user = findOrCreateUser(token);

        if (user.getUserType() == null) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Set user type before profile details");
        }

        validateDetailsForType(user.getUserType(), request);

        UserProfileEntity profile = userProfileJpaRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfileEntity created = new UserProfileEntity();
                    created.setUserId(user.getId());
                    return created;
                });

        if (user.getUserType() == UserType.INDIVIDUAL) {
            profile.setFullName(request.getFullName());
            profile.setLegalName(StringUtils.hasText(request.getLegalName()) ? request.getLegalName() : request.getFullName());
            profile.setEmail(request.getEmail());
            profile.setCompanyName(null);
            profile.setWebsite(null);
            profile.setGstNumber(null);
            profile.setCity(request.getCity());
            profile.setCountry(request.getCountry());
        } else {
            profile.setCompanyName(request.getCompanyName());
            profile.setWebsite(request.getWebsite());
            profile.setGstNumber(request.getGstNumber());
            profile.setCity(request.getCity());
            profile.setCountry(request.getCountry());
            profile.setEmail(request.getEmail());
            profile.setLegalName(request.getLegalName());
            profile.setFullName(StringUtils.hasText(request.getFullName()) ? request.getFullName() : request.getLegalName());
        }

        userProfileJpaRepository.save(profile);
        user.setOnboardingStep(OnboardingStep.DETAILS_SET);
        mobileUserJpaRepository.save(user);

        token.setUserId(user.getId());
        token.setUserType(user.getUserType());
        token.setOnboardingStep(user.getOnboardingStep());
        authRedisStore.saveVerificationToken(token);

        return UserDetailsResponse.builder()
                .userId(user.getId())
                .userType(user.getUserType())
                .fullName(profile.getFullName())
                .legalName(profile.getLegalName())
                .email(profile.getEmail())
                .companyName(profile.getCompanyName())
                .website(profile.getWebsite())
                .gstNumber(profile.getGstNumber())
                .city(profile.getCity())
                .country(profile.getCountry())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    private void validateDetailsForType(UserType userType, SetUserDetailsRequest request) {
        if (userType == UserType.INDIVIDUAL) {
            if (!StringUtils.hasText(request.getFullName())) {
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "fullName is required for individual users");
            }
            if (!StringUtils.hasText(request.getEmail())) {
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "email is required for individual users");
            }
        } else {
            if (!StringUtils.hasText(request.getCompanyName())) {
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "companyName is required for business accounts");
            }
            if (!StringUtils.hasText(request.getGstNumber())) {
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "gstNumber is required for business accounts");
            }
            if (!StringUtils.hasText(request.getEmail())) {
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "email is required for business accounts");
            }
        }
    }

    private MobileUserEntity findOrCreateUser(VerificationTokenData token) {
        if (token.getUserId() != null) {
            return mobileUserJpaRepository
                    .findById(token.getUserId())
                    .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        }
        return mobileUserJpaRepository
                .findByCountryCodeAndMobileNumber(token.getCountryCode(), token.getMobileNumber())
                .orElseGet(() -> {
                    MobileUserEntity created = new MobileUserEntity();
                    created.setCountryCode(token.getCountryCode());
                    created.setMobileNumber(token.getMobileNumber());
                    created.setOnboardingStep(OnboardingStep.OTP_VERIFIED);
                    return mobileUserJpaRepository.save(created);
                });
    }

    VerificationTokenData requireVerificationToken(String verificationToken) {
        return authRedisStore
                .getVerificationToken(verificationToken)
                .orElseThrow(() -> new MarketplaceException(ErrorCode.UNAUTHORIZED, "Invalid or expired verification token"));
    }
}

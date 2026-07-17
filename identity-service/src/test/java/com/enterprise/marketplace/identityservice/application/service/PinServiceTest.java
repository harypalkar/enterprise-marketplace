package com.enterprise.marketplace.identityservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.application.dto.CreatePinRequest;
import com.enterprise.marketplace.identityservice.application.dto.PinAuthResponse;
import com.enterprise.marketplace.identityservice.application.dto.VerifyPinRequest;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.enterprise.marketplace.identityservice.domain.OnboardingStep;
import com.enterprise.marketplace.identityservice.domain.UserType;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.MobileUserJpaRepository;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.MobileUserEntity;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AccessTokenData;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.VerificationTokenData;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PinServiceTest {

    @Mock
    private AuthSessionStore authRedisStore;

    @Mock
    private MobileUserJpaRepository mobileUserJpaRepository;

    @Mock
    private MobileUserService mobileUserService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthProperties authProperties = new AuthProperties();

    @InjectMocks
    private PinService pinService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pinService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(pinService, "authProperties", authProperties);
    }

    @Test
    void createPinHashesAndReturnsAccessToken() {
        UUID userId = UUID.randomUUID();
        VerificationTokenData token = VerificationTokenData.builder()
                .token("vt")
                .countryCode("+91")
                .mobileNumber("9876543210")
                .userId(userId)
                .userType(UserType.INDIVIDUAL)
                .build();
        MobileUserEntity user = new MobileUserEntity();
        user.setId(userId);
        user.setCountryCode("+91");
        user.setMobileNumber("9876543210");
        user.setUserType(UserType.INDIVIDUAL);
        user.setOnboardingStep(OnboardingStep.DETAILS_SET);

        when(mobileUserService.requireVerificationToken("vt")).thenReturn(token);
        when(mobileUserJpaRepository.findByCountryCodeAndMobileNumber("+91", "9876543210"))
                .thenReturn(Optional.of(user));
        when(authRedisStore.saveAccessToken(any(AccessTokenData.class))).thenReturn("access-1");

        CreatePinRequest request = new CreatePinRequest();
        request.setVerificationToken("vt");
        request.setPin("258147");
        request.setConfirmPin("258147");

        PinAuthResponse response = pinService.createPin(request);

        assertThat(response.getAccessToken()).isEqualTo("access-1");
        assertThat(user.getPinHash()).isNotBlank();
        assertThat(passwordEncoder.matches("258147", user.getPinHash())).isTrue();
        assertThat(user.getOnboardingStep()).isEqualTo(OnboardingStep.COMPLETE);
        verify(mobileUserJpaRepository).save(user);
    }

    @Test
    void createPinRejectsMismatch() {
        CreatePinRequest request = new CreatePinRequest();
        request.setVerificationToken("vt");
        request.setPin("258147");
        request.setConfirmPin("111111");
        assertThatThrownBy(() -> pinService.createPin(request)).isInstanceOf(MarketplaceException.class);
    }

    @Test
    void verifyPinSucceedsForMatchingHash() {
        UUID userId = UUID.randomUUID();
        MobileUserEntity user = new MobileUserEntity();
        user.setId(userId);
        user.setCountryCode("+91");
        user.setMobileNumber("9876543210");
        user.setUserType(UserType.BUSINESS);
        user.setPinHash(passwordEncoder.encode("258147"));
        user.setFailedPinAttempts(2);

        when(mobileUserJpaRepository.findByCountryCodeAndMobileNumber("+91", "9876543210"))
                .thenReturn(Optional.of(user));
        when(authRedisStore.saveAccessToken(any(AccessTokenData.class))).thenReturn("access-2");

        VerifyPinRequest request = new VerifyPinRequest();
        request.setMobileNumber("9876543210");
        request.setPin("258147");

        PinAuthResponse response = pinService.verifyPin(request);

        assertThat(response.getAccessToken()).isEqualTo("access-2");
        assertThat(user.getFailedPinAttempts()).isZero();
    }
}

package com.enterprise.marketplace.identityservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.application.dto.OtpResponse;
import com.enterprise.marketplace.identityservice.application.dto.ResendOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.SendOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.VerifyOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.VerifyOtpResponse;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.enterprise.marketplace.identityservice.infrastructure.client.NotificationClient;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.MobileUserJpaRepository;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.OtpSessionData;
import com.enterprise.marketplace.identityservice.infrastructure.redis.VerificationTokenData;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private AuthSessionStore authRedisStore;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private MobileUserJpaRepository mobileUserJpaRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthProperties authProperties = new AuthProperties();

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        authProperties.getOtp().setDevExposeOtp(true);
        authProperties.getOtp().setResendCooldownSeconds(0);
        ReflectionTestUtils.setField(otpService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(otpService, "authProperties", authProperties);
    }

    @Test
    void sendOtpStoresSessionAndExposesOtpInDev() {
        SendOtpRequest request = new SendOtpRequest();
        request.setCountryCode("+91");
        request.setMobileNumber("9876543210");

        OtpResponse response = otpService.sendOtp(request);

        assertThat(response.getSessionId()).isNotBlank();
        assertThat(response.getOtp()).hasSize(6);
        assertThat(response.getExpiresInSeconds()).isEqualTo(300);
        verify(authRedisStore).saveOtpSession(any(OtpSessionData.class));
        verify(notificationClient).sendOtpSms("+91", "9876543210", response.getOtp());
    }

    @Test
    void verifyOtpIssuesVerificationTokenForNewUser() {
        String otp = "482913";
        OtpSessionData session = OtpSessionData.builder()
                .sessionId("sess-1")
                .countryCode("+91")
                .mobileNumber("9876543210")
                .otpHash(passwordEncoder.encode(otp))
                .attempts(0)
                .createdAtEpochMs(System.currentTimeMillis())
                .lastSentAtEpochMs(System.currentTimeMillis())
                .build();
        when(authRedisStore.getOtpSession("sess-1")).thenReturn(Optional.of(session));
        when(mobileUserJpaRepository.findByCountryCodeAndMobileNumber("+91", "9876543210"))
                .thenReturn(Optional.empty());
        when(authRedisStore.saveVerificationToken(any(VerificationTokenData.class))).thenReturn("verify-token");

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setSessionId("sess-1");
        request.setOtp(otp);

        VerifyOtpResponse response = otpService.verifyOtp(request);

        assertThat(response.isNewUser()).isTrue();
        assertThat(response.getVerificationToken()).isEqualTo("verify-token");
        verify(authRedisStore).deleteOtpSession("sess-1");
    }

    @Test
    void verifyOtpRejectsInvalidCode() {
        OtpSessionData session = OtpSessionData.builder()
                .sessionId("sess-1")
                .countryCode("+91")
                .mobileNumber("9876543210")
                .otpHash(passwordEncoder.encode("111111"))
                .attempts(0)
                .createdAtEpochMs(System.currentTimeMillis())
                .lastSentAtEpochMs(System.currentTimeMillis())
                .build();
        when(authRedisStore.getOtpSession("sess-1")).thenReturn(Optional.of(session));

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setSessionId("sess-1");
        request.setOtp("000000");

        assertThatThrownBy(() -> otpService.verifyOtp(request)).isInstanceOf(MarketplaceException.class);
        ArgumentCaptor<OtpSessionData> captor = ArgumentCaptor.forClass(OtpSessionData.class);
        verify(authRedisStore).saveOtpSession(captor.capture());
        assertThat(captor.getValue().getAttempts()).isEqualTo(1);
        verify(authRedisStore, never()).deleteOtpSession(anyString());
    }

    @Test
    void resendOtpRequiresExistingSession() {
        when(authRedisStore.getOtpSession("missing")).thenReturn(Optional.empty());
        ResendOtpRequest request = new ResendOtpRequest();
        request.setSessionId("missing");
        assertThatThrownBy(() -> otpService.resendOtp(request)).isInstanceOf(MarketplaceException.class);
    }
}

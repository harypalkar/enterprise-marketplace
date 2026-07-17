package com.enterprise.marketplace.identityservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.application.dto.OtpResponse;
import com.enterprise.marketplace.identityservice.application.dto.ResendOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.SendOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.VerifyOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.VerifyOtpResponse;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.enterprise.marketplace.identityservice.domain.OnboardingStep;
import com.enterprise.marketplace.identityservice.infrastructure.client.NotificationClient;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.MobileUserJpaRepository;
import com.enterprise.marketplace.identityservice.infrastructure.persistence.entity.MobileUserEntity;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.OtpSessionData;
import com.enterprise.marketplace.identityservice.infrastructure.redis.VerificationTokenData;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final AuthSessionStore authRedisStore;
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    private final MobileUserJpaRepository mobileUserJpaRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpResponse sendOtp(SendOtpRequest request) {
        String countryCode = normalizeCountryCode(request.getCountryCode());
        String mobileNumber = request.getMobileNumber().trim();
        return issueOtp(countryCode, mobileNumber, UUID.randomUUID().toString());
    }

    public OtpResponse resendOtp(ResendOtpRequest request) {
        OtpSessionData existing = authRedisStore
                .getOtpSession(request.getSessionId())
                .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "OTP session not found or expired"));

        long now = System.currentTimeMillis();
        long cooldownMs = authProperties.getOtp().getResendCooldownSeconds() * 1000L;
        if (now - existing.getLastSentAtEpochMs() < cooldownMs) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Please wait before requesting another OTP");
        }

        return issueOtp(existing.getCountryCode(), existing.getMobileNumber(), existing.getSessionId());
    }

    @Transactional(readOnly = true)
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpSessionData session = authRedisStore
                .getOtpSession(request.getSessionId())
                .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "OTP session not found or expired"));

        if (session.getAttempts() >= authProperties.getOtp().getMaxAttempts()) {
            authRedisStore.deleteOtpSession(session.getSessionId());
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(request.getOtp(), session.getOtpHash())) {
            session.setAttempts(session.getAttempts() + 1);
            authRedisStore.saveOtpSession(session);
            throw new MarketplaceException(ErrorCode.UNAUTHORIZED, "Invalid OTP");
        }

        authRedisStore.deleteOtpSession(session.getSessionId());

        Optional<MobileUserEntity> existing =
                mobileUserJpaRepository.findByCountryCodeAndMobileNumber(session.getCountryCode(), session.getMobileNumber());

        VerificationTokenData tokenData = VerificationTokenData.builder()
                .sessionId(session.getSessionId())
                .countryCode(session.getCountryCode())
                .mobileNumber(session.getMobileNumber())
                .userId(existing.map(MobileUserEntity::getId).orElse(null))
                .userType(existing.map(MobileUserEntity::getUserType).orElse(null))
                .onboardingStep(existing.map(MobileUserEntity::getOnboardingStep).orElse(OnboardingStep.OTP_VERIFIED))
                .newUser(existing.isEmpty())
                .build();

        String verificationToken = authRedisStore.saveVerificationToken(tokenData);

        return VerifyOtpResponse.builder()
                .verificationToken(verificationToken)
                .isNewUser(existing.isEmpty())
                .userId(existing.map(MobileUserEntity::getId).orElse(null))
                .countryCode(session.getCountryCode())
                .mobileNumber(session.getMobileNumber())
                .build();
    }

    private OtpResponse issueOtp(String countryCode, String mobileNumber, String sessionId) {
        String otp = generateOtp(authProperties.getOtp().getLength());
        long now = System.currentTimeMillis();

        OtpSessionData data = OtpSessionData.builder()
                .sessionId(sessionId)
                .countryCode(countryCode)
                .mobileNumber(mobileNumber)
                .otpHash(passwordEncoder.encode(otp))
                .attempts(0)
                .createdAtEpochMs(now)
                .lastSentAtEpochMs(now)
                .build();
        authRedisStore.saveOtpSession(data);
        notificationClient.sendOtpSms(countryCode, mobileNumber, otp);

        boolean smsEnabled = authProperties.getOtp().isSmsEnabled();
        String deliveryNote = smsEnabled
                ? "OTP submitted to SMS provider"
                : "SMS is disabled in local/dev. Use the otp field from this response to verify.";

        return OtpResponse.builder()
                .sessionId(sessionId)
                .expiresInSeconds(authProperties.getOtp().getTtlSeconds())
                .otp(authProperties.getOtp().isDevExposeOtp() ? otp : null)
                .countryCode(countryCode)
                .mobileNumber(mobileNumber)
                .smsDelivered(smsEnabled)
                .deliveryNote(deliveryNote)
                .build();
    }

    private String generateOtp(int length) {
        int bound = (int) Math.pow(10, length);
        int min = bound / 10;
        int value = min + secureRandom.nextInt(bound - min);
        return String.valueOf(value);
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "+91";
        }
        String normalized = countryCode.trim();
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }
        return normalized;
    }
}

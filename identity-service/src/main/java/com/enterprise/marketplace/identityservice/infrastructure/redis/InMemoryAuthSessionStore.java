package com.enterprise.marketplace.identityservice.infrastructure.redis;

import com.enterprise.marketplace.identityservice.config.AuthProperties;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * In-memory OTP/token/QR store used when Redis is unavailable (standalone local profile).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "marketplace.auth", name = "session-store", havingValue = "memory")
@RequiredArgsConstructor
public class InMemoryAuthSessionStore implements AuthSessionStore {

    private final AuthProperties authProperties;
    private final Map<String, TimedValue<OtpSessionData>> otpSessions = new ConcurrentHashMap<>();
    private final Map<String, TimedValue<VerificationTokenData>> verificationTokens = new ConcurrentHashMap<>();
    private final Map<String, TimedValue<AccessTokenData>> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, TimedValue<QrSessionData>> qrSessions = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        log.warn("Using in-memory auth session store (Redis not available)");
    }

    @Override
    public void saveOtpSession(OtpSessionData data) {
        put(otpSessions, data.getSessionId(), data, authProperties.getOtp().getTtlSeconds());
    }

    @Override
    public Optional<OtpSessionData> getOtpSession(String sessionId) {
        return get(otpSessions, sessionId);
    }

    @Override
    public void deleteOtpSession(String sessionId) {
        otpSessions.remove(sessionId);
    }

    @Override
    public String saveVerificationToken(VerificationTokenData data) {
        if (data.getToken() == null) {
            data.setToken(UUID.randomUUID().toString());
        }
        put(verificationTokens, data.getToken(), data, authProperties.getToken().getVerificationTtlSeconds());
        return data.getToken();
    }

    @Override
    public Optional<VerificationTokenData> getVerificationToken(String token) {
        return get(verificationTokens, token);
    }

    @Override
    public String saveAccessToken(AccessTokenData data) {
        if (data.getToken() == null) {
            data.setToken(UUID.randomUUID().toString());
        }
        put(accessTokens, data.getToken(), data, authProperties.getToken().getAccessTtlSeconds());
        return data.getToken();
    }

    @Override
    public Optional<AccessTokenData> getAccessToken(String token) {
        return get(accessTokens, token);
    }

    @Override
    public void saveQrSession(QrSessionData data) {
        put(qrSessions, data.getQrSessionId(), data, authProperties.getQr().getTtlSeconds());
    }

    @Override
    public Optional<QrSessionData> getQrSession(String qrSessionId) {
        return get(qrSessions, qrSessionId);
    }

    private <T> void put(Map<String, TimedValue<T>> map, String key, T value, long ttlSeconds) {
        map.put(key, new TimedValue<>(value, System.currentTimeMillis() + ttlSeconds * 1000L));
    }

    private <T> Optional<T> get(Map<String, TimedValue<T>> map, String key) {
        TimedValue<T> timed = map.get(key);
        if (timed == null) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() > timed.expiresAtEpochMs()) {
            map.remove(key);
            return Optional.empty();
        }
        return Optional.of(timed.value());
    }

    private record TimedValue<T>(T value, long expiresAtEpochMs) {}
}

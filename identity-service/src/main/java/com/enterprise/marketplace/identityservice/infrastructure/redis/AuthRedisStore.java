package com.enterprise.marketplace.identityservice.infrastructure.redis;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "marketplace.auth", name = "session-store", havingValue = "redis", matchIfMissing = true)
@ConditionalOnBean(RedisConnectionFactory.class)
@RequiredArgsConstructor
public class AuthRedisStore implements AuthSessionStore {

    private static final String OTP_KEY = "identity:otp:";
    private static final String VERIFY_KEY = "identity:verify:";
    private static final String ACCESS_KEY = "identity:access:";
    private static final String QR_KEY = "identity:qr:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    @Override
    public void saveOtpSession(OtpSessionData data) {
        write(OTP_KEY + data.getSessionId(), data, authProperties.getOtp().getTtlSeconds());
    }

    @Override
    public Optional<OtpSessionData> getOtpSession(String sessionId) {
        return read(OTP_KEY + sessionId, OtpSessionData.class);
    }

    @Override
    public void deleteOtpSession(String sessionId) {
        redisTemplate.delete(OTP_KEY + sessionId);
    }

    @Override
    public String saveVerificationToken(VerificationTokenData data) {
        if (data.getToken() == null) {
            data.setToken(UUID.randomUUID().toString());
        }
        write(VERIFY_KEY + data.getToken(), data, authProperties.getToken().getVerificationTtlSeconds());
        return data.getToken();
    }

    @Override
    public Optional<VerificationTokenData> getVerificationToken(String token) {
        return read(VERIFY_KEY + token, VerificationTokenData.class);
    }

    @Override
    public String saveAccessToken(AccessTokenData data) {
        if (data.getToken() == null) {
            data.setToken(UUID.randomUUID().toString());
        }
        write(ACCESS_KEY + data.getToken(), data, authProperties.getToken().getAccessTtlSeconds());
        return data.getToken();
    }

    @Override
    public Optional<AccessTokenData> getAccessToken(String token) {
        return read(ACCESS_KEY + token, AccessTokenData.class);
    }

    @Override
    public void saveQrSession(QrSessionData data) {
        write(QR_KEY + data.getQrSessionId(), data, authProperties.getQr().getTtlSeconds());
    }

    @Override
    public Optional<QrSessionData> getQrSession(String qrSessionId) {
        return read(QR_KEY + qrSessionId, QrSessionData.class);
    }

    private void write(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to serialize auth session", ex);
        }
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to deserialize auth session", ex);
        }
    }
}

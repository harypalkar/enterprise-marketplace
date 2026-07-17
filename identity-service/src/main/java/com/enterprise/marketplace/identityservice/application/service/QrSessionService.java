package com.enterprise.marketplace.identityservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.identityservice.application.dto.ConfirmQrRequest;
import com.enterprise.marketplace.identityservice.application.dto.CreateQrRequest;
import com.enterprise.marketplace.identityservice.application.dto.QrCreateResponse;
import com.enterprise.marketplace.identityservice.application.dto.QrStatusResponse;
import com.enterprise.marketplace.identityservice.config.AuthProperties;
import com.enterprise.marketplace.identityservice.domain.QrSessionStatus;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AccessTokenData;
import com.enterprise.marketplace.identityservice.infrastructure.redis.AuthSessionStore;
import com.enterprise.marketplace.identityservice.infrastructure.redis.QrSessionData;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QrSessionService {

    private final AuthSessionStore authRedisStore;
    private final AuthProperties authProperties;

    public QrCreateResponse createQrSession(CreateQrRequest request) {
        String qrSessionId = UUID.randomUUID().toString();
        QrSessionData data = QrSessionData.builder()
                .qrSessionId(qrSessionId)
                .deviceId(request != null ? request.getDeviceId() : null)
                .status(QrSessionStatus.PENDING.name())
                .createdAtEpochMs(System.currentTimeMillis())
                .build();
        authRedisStore.saveQrSession(data);

        return QrCreateResponse.builder()
                .qrSessionId(qrSessionId)
                .qrPayload(authProperties.getQr().getPayloadPrefix() + qrSessionId)
                .expiresInSeconds(authProperties.getQr().getTtlSeconds())
                .status(QrSessionStatus.PENDING.name())
                .build();
    }

    public QrStatusResponse getQrSession(String qrSessionId) {
        QrSessionData data = authRedisStore
                .getQrSession(qrSessionId)
                .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "QR session not found or expired"));

        return QrStatusResponse.builder()
                .qrSessionId(data.getQrSessionId())
                .status(data.getStatus())
                .accessToken(data.getAccessToken())
                .userId(data.getUserId())
                .userType(data.getUserType())
                .build();
    }

    public QrStatusResponse confirmQrSession(String qrSessionId, ConfirmQrRequest request) {
        QrSessionData data = authRedisStore
                .getQrSession(qrSessionId)
                .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "QR session not found or expired"));

        if (QrSessionStatus.CONFIRMED.name().equals(data.getStatus())) {
            return getQrSession(qrSessionId);
        }

        AccessTokenData access = authRedisStore
                .getAccessToken(request.getAccessToken())
                .orElseThrow(() -> new MarketplaceException(ErrorCode.UNAUTHORIZED, "Invalid or expired access token"));

        data.setStatus(QrSessionStatus.CONFIRMED.name());
        data.setAccessToken(access.getToken());
        data.setUserId(access.getUserId());
        data.setUserType(access.getUserType());
        authRedisStore.saveQrSession(data);

        return QrStatusResponse.builder()
                .qrSessionId(data.getQrSessionId())
                .status(data.getStatus())
                .accessToken(data.getAccessToken())
                .userId(data.getUserId())
                .userType(data.getUserType())
                .build();
    }
}

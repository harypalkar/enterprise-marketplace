package com.enterprise.marketplace.identityservice.infrastructure.redis;

import java.util.Optional;

public interface AuthSessionStore {

    void saveOtpSession(OtpSessionData data);

    Optional<OtpSessionData> getOtpSession(String sessionId);

    void deleteOtpSession(String sessionId);

    String saveVerificationToken(VerificationTokenData data);

    Optional<VerificationTokenData> getVerificationToken(String token);

    String saveAccessToken(AccessTokenData data);

    Optional<AccessTokenData> getAccessToken(String token);

    void saveQrSession(QrSessionData data);

    Optional<QrSessionData> getQrSession(String qrSessionId);
}

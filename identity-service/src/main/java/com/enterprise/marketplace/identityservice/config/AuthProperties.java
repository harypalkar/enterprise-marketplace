package com.enterprise.marketplace.identityservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "marketplace.auth")
public class AuthProperties {

    private Otp otp = new Otp();
    private Pin pin = new Pin();
    private Qr qr = new Qr();
    private Token token = new Token();

    @Data
    public static class Otp {
        private int length = 6;
        private long ttlSeconds = 300;
        private int maxAttempts = 5;
        private long resendCooldownSeconds = 30;
        private boolean devExposeOtp = true;
        private boolean smsEnabled = false;
    }

    @Data
    public static class Pin {
        private int length = 6;
        private int maxFailedAttempts = 5;
        private long lockDurationSeconds = 900;
    }

    @Data
    public static class Qr {
        private long ttlSeconds = 300;
        private String payloadPrefix = "karatkart://qr-login?sessionId=";
    }

    @Data
    public static class Token {
        private long verificationTtlSeconds = 1800;
        private long accessTtlSeconds = 86400;
    }
}

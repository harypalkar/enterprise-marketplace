package com.enterprise.marketplace.identityservice.infrastructure.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSessionData {
    private String sessionId;
    private String countryCode;
    private String mobileNumber;
    private String otpHash;
    private int attempts;
    private long createdAtEpochMs;
    private long lastSentAtEpochMs;
}

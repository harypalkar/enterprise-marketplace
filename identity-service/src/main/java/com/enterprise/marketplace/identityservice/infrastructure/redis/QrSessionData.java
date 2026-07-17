package com.enterprise.marketplace.identityservice.infrastructure.redis;

import com.enterprise.marketplace.identityservice.domain.UserType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrSessionData {
    private String qrSessionId;
    private String deviceId;
    private String status;
    private UUID userId;
    private UserType userType;
    private String accessToken;
    private long createdAtEpochMs;
}

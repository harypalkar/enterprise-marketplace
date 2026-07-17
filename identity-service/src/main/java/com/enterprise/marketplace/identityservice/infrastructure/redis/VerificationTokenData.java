package com.enterprise.marketplace.identityservice.infrastructure.redis;

import com.enterprise.marketplace.identityservice.domain.OnboardingStep;
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
public class VerificationTokenData {
    private String token;
    private String sessionId;
    private String countryCode;
    private String mobileNumber;
    private UUID userId;
    private UserType userType;
    private OnboardingStep onboardingStep;
    private boolean newUser;
}

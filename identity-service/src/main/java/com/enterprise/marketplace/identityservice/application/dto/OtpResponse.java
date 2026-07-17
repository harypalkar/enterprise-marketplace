package com.enterprise.marketplace.identityservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponse {
    String sessionId;
    long expiresInSeconds;
    String otp;
    String countryCode;
    String mobileNumber;
    boolean smsDelivered;
    String deliveryNote;
}

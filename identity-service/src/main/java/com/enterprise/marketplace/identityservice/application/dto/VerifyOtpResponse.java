package com.enterprise.marketplace.identityservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyOtpResponse {
    String verificationToken;

    @JsonProperty("isNewUser")
    boolean isNewUser;

    UUID userId;
    String countryCode;
    String mobileNumber;
}

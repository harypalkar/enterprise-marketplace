package com.enterprise.marketplace.identityservice.application.dto;

import com.enterprise.marketplace.identityservice.domain.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTypeResponse {
    UUID userId;
    UserType userType;
}

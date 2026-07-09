package com.enterprise.marketplace.sellerservice.application.dto;

import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SellerResponse {

    UUID id;
    String companyName;
    String tradeName;
    String gstin;
    String pan;
    String email;
    String phone;
    String city;
    String state;
    String country;
    String pinCode;
    SellerStatus status;
    Instant createdAt;
    Instant updatedAt;
    Long version;
}

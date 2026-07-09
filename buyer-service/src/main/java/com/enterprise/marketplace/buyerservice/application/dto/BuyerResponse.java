package com.enterprise.marketplace.buyerservice.application.dto;

import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BuyerResponse {

    UUID id;
    String companyName;
    String contactPerson;
    String email;
    String phone;
    String city;
    String state;
    String country;
    String pinCode;
    BuyerStatus status;
    Instant createdAt;
    Instant updatedAt;
    Long version;
}

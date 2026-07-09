package com.enterprise.marketplace.buyerservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Buyer aggregate root (domain model - no framework dependencies).
 */
@Value
@Builder(toBuilder = true)
@With
public class Buyer {

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
    String createdBy;
    String updatedBy;
    Long version;
}

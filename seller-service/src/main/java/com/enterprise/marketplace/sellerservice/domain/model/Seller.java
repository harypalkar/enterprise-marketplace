package com.enterprise.marketplace.sellerservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Seller aggregate root (domain model — no framework dependencies).
 */
@Value
@Builder(toBuilder = true)
@With
public class Seller {

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
    String createdBy;
    String updatedBy;
    Long version;
}

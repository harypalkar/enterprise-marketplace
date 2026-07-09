package com.enterprise.marketplace.buyerservice.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BuyerSearchCriteria {

    BuyerStatus status;
    String city;
    String state;
    String country;
    String keyword;
}

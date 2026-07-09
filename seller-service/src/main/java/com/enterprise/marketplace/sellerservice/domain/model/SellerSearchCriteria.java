package com.enterprise.marketplace.sellerservice.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SellerSearchCriteria {

    SellerStatus status;
    String keyword;
}

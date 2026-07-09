package com.enterprise.marketplace.pricingservice.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PricingPageResponse {

    List<PricingResponse> content;
    long totalElements;
    int totalPages;
    int page;
    int size;
}

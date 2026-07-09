package com.enterprise.marketplace.sellerservice.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SellerPageResponse {

    List<SellerResponse> content;
    long totalElements;
    int totalPages;
    int page;
    int size;
}

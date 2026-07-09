package com.enterprise.marketplace.buyerservice.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BuyerPageResponse {

    List<BuyerResponse> content;
    long totalElements;
    int totalPages;
    int page;
    int size;
}

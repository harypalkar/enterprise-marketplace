package com.enterprise.marketplace.subscriptionservice.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubscriptionPageResponse {

    List<SubscriptionResponse> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
}

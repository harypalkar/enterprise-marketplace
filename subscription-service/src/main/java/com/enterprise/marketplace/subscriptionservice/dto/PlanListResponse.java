package com.enterprise.marketplace.subscriptionservice.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlanListResponse {

    List<PlanResponse> content;
    int totalElements;
}

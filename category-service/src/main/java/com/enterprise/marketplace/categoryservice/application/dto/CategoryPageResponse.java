package com.enterprise.marketplace.categoryservice.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CategoryPageResponse {

    List<CategoryResponse> content;
    long totalElements;
    int totalPages;
    int page;
    int size;
}

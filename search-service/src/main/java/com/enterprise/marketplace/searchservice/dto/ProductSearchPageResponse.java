package com.enterprise.marketplace.searchservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchPageResponse {

    private List<ProductSearchResult> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String query;
}

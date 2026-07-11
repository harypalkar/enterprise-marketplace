package com.enterprise.marketplace.searchservice.service;

import com.enterprise.marketplace.searchservice.dto.ProductSearchPageResponse;
import com.enterprise.marketplace.searchservice.dto.ProductSearchRequest;
import com.enterprise.marketplace.searchservice.dto.ProductSearchResult;
import java.util.UUID;

public interface SearchService {

    ProductSearchPageResponse searchProducts(ProductSearchRequest request);

    ProductSearchResult getProductById(UUID productId);

    void processIndexEvent(String payload, String eventSource);

    ProductSearchResult reindexProduct(UUID productId, String payload);
}

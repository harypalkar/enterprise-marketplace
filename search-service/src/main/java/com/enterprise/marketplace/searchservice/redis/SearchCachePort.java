package com.enterprise.marketplace.searchservice.redis;

import com.enterprise.marketplace.searchservice.dto.ProductSearchPageResponse;
import java.util.Optional;

public interface SearchCachePort {

    void cacheSearchResult(String cacheKey, ProductSearchPageResponse response);

    Optional<ProductSearchPageResponse> getSearchResult(String cacheKey);
}

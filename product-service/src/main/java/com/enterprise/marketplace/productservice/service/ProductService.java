package com.enterprise.marketplace.productservice.service;

import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductPatchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.ProductDetailResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductPageResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSearchRequest;
import java.util.UUID;

public interface ProductService {

    ProductDetailResponse createProduct(CanonicalProductRequest request);

    ProductDetailResponse getProduct(UUID id);

    ProductPageResponse listProducts(int page, int size, String sort);

    ProductPageResponse searchProducts(ProductSearchRequest request);

    ProductDetailResponse updateProduct(UUID id, CanonicalProductRequest request);

    ProductDetailResponse patchProduct(UUID id, CanonicalProductPatchRequest request);

    void deleteProduct(UUID id);
}

package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductPatchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.PricingSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.SellerSectionDto;

final class ReferenceValidationSupport {

    private ReferenceValidationSupport() {}

    static SellerSectionDto extractSeller(ProductValidationContext context) {
        if (context.request() instanceof CanonicalProductRequest request) {
            return request.getSeller();
        }
        if (context.request() instanceof CanonicalProductPatchRequest request) {
            return request.getSeller();
        }
        return null;
    }

    static ProductSectionDto extractProduct(ProductValidationContext context) {
        if (context.request() instanceof CanonicalProductRequest request) {
            return request.getProduct();
        }
        if (context.request() instanceof CanonicalProductPatchRequest request) {
            return request.getProduct();
        }
        return null;
    }

    static PricingSectionDto extractPricing(ProductValidationContext context) {
        if (context.request() instanceof CanonicalProductRequest request) {
            return request.getPricing();
        }
        if (context.request() instanceof CanonicalProductPatchRequest request) {
            return request.getPricing();
        }
        return null;
    }
}

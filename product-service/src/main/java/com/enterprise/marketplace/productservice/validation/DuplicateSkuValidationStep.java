package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductPatchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DuplicateSkuValidationStep implements ValidationStep {

    private final ProductRepository productRepository;

    @Override
    public void validate(ProductValidationContext context) {
        if (context.operation() == ValidationOperation.DELETE) {
            return;
        }

        ProductSectionDto product = extractProduct(context);
        if (product == null || !StringUtils.hasText(product.getSku())) {
            return;
        }

        String sku = product.getSku().trim();
        boolean exists = productRepository.existsBySku(sku);
        if (!exists) {
            return;
        }

        if (context.operation() == ValidationOperation.CREATE) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Product SKU already exists: " + sku);
        }

        if (context.productId() != null) {
            productRepository.findBySku(sku).ifPresent(existing -> {
                if (!existing.getId().equals(context.productId())) {
                    throw new MarketplaceException(ErrorCode.CONFLICT, "Product SKU already exists: " + sku);
                }
            });
        }
    }

    private ProductSectionDto extractProduct(ProductValidationContext context) {
        if (context.request() instanceof CanonicalProductRequest request) {
            return request.getProduct();
        }
        if (context.request() instanceof CanonicalProductPatchRequest request) {
            return request.getProduct();
        }
        return null;
    }
}

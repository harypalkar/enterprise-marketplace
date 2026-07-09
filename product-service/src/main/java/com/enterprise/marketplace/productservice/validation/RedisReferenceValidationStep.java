package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.productservice.dto.canonical.PricingSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.SellerSectionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "marketplace.redis.enabled", havingValue = "true")
public class RedisReferenceValidationStep implements ReferenceValidationStep {

    private final ReferenceDataValidator referenceDataValidator;

    @Override
    public void validate(ProductValidationContext context) {
        if (context.operation() == ValidationOperation.DELETE) {
            return;
        }
        SellerSectionDto seller = ReferenceValidationSupport.extractSeller(context);
        ProductSectionDto product = ReferenceValidationSupport.extractProduct(context);
        PricingSectionDto pricing = ReferenceValidationSupport.extractPricing(context);

        if (seller != null) {
            referenceDataValidator.validateSeller(seller.getSellerId());
        }
        if (product != null) {
            referenceDataValidator.validateCategory(product.getCategoryId());
        }
        if (pricing != null) {
            referenceDataValidator.validateCurrency(pricing.getCurrency());
        }
    }
}

package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductPatchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.InventorySectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.PricingSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class BusinessValidationStep implements ValidationStep {

    @Override
    public void validate(ProductValidationContext context) {
        if (context.operation() == ValidationOperation.DELETE) {
            return;
        }

        PricingSectionDto pricing = extractPricing(context);
        InventorySectionDto inventory = extractInventory(context);
        ProductSectionDto product = extractProduct(context);

        if (pricing != null) {
            validatePricing(pricing);
        }
        if (inventory != null) {
            validateInventory(inventory);
        }
        if (product != null && product.getStatus() != null) {
            validateStatus(product.getStatus(), context.operation());
        }
    }

    private void validatePricing(PricingSectionDto pricing) {
        if (pricing.getUnitPrice() != null && pricing.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "unitPrice must be >= 0");
        }
        if (pricing.getMinQuantity() != null && pricing.getMinQuantity() < 1) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "minQuantity must be >= 1");
        }
        if (pricing.getDiscountPercent() != null
                && (pricing.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0
                        || pricing.getDiscountPercent().compareTo(new BigDecimal("100")) > 0)) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "discountPercent must be between 0 and 100");
        }
        if (pricing.getValidFrom() != null
                && pricing.getValidTo() != null
                && pricing.getValidTo().isBefore(pricing.getValidFrom())) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "validTo must be after validFrom");
        }
    }

    private void validateInventory(InventorySectionDto inventory) {
        if (inventory.getQuantityAvailable() != null && inventory.getQuantityAvailable() < 0) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "quantityAvailable must be >= 0");
        }
        if (inventory.getQuantityReserved() != null && inventory.getQuantityReserved() < 0) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "quantityReserved must be >= 0");
        }
        if (inventory.getQuantityAvailable() != null
                && inventory.getQuantityReserved() != null
                && inventory.getQuantityReserved() > inventory.getQuantityAvailable()) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION, "quantityReserved cannot exceed quantityAvailable");
        }
        if (inventory.getReorderLevel() != null && inventory.getReorderLevel() < 0) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "reorderLevel must be >= 0");
        }
    }

    private void validateStatus(ProductStatus status, ValidationOperation operation) {
        if (operation == ValidationOperation.CREATE && status == ProductStatus.ARCHIVED) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot create product as ARCHIVED");
        }
    }

    private PricingSectionDto extractPricing(ProductValidationContext context) {
        if (context.request() instanceof CanonicalProductRequest request) {
            return request.getPricing();
        }
        if (context.request() instanceof CanonicalProductPatchRequest request) {
            return request.getPricing();
        }
        return null;
    }

    private InventorySectionDto extractInventory(ProductValidationContext context) {
        if (context.request() instanceof CanonicalProductRequest request) {
            return request.getInventory();
        }
        if (context.request() instanceof CanonicalProductPatchRequest request) {
            return request.getInventory();
        }
        return null;
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

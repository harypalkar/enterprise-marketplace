package com.enterprise.marketplace.pricingservice.domain.service;

import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.util.StringUtils;

/**
 * Pure domain rules for pricing validation and status transitions.
 */
public final class PricingDomainService {

    private PricingDomainService() {
    }

    public static void validateForCreate(Pricing pricing) {
        validateProductId(pricing.getProductId());
        validateSellerId(pricing.getSellerId());
        validateUnitPrice(pricing.getUnitPrice());
        validateCurrency(pricing.getCurrency());
        validateMinQuantity(pricing.getMinQuantity());
        validateDiscountPercent(pricing.getDiscountPercent());
        validateValidityWindow(pricing.getValidFrom(), pricing.getValidTo());
    }

    public static void validateForUpdate(Pricing pricing) {
        validateUnitPrice(pricing.getUnitPrice());
        validateCurrency(pricing.getCurrency());
        validateMinQuantity(pricing.getMinQuantity());
        validateDiscountPercent(pricing.getDiscountPercent());
        validateValidityWindow(pricing.getValidFrom(), pricing.getValidTo());
    }

    public static void validateStatusTransition(PricingStatus current, PricingStatus target) {
        if (current == target) {
            return;
        }
        if ((current == PricingStatus.ACTIVE && target == PricingStatus.INACTIVE)
                || (current == PricingStatus.INACTIVE && target == PricingStatus.ACTIVE)) {
            return;
        }
        throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s", current, target));
    }

    private static void validateProductId(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
    }

    private static void validateSellerId(UUID sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be zero or positive");
        }
    }

    private static void validateCurrency(String currency) {
        if (!StringUtils.hasText(currency) || currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code");
        }
    }

    private static void validateMinQuantity(Integer minQuantity) {
        if (minQuantity == null || minQuantity < 1) {
            throw new IllegalArgumentException("Minimum quantity must be at least 1");
        }
    }

    private static void validateDiscountPercent(BigDecimal discountPercent) {
        if (discountPercent == null) {
            return;
        }
        if (discountPercent.signum() < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
    }

    private static void validateValidityWindow(Instant validFrom, Instant validTo) {
        if (validFrom == null) {
            throw new IllegalArgumentException("validFrom is required");
        }
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo cannot be earlier than validFrom");
        }
    }
}

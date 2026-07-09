package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "marketplace.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryReferenceDataValidator implements ReferenceDataValidator {

    private static final Set<String> VALID_CURRENCIES = Set.of("INR", "USD", "EUR", "GBP");

    @Override
    public void validateSeller(UUID sellerId) {
        // In-memory mode accepts any non-null seller UUID for local development.
    }

    @Override
    public void validateCategory(UUID categoryId) {
        // In-memory mode accepts any non-null category UUID for local development.
    }

    @Override
    public void validateCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return;
        }
        if (!VALID_CURRENCIES.contains(currency.toUpperCase())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Unknown currency: " + currency);
        }
    }
}

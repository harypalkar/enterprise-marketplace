package com.enterprise.marketplace.productservice.validation;

import java.util.UUID;

public interface ReferenceDataValidator {

    void validateSeller(UUID sellerId);

    void validateCategory(UUID categoryId);

    void validateCurrency(String currency);
}

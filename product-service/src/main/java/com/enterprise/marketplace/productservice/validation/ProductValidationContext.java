package com.enterprise.marketplace.productservice.validation;

import java.util.UUID;

public record ProductValidationContext(Object request, ValidationOperation operation, UUID productId) {}

package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BeanValidationStep implements ValidationStep {

    private final Validator validator;

    @Override
    public void validate(ProductValidationContext context) {
        if (context.operation() == ValidationOperation.DELETE) {
            return;
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(context.request());
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Bean validation failed: " + message);
        }
    }
}

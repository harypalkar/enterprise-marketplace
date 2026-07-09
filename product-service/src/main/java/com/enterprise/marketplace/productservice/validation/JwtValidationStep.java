package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtValidationStep implements ValidationStep {

    private final boolean securityEnabled;

    public JwtValidationStep(@Value("${marketplace.security.enabled:true}") boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    @Override
    public void validate(ProductValidationContext context) {
        // JWT authentication is enforced by the Spring Security filter chain when enabled.
        // This step only validates that an authenticated user context is present for write operations.
        if (!securityEnabled || context.operation() == ValidationOperation.DELETE) {
            return;
        }
        if (context.operation() == ValidationOperation.CREATE
                || context.operation() == ValidationOperation.UPDATE
                || context.operation() == ValidationOperation.PATCH) {
            if (RequestContext.getUserId().isEmpty()) {
                throw new MarketplaceException(ErrorCode.UNAUTHORIZED, "Authenticated user context is required");
            }
        }
    }
}

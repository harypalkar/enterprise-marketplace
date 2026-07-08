package com.enterprise.marketplace.common.exception;

/**
 * Thrown when a requested resource does not exist.
 */
public class ResourceNotFoundException extends MarketplaceException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s not found with identifier: %s", resource, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}

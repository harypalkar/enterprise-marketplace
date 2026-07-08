package com.enterprise.marketplace.common.exception;

import lombok.Getter;

/**
 * Base runtime exception for domain and application errors.
 */
@Getter
public class MarketplaceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public MarketplaceException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public MarketplaceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public MarketplaceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public MarketplaceException(ErrorCode errorCode, Object... args) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = args;
    }
}

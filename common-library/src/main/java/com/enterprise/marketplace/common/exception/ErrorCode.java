package com.enterprise.marketplace.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Application-wide error codes mapped to HTTP semantics.
 */
@Getter
public enum ErrorCode {

    INTERNAL_ERROR("ERR-0001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("ERR-0002", "Validation failed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("ERR-0003", "Resource not found", HttpStatus.NOT_FOUND),
    BUSINESS_RULE_VIOLATION("ERR-0004", "Business rule violation", HttpStatus.UNPROCESSABLE_ENTITY),
    UNAUTHORIZED("ERR-0005", "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ERR-0006", "Forbidden", HttpStatus.FORBIDDEN),
    CONFLICT("ERR-0007", "Conflict", HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_REQUIRED("ERR-0008", "Idempotency-Key header is required", HttpStatus.BAD_REQUEST),
    IDEMPOTENCY_KEY_IN_PROGRESS("ERR-0009", "Request with this idempotency key is in progress", HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_REUSED("ERR-0010", "Idempotency key reused with different payload", HttpStatus.CONFLICT);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}

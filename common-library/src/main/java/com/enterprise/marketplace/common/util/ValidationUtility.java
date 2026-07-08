package com.enterprise.marketplace.common.util;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Shared validation helpers for domain and API input validation.
 */
@Component
@RequiredArgsConstructor
public class ValidationUtility {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern GSTIN_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    private final Validator validator;

    public <T> void validateAndThrow(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, message);
        }
    }

    public void requireNonBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR, fieldName + " must not be blank");
        }
    }

    public void requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR, fieldName + " must be a positive number");
        }
    }

    public void requireEmail(String email) {
        requireNonBlank(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid email format");
        }
    }

    public void requireIndianMobile(String mobile) {
        requireNonBlank(mobile, "mobile");
        if (!INDIAN_MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid Indian mobile number");
        }
    }

    public void requireGstin(String gstin) {
        requireNonBlank(gstin, "gstin");
        if (!GSTIN_PATTERN.matcher(gstin.toUpperCase()).matches()) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid GSTIN format");
        }
    }

    public void requirePan(String pan) {
        requireNonBlank(pan, "pan");
        if (!PAN_PATTERN.matcher(pan.toUpperCase()).matches()) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid PAN format");
        }
    }
}

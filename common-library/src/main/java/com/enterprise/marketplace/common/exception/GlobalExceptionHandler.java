package com.enterprise.marketplace.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.enterprise.marketplace.common.api.ErrorResponse;
import com.enterprise.marketplace.common.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling for all marketplace microservices.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MarketplaceException.class)
    public ResponseEntity<ErrorResponse> handleMarketplaceException(
            MarketplaceException ex, HttpServletRequest request) {
        log.warn(
                "MarketplaceException: code={}, message={}, path={}",
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                request.getRequestURI());

        return buildResponse(
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                ex.getErrorCode().getHttpStatus().value(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        log.warn("Validation failed on path={}: {} error(s)", request.getRequestURI(), fieldErrors.size());

        return buildResponse(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                ErrorCode.VALIDATION_ERROR.getHttpStatus().value(),
                request.getRequestURI(),
                fieldErrors);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request on path={}: {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Malformed request or invalid parameter value",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "Endpoint not found",
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on path={}", request.getRequestURI(), ex);

        return buildResponse(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                ErrorCode.INTERNAL_ERROR.getHttpStatus().value(),
                request.getRequestURI(),
                null);
    }

    private ErrorResponse.FieldError mapFieldError(FieldError fieldError) {
        return ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            String code,
            String message,
            int status,
            String path,
            List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .path(path)
                .correlationId(RequestContext.getCorrelationId())
                .requestId(RequestContext.getRequestId())
                .timestamp(Instant.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}

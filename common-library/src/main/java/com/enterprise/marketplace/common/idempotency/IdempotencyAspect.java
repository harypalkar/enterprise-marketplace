package com.enterprise.marketplace.common.idempotency;

import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP interceptor enforcing idempotency semantics on annotated controller methods.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = currentRequest();
        String idempotencyKey = request.getHeader(HttpHeaders.IDEMPOTENCY_KEY);

        if (!StringUtils.hasText(idempotencyKey)) {
            throw new MarketplaceException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        RequestContext.setIdempotencyKey(idempotencyKey);
        String requestHash = hashRequest(request, joinPoint.getArgs());

        Optional<IdempotencyRecord> existing = idempotencyStore.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new MarketplaceException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            }
            if (record.getStatus() == IdempotencyRecord.Status.IN_PROGRESS) {
                throw new MarketplaceException(ErrorCode.IDEMPOTENCY_KEY_IN_PROGRESS);
            }
            log.info("Returning cached idempotent response for key={}", idempotencyKey);
            return ResponseEntity.status(record.getHttpStatus()).body(record.getResponseBody());
        }

        if (!idempotencyStore.tryAcquire(idempotencyKey, requestHash, idempotent.ttlSeconds())) {
            throw new MarketplaceException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
        }

        try {
            Object result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                String body = objectMapper.writeValueAsString(responseEntity.getBody());
                idempotencyStore.complete(idempotencyKey, responseEntity.getStatusCode().value(), body);
            } else {
                String body = objectMapper.writeValueAsString(result);
                idempotencyStore.complete(idempotencyKey, 200, body);
            }
            return result;
        } catch (Throwable ex) {
            idempotencyStore.release(idempotencyKey);
            throw ex;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No active HTTP request context");
        }
        return attributes.getRequest();
    }

    private String hashRequest(HttpServletRequest request, Object[] args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
            digest.update(request.getRequestURI().getBytes(StandardCharsets.UTF_8));
            digest.update(objectMapper.writeValueAsBytes(args));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to hash idempotency payload", ex);
        }
    }
}

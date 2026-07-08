package com.enterprise.marketplace.common.filter;

import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Propagates correlation and request identifiers across service boundaries.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String correlationId = resolveHeader(request, HttpHeaders.CORRELATION_ID);
            if (!StringUtils.hasText(correlationId)) {
                correlationId = RequestContext.generateCorrelationId();
            }

            String requestId = resolveHeader(request, HttpHeaders.REQUEST_ID);
            if (!StringUtils.hasText(requestId)) {
                requestId = RequestContext.generateRequestId();
            }

            RequestContext.setCorrelationId(correlationId);
            RequestContext.setRequestId(requestId);

            String tenantId = resolveHeader(request, HttpHeaders.TENANT_ID);
            if (StringUtils.hasText(tenantId)) {
                RequestContext.setTenantId(tenantId);
            }

            String userId = resolveHeader(request, HttpHeaders.USER_ID);
            if (StringUtils.hasText(userId)) {
                RequestContext.setUserId(userId);
            }

            response.setHeader(HttpHeaders.CORRELATION_ID, correlationId);
            response.setHeader(HttpHeaders.REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }

    private String resolveHeader(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }
}

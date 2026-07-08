package com.enterprise.marketplace.common.util;

import com.enterprise.marketplace.common.constant.MdcKeys;
import com.enterprise.marketplace.common.context.RequestContext;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Structured logging helpers aligned with platform logging standards.
 */
@Slf4j
public final class LoggingUtility {

    private LoggingUtility() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void setServiceName(String serviceName) {
        MDC.put(MdcKeys.SERVICE_NAME, serviceName);
    }

    public static void info(String message, Object... args) {
        log.info(enrichMessage(message), args);
    }

    public static void warn(String message, Object... args) {
        log.warn(enrichMessage(message), args);
    }

    public static void error(String message, Throwable throwable) {
        log.error(enrichMessage(message), throwable);
    }

    public static void debug(String message, Object... args) {
        log.debug(enrichMessage(message), args);
    }

    public static void logEvent(String event, Map<String, Object> attributes) {
        MDC.put("event", event);
        try {
            attributes.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
            log.info("event={} correlationId={} requestId={}", event, RequestContext.getCorrelationId(), RequestContext.getRequestId());
        } finally {
            MDC.remove("event");
            attributes.keySet().forEach(MDC::remove);
        }
    }

    private static String enrichMessage(String message) {
        return String.format(
                "[correlationId=%s][requestId=%s] %s",
                RequestContext.getCorrelationId(), RequestContext.getRequestId(), message);
    }
}

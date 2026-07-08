package com.enterprise.marketplace.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextTest {

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    @Test
    void shouldSetAndGetCorrelationAndRequestIds() {
        RequestContext.setCorrelationId("corr-1");
        RequestContext.setRequestId("req-1");

        assertThat(RequestContext.getCorrelationId()).isEqualTo("corr-1");
        assertThat(RequestContext.getRequestId()).isEqualTo("req-1");
    }

    @Test
    void shouldGenerateUniqueIds() {
        String correlationId1 = RequestContext.generateCorrelationId();
        String correlationId2 = RequestContext.generateCorrelationId();

        assertThat(correlationId1).isNotBlank();
        assertThat(correlationId2).isNotBlank();
        assertThat(correlationId1).isNotEqualTo(correlationId2);
    }
}

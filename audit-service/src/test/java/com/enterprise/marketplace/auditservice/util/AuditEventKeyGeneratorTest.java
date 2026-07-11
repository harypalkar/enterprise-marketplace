package com.enterprise.marketplace.auditservice.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import org.junit.jupiter.api.Test;

class AuditEventKeyGeneratorTest {

    @Test
    void shouldGenerateDeterministicEventKey() {
        String key = AuditEventKeyGenerator.generate("workflow-service", "req-123", AuditOperation.CREATE);
        assertThat(key).isEqualTo("workflow-service:req-123:CREATE");
    }

    @Test
    void shouldRejectMissingFields() {
        assertThatThrownBy(() -> AuditEventKeyGenerator.generate("", "req", AuditOperation.CREATE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package com.enterprise.marketplace.notificationservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void shouldReplaceTemplateVariables() {
        String result = renderer.render(
                "Hello {{name}}, workflow {{workflowId}} completed.",
                Map.of("name", "User", "workflowId", "abc-123"));

        assertThat(result).isEqualTo("Hello User, workflow abc-123 completed.");
    }

    @Test
    void shouldLeaveUnknownPlaceholdersUntouched() {
        String result = renderer.render("Hello {{name}}", Map.of());

        assertThat(result).isEqualTo("Hello {{name}}");
    }
}

package com.enterprise.marketplace.notificationservice.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.TemplateContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationTemplateEngineTest {

    private final NotificationTemplateEngine engine = new NotificationTemplateEngine(new com.enterprise.marketplace.notificationservice.util.TemplateRenderer());

    @Test
    void shouldRenderHtmlTemplateWithVariables() {
        NotificationTemplateEntity template = new NotificationTemplateEntity();
        template.setTemplateCode("TEST");
        template.setChannel(NotificationChannel.EMAIL);
        template.setContentType(TemplateContentType.HTML);
        template.setSubject("Hello {{name}}");
        template.setBodyTemplate("<p>Welcome {{name}}</p>");

        NotificationTemplateEngine.RenderedTemplate rendered =
                engine.render(template, Map.of("name", "Enterprise"));

        assertThat(rendered.subject()).isEqualTo("Hello Enterprise");
        assertThat(rendered.body()).isEqualTo("<p>Welcome Enterprise</p>");
        assertThat(rendered.isHtml()).isTrue();
    }
}

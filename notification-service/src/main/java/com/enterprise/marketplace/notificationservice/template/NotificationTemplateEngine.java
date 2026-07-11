package com.enterprise.marketplace.notificationservice.template;

import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import com.enterprise.marketplace.notificationservice.enums.TemplateContentType;
import com.enterprise.marketplace.notificationservice.exception.NotificationTemplateException;
import com.enterprise.marketplace.notificationservice.util.TemplateRenderer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationTemplateEngine {

    private final TemplateRenderer templateRenderer;

    public RenderedTemplate render(NotificationTemplateEntity template, Map<String, String> variables) {
        if (template == null) {
            throw new NotificationTemplateException("Template is required");
        }
        TemplateContentType contentType =
                template.getContentType() != null ? template.getContentType() : TemplateContentType.PLAIN_TEXT;
        String subject = templateRenderer.render(template.getSubject(), variables);
        String body = templateRenderer.render(template.getBodyTemplate(), variables);
        return new RenderedTemplate(contentType, subject, body);
    }

    public record RenderedTemplate(TemplateContentType contentType, String subject, String body) {

        public boolean isHtml() {
            return contentType == TemplateContentType.HTML;
        }

        public String plainBody() {
            if (!StringUtils.hasText(body)) {
                return body;
            }
            if (contentType == TemplateContentType.HTML) {
                return body.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            }
            return body;
        }
    }
}

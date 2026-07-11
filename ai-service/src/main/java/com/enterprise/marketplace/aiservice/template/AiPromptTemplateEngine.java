package com.enterprise.marketplace.aiservice.template;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AiPromptTemplateEngine {

    public String render(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }
        String rendered = template;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return rendered;
    }
}

package com.enterprise.marketplace.notificationservice.util;

import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TemplateRenderer {

    public String render(String template, Map<String, String> variables) {
        if (!StringUtils.hasText(template)) {
            return template;
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            rendered = rendered.replace(placeholder, value);
        }
        return rendered;
    }

    public Map<String, String> mergeVariables(Map<String, String> primary, Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return primary != null ? primary : Collections.emptyMap();
        }
        Map<String, String> merged = primary != null
                ? new java.util.HashMap<>(primary)
                : new java.util.HashMap<>();
        metadata.forEach((key, value) -> merged.putIfAbsent(key, value != null ? String.valueOf(value) : ""));
        return merged;
    }
}

package com.enterprise.marketplace.adminservice.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigResponse {

    private UUID id;
    private String configKey;
    private Map<String, Object> configValue;
    private String scope;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}

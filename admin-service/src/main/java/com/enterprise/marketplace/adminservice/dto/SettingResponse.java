package com.enterprise.marketplace.adminservice.dto;

import java.time.Instant;
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
public class SettingResponse {

    private UUID id;
    private String settingKey;
    private String settingValue;
    private String category;
    private String description;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}

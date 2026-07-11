package com.enterprise.marketplace.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UpdateSettingRequest {

    @NotBlank
    @Size(max = 2000)
    private String settingValue;

    @Size(max = 64)
    private String category;

    @Size(max = 512)
    private String description;

    private Boolean active;
}

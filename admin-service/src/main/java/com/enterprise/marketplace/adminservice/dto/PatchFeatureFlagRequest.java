package com.enterprise.marketplace.adminservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class PatchFeatureFlagRequest {

    private Boolean enabled;

    @Size(max = 512)
    private String description;

    @Min(0)
    @Max(100)
    private Integer rolloutPercentage;
}

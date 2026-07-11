package com.enterprise.marketplace.adminservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
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
public class BulkPatchFeatureFlagsRequest {

    @NotEmpty
    @Valid
    private List<FeatureFlagPatchItem> updates;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureFlagPatchItem {

        private String flagKey;

        private Boolean enabled;

        private Integer rolloutPercentage;
    }
}

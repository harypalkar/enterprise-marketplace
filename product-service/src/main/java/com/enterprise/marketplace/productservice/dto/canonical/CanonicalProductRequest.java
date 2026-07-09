package com.enterprise.marketplace.productservice.dto.canonical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanonicalProductRequest {

    @NotNull
    @Valid
    RequestHeaderDto header;

    @NotNull
    @Valid
    RequestInfoDto requestInfo;

    @NotNull
    @Valid
    SellerSectionDto seller;

    @NotNull
    @Valid
    ProductSectionDto product;

    @NotNull
    @Valid
    PricingSectionDto pricing;

    @NotNull
    @Valid
    InventorySectionDto inventory;

    @Valid
    List<AttributeSectionDto> attributes;

    @Valid
    List<MediaSectionDto> media;

    @Valid
    WorkflowSectionDto workflow;

    @Valid
    MetadataSectionDto metadata;
}

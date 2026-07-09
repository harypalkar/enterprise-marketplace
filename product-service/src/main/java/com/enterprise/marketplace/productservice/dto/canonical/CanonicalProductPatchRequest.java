package com.enterprise.marketplace.productservice.dto.canonical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanonicalProductPatchRequest {

    @Valid
    RequestHeaderDto header;

    @Valid
    RequestInfoDto requestInfo;

    @Valid
    SellerSectionDto seller;

    @Valid
    ProductSectionDto product;

    @Valid
    PricingSectionDto pricing;

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

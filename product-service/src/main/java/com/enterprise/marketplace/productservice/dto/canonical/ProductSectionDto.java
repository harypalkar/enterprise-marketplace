package com.enterprise.marketplace.productservice.dto.canonical;

import com.enterprise.marketplace.productservice.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.util.UUID;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSectionDto {

    @NotBlank
    String sku;

    @NotBlank
    String name;

    String description;

    UUID categoryId;

    String unitOfMeasure;

    String hsnCode;

    ProductStatus status;
}

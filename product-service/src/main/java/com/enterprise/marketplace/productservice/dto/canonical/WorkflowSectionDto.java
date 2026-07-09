package com.enterprise.marketplace.productservice.dto.canonical;

import com.enterprise.marketplace.productservice.enums.ProductWorkflowStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowSectionDto {

    ProductWorkflowStatus targetStatus;

    String message;
}

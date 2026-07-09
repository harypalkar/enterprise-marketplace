package com.enterprise.marketplace.inventoryservice.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InventoryPageResponse {

    List<InventoryResponse> content;
    long totalElements;
    int totalPages;
    int page;
    int size;
}

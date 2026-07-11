package com.enterprise.marketplace.notificationservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxPageResponse {

    private List<InboxResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

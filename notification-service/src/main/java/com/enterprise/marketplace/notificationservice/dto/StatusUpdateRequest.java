package com.enterprise.marketplace.notificationservice.dto;

import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull
    private NotificationStatus targetStatus;

    @Size(max = 2000)
    private String message;
}

package com.enterprise.marketplace.notificationservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.notificationservice.dto.InboxPageResponse;
import com.enterprise.marketplace.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inbox")
@RequiredArgsConstructor
@Tag(name = "Inbox", description = "In-app notification inbox APIs")
public class InboxController {

    private final NotificationService notificationService;

    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get inbox messages for recipient")
    public ResponseEntity<ApiResponse<InboxPageResponse>> getInboxByRecipientId(
            @PathVariable String recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.getInboxByRecipientId(recipientId, page, size)));
    }
}

package com.enterprise.marketplace.identityservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.identityservice.application.dto.ConfirmQrRequest;
import com.enterprise.marketplace.identityservice.application.dto.CreateQrRequest;
import com.enterprise.marketplace.identityservice.application.dto.QrCreateResponse;
import com.enterprise.marketplace.identityservice.application.dto.QrStatusResponse;
import com.enterprise.marketplace.identityservice.application.service.QrSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/qr")
@RequiredArgsConstructor
@Tag(name = "Web QR Login")
public class AuthQrController {

    private final QrSessionService qrSessionService;

    @PostMapping("/create")
    @Operation(summary = "Create QR code session for web login")
    public ResponseEntity<ApiResponse<QrCreateResponse>> createQr(
            @RequestBody(required = false) CreateQrRequest request) {
        CreateQrRequest payload = request != null ? request : new CreateQrRequest();
        return ResponseEntity.ok(ApiResponse.success(qrSessionService.createQrSession(payload), "QR session created"));
    }

    @GetMapping("/{qrSessionId}")
    @Operation(summary = "Poll QR session status")
    public ResponseEntity<ApiResponse<QrStatusResponse>> getQr(@PathVariable UUID qrSessionId) {
        return ResponseEntity.ok(ApiResponse.success(qrSessionService.getQrSession(qrSessionId.toString())));
    }

    @PostMapping("/{qrSessionId}/confirm")
    @Operation(summary = "Confirm QR session from mobile after authentication")
    public ResponseEntity<ApiResponse<QrStatusResponse>> confirmQr(
            @PathVariable UUID qrSessionId, @Valid @RequestBody ConfirmQrRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                qrSessionService.confirmQrSession(qrSessionId.toString(), request), "QR session confirmed"));
    }
}

package com.enterprise.marketplace.identityservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.identityservice.application.dto.CreatePinRequest;
import com.enterprise.marketplace.identityservice.application.dto.PinAuthResponse;
import com.enterprise.marketplace.identityservice.application.dto.VerifyPinRequest;
import com.enterprise.marketplace.identityservice.application.service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/pin")
@RequiredArgsConstructor
@Tag(name = "Mobile PIN Auth")
public class AuthPinController {

    private final PinService pinService;

    @PostMapping("/create")
    @Operation(summary = "Create a 6-digit secure PIN")
    public ResponseEntity<ApiResponse<PinAuthResponse>> createPin(@Valid @RequestBody CreatePinRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pinService.createPin(request), "PIN created successfully"));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify PIN and issue access token")
    public ResponseEntity<ApiResponse<PinAuthResponse>> verifyPin(@Valid @RequestBody VerifyPinRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pinService.verifyPin(request), "PIN verified successfully"));
    }
}

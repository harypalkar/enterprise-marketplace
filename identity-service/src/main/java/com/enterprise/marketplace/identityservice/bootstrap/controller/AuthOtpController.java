package com.enterprise.marketplace.identityservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.identityservice.application.dto.OtpResponse;
import com.enterprise.marketplace.identityservice.application.dto.ResendOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.SendOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.VerifyOtpRequest;
import com.enterprise.marketplace.identityservice.application.dto.VerifyOtpResponse;
import com.enterprise.marketplace.identityservice.application.service.OtpService;
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
@RequestMapping("/api/v1/auth/otp")
@RequiredArgsConstructor
@Tag(name = "Mobile OTP Auth")
public class AuthOtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    @Operation(summary = "Send OTP to mobile number (SMS only when marketplace.auth.otp.sms-enabled=true)")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpResponse data = otpService.sendOtp(request);
        String message = data.isSmsDelivered()
                ? "OTP sent successfully"
                : "OTP generated (SMS disabled — use data.otp from this response)";
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend OTP for an existing session")
    public ResponseEntity<ApiResponse<OtpResponse>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        OtpResponse data = otpService.resendOtp(request);
        String message = data.isSmsDelivered()
                ? "OTP resent successfully"
                : "OTP regenerated (SMS disabled — use data.otp from this response)";
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP and issue verification token")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(otpService.verifyOtp(request), "OTP verified successfully"));
    }
}

package com.enterprise.marketplace.identityservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> endpoints = new LinkedHashMap<>();
        endpoints.put("health", "/api/v1/bootstrap/health");
        endpoints.put("swagger", "/swagger-ui.html");
        endpoints.put("sendOtp", "POST /api/v1/auth/otp/send");
        endpoints.put("resendOtp", "POST /api/v1/auth/otp/resend");
        endpoints.put("verifyOtp", "POST /api/v1/auth/otp/verify");
        endpoints.put("setUserType", "POST /api/v1/auth/user/type");
        endpoints.put("setUserDetails", "POST /api/v1/auth/user/details");
        endpoints.put("createPin", "POST /api/v1/auth/pin/create");
        endpoints.put("verifyPin", "POST /api/v1/auth/pin/verify");
        endpoints.put("createQr", "POST /api/v1/auth/qr/create");
        endpoints.put("pollQr", "GET /api/v1/auth/qr/{qrSessionId}");
        endpoints.put("confirmQr", "POST /api/v1/auth/qr/{qrSessionId}/confirm");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "identity-service");
        payload.put("message", "Use the auth endpoints below (not /). Via gateway prefix with /api/identity");
        payload.put("endpoints", endpoints);

        return ResponseEntity.ok(ApiResponse.success(payload, "Identity service root"));
    }
}

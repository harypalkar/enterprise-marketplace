package com.enterprise.marketplace.identityservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.identityservice.application.dto.SetUserDetailsRequest;
import com.enterprise.marketplace.identityservice.application.dto.SetUserTypeRequest;
import com.enterprise.marketplace.identityservice.application.dto.UserDetailsResponse;
import com.enterprise.marketplace.identityservice.application.dto.UserTypeResponse;
import com.enterprise.marketplace.identityservice.application.service.MobileUserService;
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
@RequestMapping("/api/v1/auth/user")
@RequiredArgsConstructor
@Tag(name = "Mobile User Onboarding")
public class AuthUserController {

    private final MobileUserService mobileUserService;

    @PostMapping("/type")
    @Operation(summary = "Set user type (INDIVIDUAL or BUSINESS)")
    public ResponseEntity<ApiResponse<UserTypeResponse>> setUserType(@Valid @RequestBody SetUserTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(mobileUserService.setUserType(request), "User type set successfully"));
    }

    @PostMapping("/details")
    @Operation(summary = "Set user details for individual or business accounts")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> setUserDetails(
            @Valid @RequestBody SetUserDetailsRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(mobileUserService.setUserDetails(request), "User details saved successfully"));
    }
}

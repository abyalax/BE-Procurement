package com.procurement.modules.auth.controller;

import com.procurement.modules.auth.dto.*;
import com.procurement.modules.auth.services.AuthService;
import com.procurement.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("User registered successfully", authService.register(request));
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login success", authService.login(request));
    }
}

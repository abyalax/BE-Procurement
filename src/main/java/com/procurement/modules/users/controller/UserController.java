package com.procurement.modules.users.controller;

import com.procurement.common.response.PageResponse;
import com.procurement.common.response.ApiResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.users.dto.UpdateUserRequest;
import com.procurement.modules.users.dto.UserResponse;
import com.procurement.modules.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users. Admin only.")
    @GetMapping
    @Guard({"users:read"})
    public ApiResponse<PageResponse<UserResponse>> getUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Users fetched successfully", userService.getUsers(page, size));
    }

    @Operation(summary = "Get user by id. Admin only.")
    @GetMapping("/{id}")
    @Guard({"users:read"})
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success("User fetched successfully", userService.getUserById(id));
    }

    @Operation(summary = "Get current authenticated user")
    @GetMapping("/me")
    @Guard
    public ApiResponse<UserResponse> getMe(Authentication authentication) {
        return ApiResponse.success("Current user fetched successfully",
                userService.getCurrentUser(authentication.getName()));
    }

    @Operation(summary = "Update user. Admin only.")
    @PutMapping("/{id}")
    @Guard({"users:write"})
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success("User updated successfully", userService.updateUser(id, request));
    }
}

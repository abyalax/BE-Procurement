package com.procurement.modules.users.controller;

import com.procurement.common.response.ApiResponse;
import com.procurement.common.response.PageResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.users.dto.CreateUserRequest;
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
  @Guard({ "users:read" })
  public ApiResponse<PageResponse<UserResponse>> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "Users fetched successfully",
      userService.getUsers(page, limit, sortBy, sortOrder)
    );
  }

  @Operation(summary = "Get user by id. Admin only.")
  @GetMapping("/{id}")
  @Guard({ "users:read" })
  public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
    return ApiResponse.success("User fetched successfully", userService.getUserById(id));
  }

  @Operation(summary = "Create user. Admin only.")
  @PostMapping
  @Guard({ "users:write" })
  public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    return ApiResponse.success("User created successfully", userService.createUser(request));
  }

  @Operation(summary = "Get current authenticated user")
  @GetMapping("/me")
  @Guard
  public ApiResponse<UserResponse> getMe(Authentication authentication) {
    return ApiResponse.success(
      "Current user fetched successfully",
      userService.getCurrentUser(authentication.getName())
    );
  }

  @Operation(summary = "Update user. Admin only.")
  @PatchMapping("/{id}")
  @Guard({ "users:write" })
  public ApiResponse<UserResponse> updateUser(
    @PathVariable Long id,
    @Valid @RequestBody UpdateUserRequest request
  ) {
    return ApiResponse.success("User updated successfully", userService.updateUser(id, request));
  }

  @Operation(summary = "Deactivate user. Admin only.")
  @DeleteMapping("/{id}")
  @Guard({ "users:write" })
  public ApiResponse<Boolean> deactivateUser(@PathVariable Long id) {
    userService.deactivateUser(id);
    return ApiResponse.success("User deactivated successfully", true);
  }
}

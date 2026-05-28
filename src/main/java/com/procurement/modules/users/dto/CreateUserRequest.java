package com.procurement.modules.users.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record CreateUserRequest(
  @NotBlank(message = "Name is required")
  @Size(max = 120, message = "Name must not exceed 120 characters")
  String name,

  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid")
  @Size(max = 160, message = "Email must not exceed 160 characters")
  String email,

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
  String password,

  Set<String> roles
) {}

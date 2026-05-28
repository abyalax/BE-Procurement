package com.procurement.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.procurement.modules.users.dto.UserResponse;
import com.procurement.modules.users.entities.UserStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestUser(
  Long id,

  @NotBlank(message = "User name is required")
  @Size(max = 120, message = "User name must not exceed 120 characters")
  String name,

  @NotBlank(message = "User email is required")
  @Email(message = "User email format is invalid")
  @Size(max = 160, message = "User email must not exceed 160 characters")
  String email,

  UserStatus status,

  Set<String> roles,

  Set<String> permissions,

  LocalDateTime createdAt,

  LocalDateTime updatedAt
) {
  public String label() {
    return email == null || email.isBlank() ? name.trim() : email.trim().toLowerCase();
  }

  public static RequestUser from(UserResponse user) {
    return new RequestUser(
      user.id(),
      user.name(),
      user.email(),
      user.status(),
      user.roles(),
      user.permissions(),
      user.createdAt(),
      user.updatedAt()
    );
  }
}

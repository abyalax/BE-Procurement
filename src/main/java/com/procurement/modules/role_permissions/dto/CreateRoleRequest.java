package com.procurement.modules.role_permissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(max = 80, message = "Role name must not exceed 80 characters")
        String name,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        Set<String> permissions) {}

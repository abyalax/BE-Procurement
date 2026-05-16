package com.procurement.modules.role_permissions.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateRolePermissionsRequest(
        @NotNull(message = "Permissions are required")
        Set<String> permissions) {}

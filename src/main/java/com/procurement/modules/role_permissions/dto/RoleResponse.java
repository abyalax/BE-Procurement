package com.procurement.modules.role_permissions.dto;

import java.util.Set;

public record RoleResponse(Long id, String name, String description, Set<String> permissions) {}

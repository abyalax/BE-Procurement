package com.procurement.modules.users.dto;

import com.procurement.modules.users.entities.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserStatus status,
        Set<String> roles,
        Set<String> permissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

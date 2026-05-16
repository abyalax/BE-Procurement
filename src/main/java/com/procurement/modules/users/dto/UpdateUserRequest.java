package com.procurement.modules.users.dto;

import com.procurement.modules.users.entities.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(
        @Size(max = 120, message = "Name must not exceed 120 characters")
        String name,

        @Email(message = "Email format is invalid")
        @Size(max = 160, message = "Email must not exceed 160 characters")
        String email,

        UserStatus status,

        Set<String> roles
) {}

package com.procurement.modules.auth.dto;

import com.procurement.modules.users.dto.UserResponse;

public record AuthResponse(
    String accessToken,
    String tokenType,
    Long expiresInMs,
    UserResponse user
) {}
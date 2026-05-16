package com.procurement.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
        @NotBlank(message = "Default admin email is required") String adminEmail,

        @NotBlank(message = "Default admin password is required")
        @Size(min = 8, message = "Default admin password must be at least 8 characters")
        String adminPassword) {
}

package com.procurement.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI procurementOpenApi() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info().title("Procurement API").version("v1")
                        .description("API documentation for Procurement application"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme().name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP).scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

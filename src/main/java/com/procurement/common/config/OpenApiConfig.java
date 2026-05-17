package com.procurement.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  OpenAPI procurementOpenApi() {
    Info apiInfo = new Info()
      .title("Procurement API")
      .version("v1")
      .description("API documentation for Procurement application");

    SecurityRequirement securityRequirement = new SecurityRequirement().addList(
      SECURITY_SCHEME_NAME
    );

    SecurityScheme bearerSecurityScheme = new SecurityScheme()
      .name(SECURITY_SCHEME_NAME)
      .type(SecurityScheme.Type.HTTP)
      .scheme("bearer")
      .bearerFormat("JWT");

    Components components = new Components().addSecuritySchemes(
      SECURITY_SCHEME_NAME,
      bearerSecurityScheme
    );

    return new OpenAPI().info(apiInfo).addSecurityItem(securityRequirement).components(components);
  }
}

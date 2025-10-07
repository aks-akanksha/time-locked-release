package com.example.timelock.api;

import io.swagger.v3.oas.annotations.security.SecurityScheme;     // annotation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;    // enum for the annotation

import io.swagger.v3.oas.models.OpenAPI;                          // model types below are safe
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(                                     // <-- this is the ANNOTATION
  name = "bearer",
  type = SecuritySchemeType.HTTP,
  scheme = "bearer",
  bearerFormat = "JWT"
)
public class OpenApiConfig {

  @Bean
  public OpenAPI api() {
    return new OpenAPI()
      .components(new Components().addSecuritySchemes(
        "bearer",
        new io.swagger.v3.oas.models.security.SecurityScheme()     // <-- fully-qualified MODEL class
          .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
      ))
      .addSecurityItem(new SecurityRequirement().addList("bearer"));
  }
}

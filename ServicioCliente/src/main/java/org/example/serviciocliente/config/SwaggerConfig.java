package org.example.serviciocliente.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configuración de Swagger/OpenAPI para incluir seguridad con JWT Bearer Token
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 1. Añade el requisito de seguridad global (el candado en los endpoints)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 2. Define el esquema de seguridad (Bearer Token JWT)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP) // Tipo HTTP
                                .scheme("bearer")               // Esquema Bearer
                                .bearerFormat("JWT")            // Formato JWT
                                .in(SecurityScheme.In.HEADER)   // El token va en el Header
                                .name("Authorization")          // Nombre del Header
                        )
                );
    }
}
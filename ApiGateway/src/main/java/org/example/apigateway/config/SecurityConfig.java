package org.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Permite el acceso sin autenticación a la documentación de la API de cada microservicio
                        .pathMatchers(
                                "/servicio-envios/v3/api-docs/**", "/servicio-envios/swagger-ui/**",
                                "/servicio-cliente/v3/api-docs/**", "/servicio-cliente/swagger-ui/**",
                                "/servicio-flota/v3/api-docs/**", "/servicio-flota/swagger-ui/**",
                                "/servicio-tarifa/v3/api-docs/**", "/servicio-tarifa/swagger-ui/**",
                                "/webjars/**"
                        ).permitAll()
                        // Requiere autenticación para cualquier otra petición
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
                );

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Usa el URI de tu application.yml para decodificar y validar el token JWT
        return NimbusReactiveJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}

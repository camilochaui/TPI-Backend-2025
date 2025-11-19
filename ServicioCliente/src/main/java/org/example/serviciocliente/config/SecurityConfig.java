package org.example.serviciocliente.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Se elimina el import de @EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri; // URL al JWK de Keycloak (desde application.yml)

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/api/v1/**",      // <-- Ignora toda tu API
                "/v3/api-docs/**", // <-- Ignora la especificación OpenAPI
                "/swagger-ui/**",  // <-- Ignora la UI de Swagger
                "/swagger-ui.html",
                "/actuator/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactiva CSRF (no necesario para APIs REST stateless)
                .csrf(csrf -> csrf.disable())

                // Configura la autorización de endpoint (Aunque webSecurityCustomizer ya lo ignora, dejamos .permitAll() por coherencia)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // <-- CAMBIADO DE .authenticated()
                )

                // Configura el servidor de recursos OAuth2 para validar JWTs (Ahora es opcional, pero lo dejamos para cuando reviertas el cambio)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                // Configura la política de sesión como STATELESS (API REST)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Configura el decodificador para usar la URL JWK de Keycloak
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Busca los roles dentro del claim 'realm_access' del JWT de KeyKcloak
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access");

        // Busca la lista 'roles' dentro de 'realm_access'
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // Prefijo estándar

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }
}
package org.example.servicioenvios.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class FeignSecurityConfig {

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return template -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            String tokenValue = null;

            // 1. Si hay un usuario autenticado, propagar su token
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                tokenValue = jwtAuth.getToken().getTokenValue();
                System.out.println(">>> Usando token del usuario autenticado");
            }
            // 2. Si no hay usuario, usar Client Credentials
            else {
                System.out.println(">>> No hay usuario autenticado, usando Client Credentials");
                var authorizeRequest = OAuth2AuthorizeRequest
                        .withClientRegistrationId("keycloak")
                        .principal("servicio-envios")
                        .build();

                var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                    tokenValue = authorizedClient.getAccessToken().getTokenValue();
                    System.out.println(">>> Token obtenido via Client Credentials");
                }
            }

            if (tokenValue != null) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
            } else {
                System.err.println(">>> ERROR: No se pudo obtener token");
            }
        };
    }
}

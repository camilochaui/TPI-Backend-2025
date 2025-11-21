package org.example.servicioenvios.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

@Configuration
@RequiredArgsConstructor
public class FeignOAuth2Config {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Bean
    public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager() {
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService);
    }

    /**
     * Interceptor que agrega "Authorization: Bearer <token>" en cada llamada Feign
     * usando el flujo client_credentials del registro "servicio-envios-client".
     */
    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager manager) {
        return template -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("servicio-envios-client") // debe coincidir con application.yml
                    .principal("servicio-envios-feign") // usuario técnico cualquiera
                    .build();

            OAuth2AuthorizedClient client = manager.authorize(authorizeRequest);
            if (client == null || client.getAccessToken() == null) {
                throw new IllegalStateException("No se pudo obtener token OAuth2 para servicio-envios-client");
            }

            OAuth2AccessToken accessToken = client.getAccessToken();
            template.header("Authorization", "Bearer " + accessToken.getTokenValue());
        };
    }
}

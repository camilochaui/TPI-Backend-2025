package org.example.serviciocliente.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

// Configuración de Feign Client para logging y manejo de tokens Bearer

@Configuration
public class FeignClientConfig {

    // Configura el nivel de logging de Feign a FULL para debuggear las peticiones
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    // Interceptor para reenviar el token Bearer JWT en las peticiones Feign
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Obtener los atributos de la petición actual
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    // Obtener el request HTTP entrante
                    HttpServletRequest request = attributes.getRequest();

                    // Extraer el header "Authorization"
                    String authHeader = request.getHeader("Authorization");

                    // Si existe y es un Bearer token, reenviarlo
                    if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer ")) {
                        template.header("Authorization", authHeader);
                    }
                }
            }
        };
    }
}
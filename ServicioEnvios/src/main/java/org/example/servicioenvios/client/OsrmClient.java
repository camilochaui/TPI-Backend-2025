package org.example.servicioenvios.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class OsrmClient {

    private final WebClient webClient;
    private final String routePath;

    public OsrmClient(@Value("${osrm.base-url:http://localhost:5000}") String baseUrl,
                      @Value("${osrm.route-path:/route/v1/driving}") String routePath) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.routePath = routePath;
    }

    /**
     * Llama a OSRM y devuelve el Map con la respuesta JSON.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getRoute(double lon1, double lat1, double lon2, double lat2) {
        String coordinates = String.format("%f,%f;%f,%f", lon1, lat1, lon2, lat2);
        String uri = String.format("%s/%s", routePath, coordinates);

        log.debug("OsrmClient: requesting {}", uri);

        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(uri)
                        .queryParam("alternatives", "false")
                        .queryParam("overview", "false")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return response;
    }
}

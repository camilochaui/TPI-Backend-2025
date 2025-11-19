package org.example.servicioenvios.service;

import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.entity.Ubicacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
public class OsrmService {

    private final RestTemplate restTemplate;
    private final String osrmBaseUrl;
    private final String osrmRoutePath;

    private final String googleApiKey;
    private final String googleBaseUrl;

    public OsrmService(
            @Value("${osrm.base-url:http://localhost:5000}") String osrmBaseUrl,
            @Value("${osrm.route-path:/route/v1/driving}") String osrmRoutePath,
            @Value("${google.api.base-url:https://maps.googleapis.com}") String googleBaseUrl,
            @Value("${google.api.key:}") String googleApiKey) {
        this.restTemplate = new RestTemplate();
        this.osrmBaseUrl = osrmBaseUrl;
        this.osrmRoutePath = osrmRoutePath;
        this.googleApiKey = googleApiKey != null ? googleApiKey.trim() : "";
        this.googleBaseUrl = googleBaseUrl;
    }

    public Double calcularDistanciaEntreUbicaciones(Ubicacion origen, Ubicacion destino) {
        try {
            // Si hay API Key de Google configurada, usar Google Directions API
            if (googleApiKey != null && !googleApiKey.isEmpty()) {
                log.info("Calculando distancia con Google Maps Directions: {} -> {}",
                        origen.getDireccion(), destino.getDireccion());

                String origin = String.format("%f,%f", origen.getLatitud(), origen.getLongitud());
                String destination = String.format("%f,%f", destino.getLatitud(), destino.getLongitud());

                String url = UriComponentsBuilder.fromHttpUrl(googleBaseUrl)
                        .path("/maps/api/directions/json")
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParam("key", googleApiKey)
                        .build()
                        .toUriString();

                log.debug("Llamando a Google Directions: {}", url);

                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                try {
                    if (response != null && "OK".equals(response.get("status"))) {
                        java.util.List<Map<String, Object>> routes = (java.util.List<Map<String, Object>>) response.get("routes");
                        if (routes != null && !routes.isEmpty()) {
                            Map<String, Object> route = routes.get(0);
                            java.util.List<Map<String, Object>> legs = (java.util.List<Map<String, Object>>) route.get("legs");
                            long distanciaMetros = 0L;
                            for (Map<String, Object> leg : legs) {
                                Map<String, Object> distance = (Map<String, Object>) leg.get("distance");
                                Object val = distance.get("value");
                                if (val instanceof Number) {
                                    distanciaMetros += ((Number) val).longValue();
                                }
                            }

                            Double distanciaKm = distanciaMetros / 1000.0;
                            log.info("Distancia (Google) calculada: {} km ({} m) entre {} y {}",
                                    String.format("%.2f", distanciaKm), String.format("%d", distanciaMetros),
                                    origen.getDireccion(), destino.getDireccion());
                            return distanciaKm;
                        }
                    } else {
                        log.warn("Google Directions no devolvió OK: status={}. Response={}", response != null ? response.get("status") : null, response);
                    }
                } catch (Exception ex) {
                    log.error("Error procesando respuesta de Google Directions: {}", ex.getMessage());
                }
                log.warn("Fallo al usar Google Directions, intentando con OSRM como fallback.");
            }

            log.info("Calculando distancia OSRM: {} -> {}",
                    origen.getDireccion(), destino.getDireccion());

            // Construir URL: /route/v1/driving/lon1,lat1;lon2,lat2
            String url = UriComponentsBuilder.fromHttpUrl(osrmBaseUrl)
                    .path(osrmRoutePath)
                    .path("/{coordenadas}")
                    .queryParam("alternatives", "false")
                    .queryParam("overview", "false")
                    .buildAndExpand(buildCoordenadasString(origen, destino))
                    .toUriString();

            log.debug("Llamando a OSRM: {}", url);

            // Hacer la llamada a OSRM
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "Ok".equals(response.get("code"))) {
                Double distanciaMetros = extraerDistanciaDeRespuesta(response);
                Double distanciaKm = distanciaMetros / 1000.0;

                log.info("Distancia calculada: {} km ({} m) entre {} y {}",
                        String.format("%.2f", distanciaKm),
                        String.format("%.0f", distanciaMetros),
                        origen.getDireccion(), destino.getDireccion());

                return distanciaKm;
            } else {
                log.warn("OSRM no pudo calcular la distancia. Respuesta: {}", response);
                return calcularDistanciaHaversine(origen, destino); // Fallback
            }

        } catch (Exception e) {
            log.error("Error al calcular distancia con OSRM: {}", e.getMessage());
            return calcularDistanciaHaversine(origen, destino); // Fallback
        }
    }

    // Construye la cadena de coordenadas para OSRM
    private String buildCoordenadasString(Ubicacion origen, Ubicacion destino) {
        return String.format("%f,%f;%f,%f",
                origen.getLongitud(), origen.getLatitud(),
                destino.getLongitud(), destino.getLatitud());
    }

    // Extrae la distancia en metros de la respuesta de OSRM
    @SuppressWarnings("unchecked")
    private Double extraerDistanciaDeRespuesta(Map<String, Object> response) {
        try {
            Map<String, Object> route = ((java.util.List<Map<String, Object>>) response.get("routes")).get(0);
            return (Double) route.get("distance"); // Distancia en metros
        } catch (Exception e) {
            log.error("Error extrayendo distancia de respuesta OSRM: {}", e.getMessage());
            throw new RuntimeException("Formato de respuesta OSRM inválido");
        }
    }

    // Cálculo de distancia Haversine como fallback
    public Double calcularDistanciaHaversine(Ubicacion origen, Ubicacion destino) {
        return calcularDistanciaHaversine(
                origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud()
        );
    }

    public Double calcularDistanciaHaversine(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
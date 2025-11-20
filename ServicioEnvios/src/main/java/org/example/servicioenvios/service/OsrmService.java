package org.example.servicioenvios.service;

import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.client.OsrmClient;
import org.example.servicioenvios.entity.Ubicacion;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class OsrmService {

    private final OsrmClient osrmClient;

    public OsrmService(OsrmClient osrmClient) {
        this.osrmClient = osrmClient;
    }

    public Double calcularDistanciaEntreUbicaciones(Ubicacion origen, Ubicacion destino) {
        try {
            log.info("Calculando distancia OSRM: {} -> {}",
                    origen.getDireccion(), destino.getDireccion());

            // Llamar al cliente OSRM
            Map<String, Object> response = osrmClient.getRoute(
                    origen.getLongitud(), origen.getLatitud(),
                    destino.getLongitud(), destino.getLatitud());

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

    // Extrae la distancia en metros de la respuesta de OSRM
    @SuppressWarnings("unchecked")
    private Double extraerDistanciaDeRespuesta(Map<String, Object> response) {
        try {
            Map<String, Object> route = ((java.util.List<Map<String, Object>>) response.get("routes")).get(0);
            Object dist = route.get("distance");
            if (dist instanceof Number) {
                return ((Number) dist).doubleValue();
            }
            return Double.valueOf(dist.toString());
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
package org.example.servicioenvios.service;

import lombok.extern.slf4j.Slf4j;

// IMPORTACIONES PROPIAS DEL PROYECTO
import org.example.servicioenvios.dto.feign.CamionDTO;
import org.example.servicioenvios.dto.feign.CotizacionRequestDTO;
import org.example.servicioenvios.dto.feign.CotizacionResponseDTO;
import org.example.servicioenvios.dto.feign.DepositoDTO;
import org.example.servicioenvios.entity.Solicitud;
import org.example.servicioenvios.entity.Tramo;
import org.example.servicioenvios.entity.Ubicacion;
import org.example.servicioenvios.feign.FlotaFeignClient;
import org.example.servicioenvios.feign.TarifasFeignClient;

//IMPORTACIONES PARA SPRING
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

// IMPORTACIONES PARA JAVA
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CalcularCostosService {

    private final OsrmService osrmService;
    private final FlotaFeignClient flotaFeignClient;
    private final TarifasFeignClient tarifasFeignClient;

    // Constantes para valores por defecto en estimaciones
    private static final Float CONSUMO_DEFAULT = 0.3f;
    private static final String COMBUSTIBLE_DEFAULT = "Estandar";
    private static final Float VOLUMEN_DEFAULT = 50.0f;
    private static final Float TARIFA_GESTION_DEFAULT = 1000.0f;
    private static final double UMBRAL_ASOCIACION_KM = 10.0;


    @Autowired
    public CalcularCostosService(FlotaFeignClient flotaFeignClient,
                                 OsrmService osrmService,
                                 TarifasFeignClient tarifasFeignClient) {
        this.flotaFeignClient = flotaFeignClient;
        this.osrmService = osrmService;
        this.tarifasFeignClient = tarifasFeignClient;
    }

    public double calcularCostoTramo(double distanciaKm, double costoEstadia) {
        log.info("Calculando costo estimado para tramo de {} km con estadía de ${}", distanciaKm, costoEstadia);
        try {
            List<CotizacionRequestDTO.EstadiaDTO> estadias = new ArrayList<>();
            if (costoEstadia > 0) {
                estadias.add(CotizacionRequestDTO.EstadiaDTO.builder()
                        .idDeposito(1L)
                        .fechaEntrada(LocalDate.now().toString())
                        .fechaSalida(LocalDate.now().plusDays(1).toString())
                        .build());
            }

            CotizacionRequestDTO request = CotizacionRequestDTO.builder()
                    .consumoCamionLitroKm(CONSUMO_DEFAULT)
                    .tipoCombustible(COMBUSTIBLE_DEFAULT)
                    .distanciaTotalKm((float) distanciaKm)
                    .volumenContenedor(VOLUMEN_DEFAULT)
                    .tarifaGestion(TARIFA_GESTION_DEFAULT)
                    .cantidadTramos(1)
                    .estadias(estadias)
                    .build();

            CotizacionResponseDTO response = tarifasFeignClient.calcularTarifas(request);
            double costoTotal = response.getCostoTotal();
            log.info("Costo estimado de tramo calculado por ServicioTarifas: ${}", costoTotal);
            return costoTotal;

        } catch (Exception e) {
            log.error("Error al llamar a ServicioTarifas para estimar costo de tramo. Usando cálculo de fallback. Error: {}", e.getMessage());
            // Fallback a un cálculo simple si el servicio de tarifas falla
            double costoBase = 5000; // Costo fijo de operación
            double costoPorKm = 150; // Costo variable por km
            return costoBase + (distanciaKm * costoPorKm) + costoEstadia;
        }
    }


    // 8) CALCULAR EL COSTO TOTAL DE LA ENTREGA.
    public CotizacionResponseDTO calcularCostoSolicitud(Solicitud solicitud) {
        log.info("Calculando costo para solicitud {}", solicitud.getNumSolicitud());

        try {

            CamionDTO camionDetalle = obtenerDetalleCamion(solicitud);

            Float consumoLitroKm = (camionDetalle != null && camionDetalle.getConsumoXKm() != null)
                    ? camionDetalle.getConsumoXKm()
                    : CONSUMO_DEFAULT;

            String tipoCombustible = (camionDetalle != null)
                    ? mapearIdACategoriaCombustible(camionDetalle.getIdCombustible_ext())
                    : COMBUSTIBLE_DEFAULT;

            Double distanciaTotalDouble = calcularDistanciaTotal(solicitud);
            Float distanciaTotal = distanciaTotalDouble != null ? distanciaTotalDouble.floatValue() : 0.0f;

            List<CotizacionRequestDTO.EstadiaDTO> estadias = calcularEstadias(solicitud);

            CotizacionRequestDTO request = CotizacionRequestDTO.builder()
                    .idSolicitud(solicitud.getNumSolicitud().intValue())
                    .consumoCamionLitroKm(consumoLitroKm)
                    .tipoCombustible(tipoCombustible)
                    .distanciaTotalKm(distanciaTotal)
                    .volumenContenedor(solicitud.getVolumen().floatValue())
                    .tarifaGestion(TARIFA_GESTION_DEFAULT)
                    .cantidadTramos(solicitud.getRuta().getCantidadTramos())
                    .estadias(estadias)
                    .build();

            log.info("Enviando cálculo a ServicioTarifas: distancia={}km, tramos={}, estadias={}",
                    distanciaTotal, request.getCantidadTramos(), estadias.size());

            CotizacionResponseDTO respuesta = tarifasFeignClient.calcularTarifas(request);

            log.info("Costo calculado para solicitud {}: ${}",
                    solicitud.getNumSolicitud(), respuesta.getCostoTotal());

            return respuesta;

        } catch (Exception e) {
            log.error("Error al calcular costo para solicitud {}: {}",
                    solicitud.getNumSolicitud(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo calcular el costo, servicio de tarifas no disponible");
        }
    }

    private List<CotizacionRequestDTO.EstadiaDTO> calcularEstadias(Solicitud solicitud) {
        List<CotizacionRequestDTO.EstadiaDTO> estadias = new ArrayList<>();

        if (solicitud.getRuta() == null || solicitud.getRuta().getTramos() == null) {
            return estadias;
        }

        for (Tramo tramo : solicitud.getRuta().getTramos()) {
            Ubicacion destino = tramo.getDestino();

            if (esDeposito(destino)) {
                Long idDeposito = asociarDepositoExistente(destino);
                if (idDeposito != null) {
                    CotizacionRequestDTO.EstadiaDTO estadia = CotizacionRequestDTO.EstadiaDTO.builder()
                            .idDeposito(idDeposito)
                            .fechaEntrada(formatFecha(tramo.getFechaHoraInicioEstimada()))
                            .fechaSalida(formatFecha(tramo.getFechaHoraFinEstimada()))
                            .build();
                    estadias.add(estadia);
                } else {
                    log.warn("El destino es tipo DEPOSITO pero no coincidió con ningún ID fijo dentro del umbral de {} km.", UMBRAL_ASOCIACION_KM);
                }
            }
        }
        return estadias;
    }

    private boolean esDeposito(org.example.servicioenvios.entity.Ubicacion ubicacion) {
        return ubicacion != null &&
                ubicacion.getTipo() != null &&
                "DEPOSITO".equalsIgnoreCase(ubicacion.getTipo().getNombre());
    }

    private String formatFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return LocalDate.now().plusDays(1).toString();
        }
        return fecha.toLocalDate().toString();
    }

    public Double calcularDistanciaTotal(Solicitud solicitud) {
        if (solicitud.getRuta() == null || solicitud.getRuta().getTramos() == null) {
            log.warn("Solicitud {} no tiene ruta o tramos configurados", solicitud.getNumSolicitud());
            return 0.0;
        }

        double distanciaTotal = 0.0;
        for (Tramo tramo : solicitud.getRuta().getTramos()) {
            if (tramo.getDistanciaKmEstimada() != null) {
                distanciaTotal += tramo.getDistanciaKmEstimada();
            } else {
                Double distanciaTramo = osrmService.calcularDistanciaEntreUbicaciones(
                        tramo.getOrigen(), tramo.getDestino());
                distanciaTotal += distanciaTramo;
                tramo.setDistanciaKmEstimada(distanciaTramo);
            }
        }
        log.info("Distancia total calculada: {} km para solicitud {}",
                String.format("%.2f", distanciaTotal), solicitud.getNumSolicitud());
        return distanciaTotal;
    }

    private Long asociarDepositoExistente(org.example.servicioenvios.entity.Ubicacion ubicacion) {
        if (ubicacion == null || ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
            return null;
        }

        List<DepositoDTO> depositos = flotaFeignClient.listarDepositos();
        if (depositos == null || depositos.isEmpty()) {
            log.warn("ServicioFlota no devolvió depósitos. Imposible asociar ID.");
            return null;
        }

        Long idDepositoAsociado = null;
        double minDistanceKm = Double.MAX_VALUE;

        for (DepositoDTO deposito : depositos) {
            double distanciaKm = osrmService.calcularDistanciaHaversine(
                    ubicacion.getLatitud(), ubicacion.getLongitud(),
                    deposito.getLatitud(), deposito.getLongitud()
            );

            if (distanciaKm < minDistanceKm) {
                minDistanceKm = distanciaKm;
                idDepositoAsociado = Long.valueOf(deposito.getIdDeposito());
            }
        }

        if (idDepositoAsociado != null && minDistanceKm <= UMBRAL_ASOCIACION_KM) {
            log.info("Ubicación tipo DEPOSITO ({}) asociada dinámicamente al Depósito ID {} a {} km.",
                    ubicacion.getDireccion(), idDepositoAsociado, String.format("%.2f", minDistanceKm));
            return idDepositoAsociado;
        }

        log.warn("La ubicación DEPOSITO no está a menos de {} km de ningún depósito listado.", UMBRAL_ASOCIACION_KM);
        return null;
    }

    private CamionDTO obtenerDetalleCamion(Solicitud solicitud) {
        if (solicitud.getRuta() == null || solicitud.getRuta().getTramos().isEmpty()) {
            log.warn("Solicitud {} no tiene ruta o tramos para obtener el camión.", solicitud.getNumSolicitud());
            return null;
        }

        Tramo primerTramo = solicitud.getRuta().getTramos().get(0);
        String patente = primerTramo.getPatenteCamionExt();

        if (patente == null || patente.isEmpty()) {
            log.warn("El primer tramo de la solicitud {} no tiene patente asignada.", solicitud.getNumSolicitud());
            return null;
        }

        try {
            return flotaFeignClient.obtenerCamionPorPatente(patente);
        } catch (Exception e) {
            log.error("Error al consultar detalles del camión {} en ServicioFlota: {}", patente, e.getMessage());
            return null;
        }
    }

    private String mapearIdACategoriaCombustible(Integer idCombustibleExt) {
        if (idCombustibleExt == null) {
            return COMBUSTIBLE_DEFAULT;
        }
        return switch (idCombustibleExt) {
            case 1 -> "Economico";
            case 2 -> "Estandar";
            case 3 -> "Premium";
            default -> {
                log.warn("ID de combustible desconocido: {}. Usando categoría por defecto: {}", idCombustibleExt, COMBUSTIBLE_DEFAULT);
                yield COMBUSTIBLE_DEFAULT;
            }
        };
    }

    public Double calcularCostoRealTramo(Tramo tramo) {
        log.info("Calculando costo REAL para tramo ID: {}", tramo.getIdTramo());

        try {
            // 1. Validar que el tramo tenga los datos necesarios
            if (tramo.getDistanciaKmEstimada() == null || tramo.getDistanciaKmEstimada() <= 0) {
                log.warn("El tramo {} no tiene distancia real calculada. No se puede calcular el costo real.", tramo.getIdTramo());
                return 0.0;
            }

            // 2. Obtener la información del camión
            CamionDTO camionDetalle = obtenerDetalleCamionPorTramo(tramo);
            Float consumoLitroKm = (camionDetalle != null && camionDetalle.getConsumoXKm() != null)
                    ? camionDetalle.getConsumoXKm()
                    : CONSUMO_DEFAULT;
            String tipoCombustible = (camionDetalle != null)
                    ? mapearIdACategoriaCombustible(camionDetalle.getIdCombustible_ext())
                    : COMBUSTIBLE_DEFAULT;

            // 3. Obtener el costo de estadía
            List<CotizacionRequestDTO.EstadiaDTO> estadias = new ArrayList<>();
            Ubicacion destino = tramo.getDestino();


            if (esDeposito(destino)) {
                Long idDeposito = asociarDepositoExistente(destino);
                if (idDeposito != null) {
                    estadias.add(CotizacionRequestDTO.EstadiaDTO.builder()
                            .idDeposito(idDeposito)
                            .fechaEntrada(formatFecha(tramo.getFechaHoraInicioReal() != null ? tramo.getFechaHoraInicioReal() : tramo.getFechaHoraInicioEstimada()))
                            .fechaSalida(formatFecha(tramo.getFechaHoraFinReal() != null ? tramo.getFechaHoraFinReal() : tramo.getFechaHoraFinEstimada()))
                            .build());
                }
            }

            // 4. Preparar Request para ServicioTarifas
            Float distanciaReal = tramo.getDistanciaKmEstimada().floatValue();

            CotizacionRequestDTO request = CotizacionRequestDTO.builder()
                    .idSolicitud(tramo.getRuta().getSolicitud().getNumSolicitud().intValue())
                    .consumoCamionLitroKm(consumoLitroKm)
                    .tipoCombustible(tipoCombustible)
                    .distanciaTotalKm(distanciaReal)
                    .volumenContenedor(tramo.getRuta().getSolicitud().getVolumen().floatValue())
                    .tarifaGestion(TARIFA_GESTION_DEFAULT)
                    .cantidadTramos(1) // Siempre 1 tramo para cálculo unitario
                    .estadias(estadias)
                    .build();

            // 5. Llamar a ServicioTarifas
            CotizacionResponseDTO respuesta = tarifasFeignClient.calcularTarifas(request);

            log.info("Costo REAL calculado para tramo {} ({} km): ${}",
                    tramo.getIdTramo(), distanciaReal, respuesta.getCostoTotal());

            return respuesta.getCostoTotal();

        } catch (Exception e) {
            log.error("Error FATAL al calcular costo REAL para tramo {}: {}", tramo.getIdTramo(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Fallo al obtener costo real del ServicioTarifas.");
        }
    }


    private CamionDTO obtenerDetalleCamionPorTramo(Tramo tramo) {
        String patente = tramo.getPatenteCamionExt();

        if (patente == null || patente.isEmpty()) {
            log.warn("El tramo ID {} no tiene patente asignada para el cálculo.", tramo.getIdTramo());
            return null;
        }

        try {
            return flotaFeignClient.obtenerCamionPorPatente(patente);
        } catch (Exception e) {
            log.error("Error al consultar detalles del camión {} en ServicioFlota: {}", patente, e.getMessage());
            return null;
        }
    }

}
package org.example.servicioenvios.service;

import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.feign.DepositoDTO;
import org.example.servicioenvios.dto.response.RutaTentativaDTO;
import org.example.servicioenvios.dto.response.SolicitudResponseDTO;
import org.example.servicioenvios.dto.response.TramoResponseDTO;
import org.example.servicioenvios.dto.response.UbicacionResponseDTO;
import org.example.servicioenvios.entity.*;
import org.example.servicioenvios.feign.FlotaFeignClient;

import org.example.servicioenvios.repository.RutaRepository;
import org.example.servicioenvios.repository.SolicitudRepository;
import org.example.servicioenvios.repository.TramoRepository;
import org.example.servicioenvios.repository.TipoUbicacionRepository;
import org.example.servicioenvios.repository.UbicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RutaService {

    private final SolicitudRepository solicitudRepository;
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final TipoUbicacionRepository tipoUbicacionRepository;
    private final FlotaFeignClient flotaFeignClient;
    private final OsrmService osrmService;
    private final CalcularCostosService calcularCostosService;
    private final SolicitudService solicitudService;
    private static final double DESVIO_MAXIMO_KM = 300.0;

    @Autowired
    public RutaService(SolicitudRepository solicitudRepository,
                       RutaRepository rutaRepository,
                       TramoRepository tramoRepository,
                       UbicacionRepository ubicacionRepository,
                       TipoUbicacionRepository tipoUbicacionRepository,
                       FlotaFeignClient flotaFeignClient,
                       OsrmService osrmService,
                       CalcularCostosService calcularCostosService,
                       SolicitudService solicitudService) {
        this.solicitudRepository = solicitudRepository;
        this.rutaRepository = rutaRepository;
        this.tramoRepository = tramoRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.tipoUbicacionRepository = tipoUbicacionRepository;
        this.flotaFeignClient = flotaFeignClient;
        this.osrmService = osrmService;
        this.calcularCostosService = calcularCostosService;
        this.solicitudService = solicitudService;
    }

    @Transactional(readOnly = true)
    public List<RutaTentativaDTO> consultarRutasTentativas(Long numSolicitud) {
        Solicitud solicitud = findSolicitud(numSolicitud);

        // Obtener origen y destino desde las ubicaciones de la solicitud
        Ubicacion origen = solicitud.getUbicaciones().stream()
                .filter(u -> "CLIENTE_ORIGEN".equals(u.getTipo().getNombre()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La solicitud no tiene una ubicación de ORIGEN definida."));

        Ubicacion destino = solicitud.getUbicaciones().stream()
                .filter(u -> "CLIENTE_DESTINO".equals(u.getTipo().getNombre()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La solicitud no tiene una ubicación de DESTINO definida."));


        List<RutaTentativaDTO> rutas = new ArrayList<>();

        // 1. Ruta Directa
        double distanciaDirecta = osrmService.calcularDistanciaEntreUbicaciones(origen, destino);
        RutaTentativaDTO rutaDirecta = generarRutaDirecta(solicitud, origen, destino, distanciaDirecta);
        rutas.add(rutaDirecta);

        // 2. Buscar depósitos intermedios válidos
        List<DepositoDTO> depositosCercanos = buscarDepositosIntermedios(origen, destino, distanciaDirecta);
        
        if (!depositosCercanos.isEmpty()) {
            // 3. Ruta con 1 depósito (el más óptimo)
            DepositoDTO mejorDeposito = depositosCercanos.get(0);
            RutaTentativaDTO rutaConUnDeposito = generarRutaConDeposito(solicitud, origen, destino, mejorDeposito);
            if (rutaConUnDeposito != null) {
                rutas.add(rutaConUnDeposito);
            }

            // 4. Ruta con 2 depósitos (si hay al menos 2 candidatos)
            if (depositosCercanos.size() >= 2) {
                DepositoDTO deposito1 = depositosCercanos.get(0);
                DepositoDTO deposito2 = depositosCercanos.get(1);
                RutaTentativaDTO rutaConDosDepositos = generarRutaConDosDepositos(
                        solicitud, origen, destino, deposito1, deposito2);
                if (rutaConDosDepositos != null) {
                    rutas.add(rutaConDosDepositos);
                }
            }

            // 5. Si hay 3+ depósitos, agregar una tercera opción con el segundo mejor
            if (depositosCercanos.size() >= 3 && rutas.size() < 4) {
                DepositoDTO tercerDeposito = depositosCercanos.get(2);
                RutaTentativaDTO rutaAlternativa = generarRutaConDeposito(
                        solicitud, origen, destino, tercerDeposito);
                if (rutaAlternativa != null) {
                    rutas.add(rutaAlternativa);
                }
            }
        }

        // Asignar IDs a las rutas
        AtomicInteger contador = new AtomicInteger(1);
        rutas.forEach(ruta -> ruta.setIdRutaTentativa(contador.getAndIncrement()));

        // Ordenar por costo total estimado (más económica primero)
        rutas.sort(Comparator.comparing(RutaTentativaDTO::getCostoTotalEstimado));

        log.info("✅ Generadas {} rutas tentativas para solicitud {} (directa + {} con depósitos)", 
                rutas.size(), numSolicitud, rutas.size() - 1);
        return rutas;
    }

    @Transactional
    public SolicitudResponseDTO seleccionarRuta(Long numSolicitud, RutaTentativaDTO rutaSeleccionada) {
        Solicitud solicitud = findSolicitud(numSolicitud);

        // Validar estado de la solicitud
        if (solicitud.getEstadoSolicitud() != EstadoSolicitud.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden asignar rutas a solicitudes en estado BORRADOR");
        }

        // Limpiar ruta anterior
        Ruta rutaAntigua = solicitud.getRuta();
        if (rutaAntigua != null) {
            tramoRepository.deleteAll(rutaAntigua.getTramos());
            rutaRepository.delete(rutaAntigua);
        }

        // Crear nueva ruta
        Ruta nuevaRuta = Ruta.builder()
                .solicitud(solicitud)
                .cantidadTramos(rutaSeleccionada.getTramos().size())
                .cantidadDepositos(calcularCantidadDepositos(rutaSeleccionada))
                .tramos(new ArrayList<>())
                .build();
        Ruta rutaGuardada = rutaRepository.save(nuevaRuta);

        // Crear tramos
        List<Tramo> nuevosTramos = new ArrayList<>();
        AtomicInteger orden = new AtomicInteger(1);

        for (TramoResponseDTO tramoDTO : rutaSeleccionada.getTramos()) {
            Ubicacion origen = obtenerOCrearUbicacion(tramoDTO.getOrigen());
            Ubicacion destino = obtenerOCrearUbicacion(tramoDTO.getDestino());

            Tramo tramo = Tramo.builder()
                    .ruta(rutaGuardada)
                    .orden(orden.getAndIncrement())
                    .origen(origen)
                    .destino(destino)
                    .distanciaKmEstimada(tramoDTO.getDistanciaKmEstimada())
                    .costoEstimado(tramoDTO.getCostoEstimado())
                    .costoEstadiaDeposito(tramoDTO.getCostoEstadiaDeposito())
                    .estadoTramo(EstadoTramo.PENDIENTE)
                    .fechaHoraInicioEstimada(LocalDateTime.now().plusDays(orden.get()))
                    .fechaHoraFinEstimada(LocalDateTime.now().plusDays(orden.get() + 1))
                    .build();
            nuevosTramos.add(tramo);
        }
        tramoRepository.saveAll(nuevosTramos);
        rutaGuardada.setTramos(nuevosTramos);

        // Actualizar solicitud
        solicitud.setRuta(rutaGuardada);
        solicitud.setCostoEstimado(rutaSeleccionada.getCostoTotalEstimado());
        solicitud.setTiempoEstimado(calcularTiempoEstimado(nuevosTramos));
        solicitud.setEstadoSolicitud(EstadoSolicitud.PROGRAMADA);

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        log.info("Ruta asignada exitosamente a solicitud {}", numSolicitud);
        return solicitudService.mapToSolicitudResponse(solicitudActualizada);
    }

    /**
     * Busca depósitos intermedios válidos entre origen y destino.
     * Filtra por desvío máximo y ordena por desvío mínimo (más eficiente primero).
     */
    private List<DepositoDTO> buscarDepositosIntermedios(Ubicacion origen, Ubicacion destino, double distanciaDirecta) {
        List<DepositoCandidate> candidatos = new ArrayList<>();

        try {
            List<DepositoDTO> todosLosDepositos = flotaFeignClient.listarDepositos();
            log.info("Analizando {} depósitos para encontrar intermedios (desvío máx: {} km)", 
                    todosLosDepositos.size(), DESVIO_MAXIMO_KM);

            for (DepositoDTO deposito : todosLosDepositos) {
                // Calcular distancias usando Haversine (más rápido para filtrado inicial)
                double distOrigenDeposito = osrmService.calcularDistanciaHaversine(
                        origen.getLatitud(), origen.getLongitud(),
                        deposito.getLatitud(), deposito.getLongitud()
                );
                double distDepositoDestino = osrmService.calcularDistanciaHaversine(
                        deposito.getLatitud(), deposito.getLongitud(),
                        destino.getLatitud(), destino.getLongitud()
                );

                double distanciaTotal = distOrigenDeposito + distDepositoDestino;
                double desvio = distanciaTotal - distanciaDirecta;

                // Filtrar: debe tener desvío positivo pero dentro del máximo permitido
                if (desvio > 0 && desvio <= DESVIO_MAXIMO_KM) {
                    candidatos.add(new DepositoCandidate(deposito, desvio, distanciaTotal));
                    log.debug("   ✓ {} - Desvío: {:.1f} km, Dist Total: {:.1f} km",
                            deposito.getNombre(), desvio, distanciaTotal);
                }
            }

            if (candidatos.isEmpty()) {
                log.warn("⚠️ No se encontraron depósitos intermedios válidos (desvío máx: {} km)",
                        DESVIO_MAXIMO_KM);
                return List.of();
            }

            // Ordenar por desvío (menor primero = más eficiente)
            candidatos.sort(Comparator.comparingDouble(c -> c.desvio));

            log.info("✅ {} depósitos candidatos encontrados (ordenados por eficiencia)", candidatos.size());
            return candidatos.stream()
                    .limit(5) // Máximo 5 depósitos para evaluar
                    .map(c -> c.deposito)
                    .toList();

        } catch (Exception e) {
            log.error("❌ Error al buscar depósitos intermedios: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Clase auxiliar para ordenar depósitos candidatos por eficiencia
     */
    private static class DepositoCandidate {
        final DepositoDTO deposito;
        final double desvio;
        final double distanciaTotal;

        DepositoCandidate(DepositoDTO deposito, double desvio, double distanciaTotal) {
            this.deposito = deposito;
            this.desvio = desvio;
            this.distanciaTotal = distanciaTotal;
        }
    }

    private RutaTentativaDTO generarRutaDirecta(Solicitud solicitud, Ubicacion origen,
                                                Ubicacion destino, double distancia) {
        double costoEstadia = 0.0;
        double costoTramo = calcularCostosService.calcularCostoTramo(distancia, costoEstadia);

        TramoResponseDTO tramoUnico = TramoResponseDTO.builder()
                .origen(mapToUbicacionResponse(origen))
                .destino(mapToUbicacionResponse(destino))
                .distanciaKmEstimada(distancia)
                .costoEstimado(costoTramo)
                .costoEstadiaDeposito(costoEstadia)
                .build();

        return RutaTentativaDTO.builder()
                .descripcion("Ruta Directa")
                .distanciaTotalKm(distancia)
                .costoTotalEstimado((Double) costoTramo)
                .cantidadTramos(1)
                .tramos(List.of(tramoUnico))
                .build();
    }

    private RutaTentativaDTO generarRutaConDeposito(Solicitud solicitud, Ubicacion origen,
                                                    Ubicacion destino, DepositoDTO deposito) {
        try {
            Ubicacion ubicacionDeposito = mapDepositoToUbicacion(deposito);

            double distTramo1 = osrmService.calcularDistanciaEntreUbicaciones(origen, ubicacionDeposito);
            double distTramo2 = osrmService.calcularDistanciaEntreUbicaciones(ubicacionDeposito, destino);

            double costoEstadia = 5000.0; // Costo fijo de estadía

            double costoTramo1 = calcularCostosService.calcularCostoTramo(distTramo1, costoEstadia);
            double costoTramo2 = calcularCostosService.calcularCostoTramo(distTramo2, 0.0);

            TramoResponseDTO tramo1 = TramoResponseDTO.builder()
                    .origen(mapToUbicacionResponse(origen))
                    .destino(mapToUbicacionResponse(ubicacionDeposito))
                    .distanciaKmEstimada(distTramo1)
                    .costoEstimado(costoTramo1)
                    .costoEstadiaDeposito(costoEstadia)
                    .build();

            TramoResponseDTO tramo2 = TramoResponseDTO.builder()
                    .origen(mapToUbicacionResponse(ubicacionDeposito))
                    .destino(mapToUbicacionResponse(destino))
                    .distanciaKmEstimada(distTramo2)
                    .costoEstimado(costoTramo2)
                    .costoEstadiaDeposito(0.0)
                    .build();

            double costoTotal = costoTramo1 + costoTramo2;
            double distanciaTotal = distTramo1 + distTramo2;

            return RutaTentativaDTO.builder()
                    .descripcion("Ruta vía Depósito " + deposito.getNombre())
                    .distanciaTotalKm(distanciaTotal)
                    .costoTotalEstimado((Double) costoTotal)
                    .cantidadTramos(2)
                    .tramos(List.of(tramo1, tramo2))
                    .build();

        } catch (Exception e) {
            log.error("Error generando ruta con depósito {}: {}", deposito.getNombre(), e.getMessage());
            return null;
        }
    }

    /**
     * Genera una ruta con 2 depósitos intermedios: origen → dep1 → dep2 → destino
     */
    private RutaTentativaDTO generarRutaConDosDepositos(Solicitud solicitud, Ubicacion origen,
                                                        Ubicacion destino, 
                                                        DepositoDTO deposito1, 
                                                        DepositoDTO deposito2) {
        try {
            Ubicacion ubicDep1 = mapDepositoToUbicacion(deposito1);
            Ubicacion ubicDep2 = mapDepositoToUbicacion(deposito2);

            // Calcular distancias de cada tramo
            double distTramo1 = osrmService.calcularDistanciaEntreUbicaciones(origen, ubicDep1);
            double distTramo2 = osrmService.calcularDistanciaEntreUbicaciones(ubicDep1, ubicDep2);
            double distTramo3 = osrmService.calcularDistanciaEntreUbicaciones(ubicDep2, destino);

            double costoEstadia = 5000.0; // Costo por depósito

            // Calcular costos de cada tramo (incluye estadía en los depósitos intermedios)
            double costoTramo1 = calcularCostosService.calcularCostoTramo(distTramo1, costoEstadia);
            double costoTramo2 = calcularCostosService.calcularCostoTramo(distTramo2, costoEstadia);
            double costoTramo3 = calcularCostosService.calcularCostoTramo(distTramo3, 0.0);

            // Construir DTOs de tramos
            TramoResponseDTO tramo1 = TramoResponseDTO.builder()
                    .origen(mapToUbicacionResponse(origen))
                    .destino(mapToUbicacionResponse(ubicDep1))
                    .distanciaKmEstimada(distTramo1)
                    .costoEstimado(costoTramo1)
                    .costoEstadiaDeposito(costoEstadia)
                    .build();

            TramoResponseDTO tramo2 = TramoResponseDTO.builder()
                    .origen(mapToUbicacionResponse(ubicDep1))
                    .destino(mapToUbicacionResponse(ubicDep2))
                    .distanciaKmEstimada(distTramo2)
                    .costoEstimado(costoTramo2)
                    .costoEstadiaDeposito(costoEstadia)
                    .build();

            TramoResponseDTO tramo3 = TramoResponseDTO.builder()
                    .origen(mapToUbicacionResponse(ubicDep2))
                    .destino(mapToUbicacionResponse(destino))
                    .distanciaKmEstimada(distTramo3)
                    .costoEstimado(costoTramo3)
                    .costoEstadiaDeposito(0.0)
                    .build();

            double costoTotal = costoTramo1 + costoTramo2 + costoTramo3;
            double distanciaTotal = distTramo1 + distTramo2 + distTramo3;

            return RutaTentativaDTO.builder()
                    .descripcion(String.format("Ruta vía %s y %s", 
                            deposito1.getNombre(), deposito2.getNombre()))
                    .distanciaTotalKm(distanciaTotal)
                    .costoTotalEstimado((Double) costoTotal)
                    .cantidadTramos(3)
                    .tramos(List.of(tramo1, tramo2, tramo3))
                    .build();

        } catch (Exception e) {
            log.error("Error generando ruta con 2 depósitos ({}, {}): {}", 
                    deposito1.getNombre(), deposito2.getNombre(), e.getMessage());
            return null;
        }
    }

    private Ubicacion mapDepositoToUbicacion(DepositoDTO deposito) {
        TipoUbicacion tipoDeposito = tipoUbicacionRepository.findByNombre("DEPOSITO")
                .orElseThrow(() -> new IllegalStateException(
                        "Tipo de ubicación 'DEPOSITO' no encontrado"));

        return Ubicacion.builder()
                // .idDepositoExt(deposito.getIdDeposito())
                .direccion(deposito.getDireccion())
                .latitud(deposito.getLatitud())
                .longitud(deposito.getLongitud())
                .tipo(tipoDeposito)
                .build();
    }

    private Ubicacion obtenerOCrearUbicacion(UbicacionResponseDTO dto) {
        if (dto.getIdUbicacion() != null) {
            return ubicacionRepository.findById(dto.getIdUbicacion())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Ubicación no encontrada: " + dto.getIdUbicacion()));
        }

        TipoUbicacion tipo = tipoUbicacionRepository.findByNombre(dto.getTipoUbicacion())
                .orElseThrow(() -> new IllegalStateException(
                        "Tipo de ubicación no encontrado: " + dto.getTipoUbicacion()));

        Ubicacion nuevaUbicacion = Ubicacion.builder()
                .direccion(dto.getDireccion())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .tipo(tipo)
                .build();

        return ubicacionRepository.save(nuevaUbicacion);
    }

    private UbicacionResponseDTO mapToUbicacionResponse(Ubicacion u) {
        if (u == null) return null;
        return UbicacionResponseDTO.builder()
                .idUbicacion(u.getIdUbicacion())
                .direccion(u.getDireccion())
                .latitud(u.getLatitud())
                .longitud(u.getLongitud())
                .tipoUbicacion(u.getTipo() != null ? u.getTipo().getNombre() : "DESCONOCIDO")
                .build();
    }

    private Integer calcularCantidadDepositos(RutaTentativaDTO ruta) {
        return (int) ruta.getTramos().stream()
                .filter(t -> t.getCostoEstadiaDeposito() != null && t.getCostoEstadiaDeposito() > 0)
                .count();
    }

    private String calcularTiempoEstimado(List<Tramo> tramos) {
        int dias = tramos.size() * 2; // 2 días por tramo
        return dias + " días";
    }

    private Solicitud findSolicitud(Long numSolicitud) {
        return solicitudRepository.findByIdWithUbicacionesAndTipos(numSolicitud)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada: " + numSolicitud));
    }
}

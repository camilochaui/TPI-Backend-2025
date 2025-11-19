package org.example.servicioenvios.service;

import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.entity.EstadoSolicitud;
import org.example.servicioenvios.entity.EstadoTramo;
import org.example.servicioenvios.dto.feign.CamionDTO;
import org.example.servicioenvios.dto.feign.TransportistaDTO;
import org.example.servicioenvios.dto.request.AsignarCamionRequestDTO;
import org.example.servicioenvios.dto.response.TramoResponseDTO;
import org.example.servicioenvios.dto.response.UbicacionResponseDTO;
import org.example.servicioenvios.entity.Solicitud;
import org.example.servicioenvios.entity.Tramo;
import org.example.servicioenvios.entity.Ubicacion;
import org.example.servicioenvios.feign.FlotaFeignClient;
import org.example.servicioenvios.repository.SolicitudRepository;
import org.example.servicioenvios.repository.TramoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
// import org.springframework.security.oauth2.jwt.Jwt; // Ya no se usa
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Asociado al REQ #6, #7, #8, #9 y #11

@Slf4j
@Service
public class TramoService {

    private final TramoRepository tramoRepository;
    private final SolicitudRepository solicitudRepository;
    private final FlotaFeignClient flotaFeignClient;

    private final OsrmService osrmService;
    private final CalcularCostosService calcularCostosService;

    @Autowired
    public TramoService(TramoRepository tramoRepository,
                        SolicitudRepository solicitudRepository,
                        FlotaFeignClient flotaFeignClient,
                        OsrmService osrmService,
                        CalcularCostosService calcularCostosService
    ) {
        this.tramoRepository = tramoRepository;
        this.solicitudRepository = solicitudRepository;
        this.flotaFeignClient = flotaFeignClient;
        this.osrmService = osrmService;
        this.calcularCostosService = calcularCostosService;
    }

    // REQ 6) Asignar camión a un tramo
    @Transactional
    public TramoResponseDTO asignarCamionATramo(Long idTramo, AsignarCamionRequestDTO requestDTO) {

        String patente = requestDTO.getPatenteCamion();
        log.info("REQ #6: Asignando camión {} al tramo {}", patente, idTramo);

        // 1. Buscar el tramo
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado: " + idTramo));

        // 2. Obtener la solicitud
        Solicitud solicitud = tramo.getRuta().getSolicitud();

        // ¡NECESITAMOS ESTE DATO!
        String idContenedor = solicitud.getIdContenedorExt();
        if (idContenedor == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La solicitud no tiene un ID de contenedor.");
        }

        // 3. Orquestación: Validar camión (Esto está bien)
        CamionDTO camion;
        try {
            log.info("Validando camión {} con ServicioFlota...", patente);
            camion = flotaFeignClient.obtenerCamionPorPatente(patente);
            // ... (resto del try-catch de validación)
        } catch (Exception e) {
            log.error("Error al contactar ServicioFlota: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "ServicioFlota no disponible: " + e.getMessage());
        }

        // 4. Validación de Reglas de Negocio (Esto está bien)
        // ... (tus if de disponibilidad, peso, volumen) ...

        // 5. Orquestación: ¡VINCULAR CONTENEDOR Y ASIGNAR CAMIÓN!
        try {
            log.info("Vinculando contenedor {} a camión {} en ServicioFlota...", idContenedor, patente);

            // ¡¡¡CAMBIO CLAVE!!!
            // Ya no llamamos a 'asignarCamion', llamamos al nuevo método 'vincularContenedorACamion'
            flotaFeignClient.vincularContenedorACamion(patente, idContenedor);

        } catch (Exception e) {
            log.error("Error al VINCULAR camión en ServicioFlota (posible sobrecarga o 404): {}", e.getMessage());
            // Si esto falla (ej. por validación de carga), la transacción hace rollback
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo asignar el camión (verificar capacidad o existencia): " + e.getMessage());
        }

        // 6. Persistencia Local (Esto ahora SÍ es correcto)
        tramo.setPatenteCamionExt(patente);
        tramo.setEstadoTramo(EstadoTramo.ASIGNADO);
        Tramo tramoActualizado = tramoRepository.save(tramo);

        log.info("Camión {} asignado y vinculado exitosamente al tramo {}", patente, idTramo);
        return mapToTramoResponse(tramoActualizado);
    }


    // REQ 7) Obtener tramos de un transportista
    @Transactional(readOnly = true)
    public List<TramoResponseDTO> obtenerTramosDelTransportista(Integer transportistaId) { // <-- CAMBIADO

        // 1. Obtener el ID del Transportista (ahora viene por parámetro)
        log.warn("--- ADVERTENCIA DE SEGURIDAD (IDOR) ---");
        log.info("REQ #7 (INSEGURO): Buscando tramos para transportista ID: {}", transportistaId);

        // 2. Orquestación: Llamar a ServicioFlota para obtener los camiones de este transportista
        TransportistaDTO transportista;
        try {
            transportista = flotaFeignClient.obtenerTransportistaPorId(transportistaId);
        } catch (Exception e) {
            log.error("Error al contactar ServicioFlota para obtener transportista {}: {}", transportistaId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "ServicioFlota no disponible");
        }

        List<String> misPatentes = transportista.getCamionesPatentes();
        if (misPatentes == null || misPatentes.isEmpty()) {
            log.info("Transportista {} no tiene camiones asignados.", transportistaId);
            return List.of();
        }

        // 3. Encontrar tramos (ASIGNADOS o INICIADOS) para esas patentes
        log.info("Buscando tramos para patentes: {}", misPatentes);
        List<Tramo> tramos = tramoRepository.findTramosByTransportista(misPatentes);

        return tramos.stream()
                .map(this::mapToTramoResponse)
                .collect(Collectors.toList());
    }

    // REQ 7: Iniciar un tramo
    @Transactional
    public TramoResponseDTO iniciarTramo(Long idTramo, Integer transportistaId) { // <-- CAMBIADO
        log.warn("--- ADVERTENCIA DE SEGURIDAD (IDOR) ---");
        log.info("REQ #7 (INSEGURO): Transportista {} iniciando tramo ID: {}", transportistaId, idTramo);

        // 1. Validar Seguridad (Versión Insegura )
        Tramo tramo = validarTransportistaDueñoDelTramo(idTramo, transportistaId);

        // 2. Validación de Estado:
        if (tramo.getEstadoTramo() != EstadoTramo.ASIGNADO) {
            log.warn("Conflicto: Tramo {} no está en estado ASIGNADO. Estado actual: {}", idTramo, tramo.getEstadoTramo());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tramo no está en estado ASIGNADO (actual: " + tramo.getEstadoTramo() + ")");
        }

        // 3. Orquestación: Marcar Transportista como OCUPADO
        try {
            log.info("Marcando transportista {} como OCUPADO en ServicioFlota...", transportistaId);
            flotaFeignClient.asignarTransportista(transportistaId);
        } catch (Exception e) {
            log.error("Error al ocupar transportista en ServicioFlota: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo actualizar estado del transportista: " + e.getMessage());
        }

        // 4. Actualizar Estado del Tramo
        tramo.setEstadoTramo(EstadoTramo.INICIADO);
        tramo.setFechaHoraInicioReal(LocalDateTime.now());

        // 5. Actualizar Estado de la Solicitud (si es el primer tramo)
        Solicitud solicitud = tramo.getRuta().getSolicitud();
        if (solicitud.getEstadoSolicitud() == EstadoSolicitud.PROGRAMADA) {
            log.info("Primer tramo iniciado. Actualizando estado de Solicitud {} a EN_TRANSITO", solicitud.getNumSolicitud());
            solicitud.setEstadoSolicitud(EstadoSolicitud.EN_TRANSITO);
            solicitudRepository.save(solicitud);
        }

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo {} INICIADO exitosamente.", idTramo);
        return mapToTramoResponse(tramoActualizado);
    }

    // REQ 7) Finalizar un tramo
    @Transactional
    public TramoResponseDTO finalizarTramo(Long idTramo, Integer transportistaId) { // <-- CAMBIADO
        log.warn("--- ADVERTENCIA DE SEGURIDAD (IDOR) ---");
        log.info("REQ #7 (INSEGURO): Transportista {} finalizando tramo ID: {}", transportistaId, idTramo);

        // 1. Validar Seguridad (Versión Insegura)
        Tramo tramo = validarTransportistaDueñoDelTramo(idTramo, transportistaId);

        // 2. Validación de Estado:
        if (tramo.getEstadoTramo() != EstadoTramo.INICIADO) {
            log.warn("Conflicto: Tramo {} no está en estado INICIADO. Estado actual: {}", idTramo, tramo.getEstadoTramo());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tramo no está en estado INICIADO (actual: " + tramo.getEstadoTramo() + ")");
        }

        // 3. Actualizar Estado del Tramo
        tramo.setEstadoTramo(EstadoTramo.FINALIZADO);
        tramo.setFechaHoraFinReal(LocalDateTime.now());

        // 4. Calcular Distancia y Costo Real
        try {
            // A. Calcular Distancia Real
            log.info("REQ #8: Calculando distancia REAL para tramo {}", idTramo);
            Double distanciaReal = osrmService.calcularDistanciaEntreUbicaciones(
                    tramo.getOrigen(), tramo.getDestino());

            if (distanciaReal != null) {
                tramo.setDistanciaKmEstimada(distanciaReal);
            }

            // B. Calcular Costo Real
            log.info("REQ #9: Calculando costo REAL para tramo {}", idTramo);
            Double costoRealTramo = calcularCostosService.calcularCostoRealTramo(tramo);

            if (costoRealTramo != null) {
                tramo.setCostoReal(costoRealTramo);
            }

        } catch (Exception e) {
            log.error("Error al calcular Distancia/Costo Real para tramo {}: {}", idTramo, e.getMessage());
        }

        // 5. Orquestación: Liberar el camión en ServicioFlota
        try {
            log.info("Liberando camión {} en ServicioFlota...", tramo.getPatenteCamionExt());
            flotaFeignClient.liberarCamion(tramo.getPatenteCamionExt());
        } catch (Exception e) {
            log.error("Error al liberar camión {} en ServicioFlota: {}", tramo.getPatenteCamionExt(), e.getMessage());
        }

        // 6. Orquestación: Liberar Transportista
        try {
            log.info("Liberando transportista {} en ServicioFlota...", transportistaId);
            flotaFeignClient.liberarTransportista(transportistaId);
        } catch (Exception e) {
            log.error("Error al liberar transportista {} en ServicioFlota: {}", transportistaId, e.getMessage());
            // No detenemos la finalización, solo logueamos.
        }

        // 7. Actualizar Estado de la Solicitud (si es el último tramo)

        // Actualizar Estado de la Solicitud (si es el último tramo)
        try {
            Solicitud solicitud = tramo.getRuta().getSolicitud();
            List<Tramo> todosLosTramos = tramo.getRuta().getTramos();

            boolean todosFinalizados = todosLosTramos.stream()
                    .allMatch(t -> t.getEstadoTramo() == EstadoTramo.FINALIZADO);

            if (todosFinalizados) {
                log.info("¡TODOS LOS TRAMOS FINALIZADOS! Actualizando Solicitud {} a ENTREGADA.", solicitud.getNumSolicitud());
                solicitud.setEstadoSolicitud(EstadoSolicitud.ENTREGADA);
                solicitudRepository.save(solicitud); // Guardar el estado de la solicitud
            } else {
                long tramosFaltantes = todosLosTramos.stream()
                        .filter(t -> t.getEstadoTramo() != EstadoTramo.FINALIZADO)
                        .count();
                log.info("Tramo finalizado. Aún quedan {} tramos pendientes para la Solicitud {}.", tramosFaltantes, solicitud.getNumSolicitud());
            }
        } catch (Exception e) {
            log.error("Error al verificar el estado de la solicitud después de finalizar el tramo {}: {}", idTramo, e.getMessage());
            // No detenemos la finalización del tramo, pero logueamos el error.
        }

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo {} FINALIZADO exitosamente.", idTramo);
        return mapToTramoResponse(tramoActualizado);
    }

    // VALIDACIONES
    private Tramo validarTransportistaDueñoDelTramo(Long idTramo, Integer transportistaId) { // <-- CAMBIADO
        log.debug("Validando propiedad del tramo {} para el transportista {}...", idTramo, transportistaId);

        // 1. Obtener el ID del Transportista (desde parámetro)
        if (transportistaId == null) {
            log.warn("El ID de transportista es nulo.");
            // Esto no debería pasar si el @RequestParam es requerido, pero es buena idea validarlo.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta 'id_transportista'");
        }

        // 2. Obtener el Tramo
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado: " + idTramo));

        // 3. Validar que el tramo tenga un camión
        String patenteAsignada = tramo.getPatenteCamionExt();
        if (patenteAsignada == null) {
            log.warn("Conflicto: Tramo {} no tiene ningún camión asignado.", idTramo);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tramo aún no tiene un camión asignado.");
        }

        // 4. Orquestación: Obtener los datos del Camión desde ServicioFlota
        CamionDTO camion;
        try {
            camion = flotaFeignClient.obtenerCamionPorPatente(patenteAsignada);
        } catch (Exception e) {
            log.error("Error al validar camión {}: {}", patenteAsignada, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "ServicioFlota no disponible");
        }

        // 5. Comprueba si el ID del transportista del parámetro coincide con el ID del dueño del camión
        if (!camion.getTransportistaId().equals(transportistaId)) {
            log.warn("¡ACCESO DENEGADO! Transportista (ID de parámetro: {}) intentó operar tramo {} (Patente: {}) que pertenece a Transportista {}",
                    transportistaId, idTramo, patenteAsignada, camion.getTransportistaId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Este tramo no pertenece a sus camiones.");
        }

        log.debug("Validación exitosa: Transportista {} es dueño del camión {}", transportistaId, patenteAsignada);
        return tramo;
    }


    // --- MAPPER PRIVADO ---
    private TramoResponseDTO mapToTramoResponse(Tramo tramo) {
        return TramoResponseDTO.builder()
                .idTramo(tramo.getIdTramo())
                .orden(tramo.getOrden())
                .origen(mapToUbicacionResponse(tramo.getOrigen()))
                .destino(mapToUbicacionResponse(tramo.getDestino()))
                .estadoTramo(tramo.getEstadoTramo().name())
                .patenteCamionExt(tramo.getPatenteCamionExt())
                .distanciaKmEstimada(tramo.getDistanciaKmEstimada())
                .fechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada())
                .fechaHoraFinEstimada(tramo.getFechaHoraFinEstimada())
                .build();
    }

    private UbicacionResponseDTO mapToUbicacionResponse(Ubicacion u) {
        if (u == null) return null;
        return UbicacionResponseDTO.builder()
                .idUbicacion(u.getIdUbicacion())
                .direccion(u.getDireccion())
                .latitud(u.getLatitud())
                .longitud(u.getLongitud())
                .tipoUbicacion(u.getTipo().getNombre())
                .build();
    }
}
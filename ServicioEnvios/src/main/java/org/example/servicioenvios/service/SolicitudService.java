package org.example.servicioenvios.service;

import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.feign.*;
import org.example.servicioenvios.dto.response.SeguimientoDTO;
import org.example.servicioenvios.entity.*;
import org.example.servicioenvios.dto.request.SolicitudRequestDTO;
import org.example.servicioenvios.dto.response.SolicitudResponseDTO;
import org.example.servicioenvios.dto.response.RutaResponseDTO;
import org.example.servicioenvios.dto.response.TramoResponseDTO;
import org.example.servicioenvios.dto.response.UbicacionResponseDTO;

import org.example.servicioenvios.feign.ClienteFeignClient;
import org.example.servicioenvios.feign.FlotaFeignClient;
import org.example.servicioenvios.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteFeignClient clienteFeignClient;
    private final UbicacionRepository ubicacionRepository;
    private final TipoUbicacionRepository tipoUbicacionRepository;
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final CalcularCostosService  calcularCostosService ;
    private final FlotaFeignClient flotaFeignClient;

    @Autowired
    public SolicitudService(
            SolicitudRepository solicitudRepository,
            ClienteFeignClient clienteFeignClient,
            FlotaFeignClient flotaFeignClient,
            UbicacionRepository ubicacionRepository,
            TipoUbicacionRepository tipoUbicacionRepository,
            RutaRepository rutaRepository,
            TramoRepository tramoRepository,
            CalcularCostosService calcularCostosService) {
        this.solicitudRepository = solicitudRepository;
        this.clienteFeignClient = clienteFeignClient;
        this.ubicacionRepository = ubicacionRepository;
        this.tipoUbicacionRepository = tipoUbicacionRepository;
        this.rutaRepository = rutaRepository;
        this.tramoRepository = tramoRepository;
        this.calcularCostosService = calcularCostosService;
        this.flotaFeignClient = flotaFeignClient;
    }

    // A) REGISTRAR UNA NUEVA SOLICITUD DE TRANSPORTE DE CONTENEDOR: RegistrarNuevaSolicitud.
    @Transactional
    public SolicitudResponseDTO registrarNuevaSolicitud(SolicitudRequestDTO dto) {
        log.info("Iniciando registro de solicitud para contenedor {}", dto.getIdContenedor());

        // 1. Validar que el contenedor no esté ya en una solicitud activa
        if (solicitudRepository.findByIdContenedorExt(dto.getIdContenedor()).isPresent()) {
            log.warn("El contenedor {} ya tiene una solicitud existente.", dto.getIdContenedor());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El contenedor ya posee una solicitud activa");
        }

        // 2. Orquestación: Llamar a ServicioCliente
        ClienteRegistroRequestDTO clienteRequest = mapToClienteRegistroRequest(dto);
        ClienteInternoResponseDTO clienteRegistrado;

        try {
            log.info("Llamando a ServicioCliente para registrar/obtener DNI {}", clienteRequest.getDni());
            clienteRegistrado = clienteFeignClient.registrarOObtenerCliente(clienteRequest);
            log.info("Cliente obtenido/registrado con ID: {}", clienteRegistrado.getIdCliente());
        } catch (Exception e) {
            log.error("Error al comunicarse con ServicioCliente: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar al cliente, el servicio no está disponible.");
        }

        // 3. Crear Contenedor en ServicioFlota
        ContenedorResponseDTO contenedorCreado;
        try {
            log.info("Llamando a ServicioFlota para crear contenedor (P: {}, V: {})", dto.getPeso(), dto.getVolumen());

            contenedorRequestDTO contenedorRequest = mapToContenedorCreacionRequest(dto, clienteRegistrado.getIdCliente());

            contenedorCreado = flotaFeignClient.crearContenedor(contenedorRequest);

            log.info("Contenedor creado exitosamente con ID: {}", contenedorCreado.getIdContenedor());

        } catch (Exception e) {
            log.error("Error al comunicarse con ServicioFlota para crear contenedor: {}", e.getMessage());

            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo crear el contenedor, el servicio de Flota no está disponible.");
        }


        // 4. Crear solicitud
        Solicitud nuevaSolicitud = Solicitud.builder()
                .idClienteExt(clienteRegistrado.getIdCliente())
                .idContenedorExt(dto.getIdContenedor())
                .peso(dto.getPeso())
                .volumen(dto.getVolumen())
                .estadoSolicitud(EstadoSolicitud.BORRADOR)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Solicitud solicitudGuardada = solicitudRepository.save(nuevaSolicitud);
        log.info("Solicitud creada exitosamente con ID: {}", solicitudGuardada.getNumSolicitud());


        // 5. Crear ubicaciones (Origen y Destino)
        Ubicacion origen = crearUbicacion(
                solicitudGuardada,
                dto.getOrigenDireccion(),
                dto.getOrigenLatitud(),
                dto.getOrigenLongitud(),
                "CLIENTE_ORIGEN"
        );

        Ubicacion destino = crearUbicacion(
                solicitudGuardada,
                dto.getDestinoDireccion(),
                dto.getDestinoLatitud(),
                dto.getDestinoLongitud(),
                "CLIENTE_DESTINO"
        );

        // Retornamos el DTO de la solicitud (que ahora tiene el ID)
        Solicitud solicitudFinal = solicitudRepository.save(solicitudGuardada);


        try {
            CotizacionResponseDTO cotizacion = calcularCostosService.calcularCostoSolicitud(solicitudFinal);

            // Actualizar solicitud con costos calculados
            solicitudFinal.setCostoEstimado(cotizacion.getCostoTotal());
            solicitudFinal.setTiempoEstimado(calcularTiempoEstimado(solicitudFinal));

            solicitudRepository.save(solicitudFinal);

        } catch (Exception e) {
            log.warn("No se pudieron calcular costos para solicitud {}: {}",
                    solicitudFinal.getNumSolicitud(), e.getMessage());
            // Continuar sin costos calculados
        }

        SolicitudResponseDTO responseDTO = mapToSolicitudResponse(solicitudFinal);
        responseDTO.setCliente(clienteRegistrado);
        return responseDTO;
    }

    private Ubicacion crearUbicacion(
            Solicitud solicitud, // <-- Acepta la Solicitud
            String direccion, Double latitud, Double longitud, String tipoNombre) {

        TipoUbicacion tipo = tipoUbicacionRepository.findByNombre(tipoNombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Tipo de ubicación no encontrado: " + tipoNombre));

        Ubicacion ubicacion = Ubicacion.builder()
                .direccion(direccion)
                .latitud(latitud)
                .longitud(longitud)
                .tipo(tipo)
                .solicitud(solicitud)
                .build();

        return ubicacionRepository.save(ubicacion);
    }

    private ClienteRegistroRequestDTO mapToClienteRegistroRequest(SolicitudRequestDTO dto) {
        return ClienteRegistroRequestDTO.builder()
                .nombre(dto.getNombreCliente())
                .apellido(dto.getApellidoCliente())
                .dni(dto.getDniCliente())
                .telefono(dto.getTelefonoCliente())
                .mail(dto.getEmailCliente())
                .calle(dto.getCalleCliente())
                .altura(dto.getAlturaCliente())
                .idCiudad(dto.getIdCiudadCliente())
                .build();
    }

    SolicitudResponseDTO mapToSolicitudResponse(Solicitud entity) {
        if (entity == null) return null;

        return SolicitudResponseDTO.builder()
                .numSolicitud(entity.getNumSolicitud())
                .idContenedorExt(entity.getIdContenedorExt())
                // .idClienteExt(entity.getIdClienteExt()) // Oculto, usamos el objeto 'cliente'
                .peso(entity.getPeso())
                .volumen(entity.getVolumen())
                .estadoSolicitud(entity.getEstadoSolicitud().name())
                .fechaCreacion(entity.getFechaCreacion())

                .costoEstimado(entity.getCostoEstimado())
                .tiempoEstimado(entity.getTiempoEstimado())
                .costoReal(entity.getCostoReal())
                .tiempoReal(entity.getTiempoReal())

                .ruta(entity.getRuta() != null ? mapRutaToDTO(entity.getRuta()) : null)

                .build();
    }

    private contenedorRequestDTO mapToContenedorCreacionRequest(
            SolicitudRequestDTO dto, Long idCliente) {

        return contenedorRequestDTO.builder()
                .idContenedor(dto.getIdContenedor())
                .peso(dto.getPeso().intValue())
                .volumen(dto.getVolumen().intValue())
                .idClienteExt(idCliente.intValue())
                .build();
    }


    @Transactional(readOnly = true)
    public SeguimientoDTO consultarSeguimiento(String idContenedor) {
        Solicitud solicitud = solicitudRepository.findByIdContenedorExt(idContenedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la solicitud para el contenedor " + idContenedor));

        return SeguimientoDTO.builder()
                .idContenedor(solicitud.getIdContenedorExt())
                .estadoActual(solicitud.getEstadoSolicitud().name())
                .costoEstimado(solicitud.getCostoEstimado())
                .tiempoEstimado(solicitud.getTiempoEstimado())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> obtenerSolicitudes(EstadoSolicitud estadoFiltro) {
        log.info("Obteniendo solicitudes con filtro: {}", estadoFiltro);

        List<Solicitud> solicitudes;

        if (estadoFiltro != null) {
            solicitudes = solicitudRepository.findByEstadoSolicitud(estadoFiltro);
        } else {
            solicitudes = solicitudRepository.findAll();
        }

        log.info("Encontradas {} solicitudes", solicitudes.size());

        // Convertir a DTOs
        return solicitudes.stream()
                .map(this::mapToSolicitudResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerSolicitudPorId(Long numSolicitud) {
        log.info("Buscando solicitud con ID: {}", numSolicitud);

        Solicitud solicitud = solicitudRepository.findById(numSolicitud)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la solicitud con ID: " + numSolicitud));

        SolicitudResponseDTO responseDTO = mapToSolicitudResponse(solicitud);


        try {
             ClienteInternoResponseDTO clienteInfo = clienteFeignClient.obtenerClientePorId(solicitud.getIdClienteExt());
             responseDTO.setCliente(clienteInfo);

        } catch (Exception e) {
            log.warn("No se pudo obtener la información del cliente {}: {}", solicitud.getIdClienteExt(), e.getMessage());
            responseDTO.setCliente(ClienteInternoResponseDTO.builder()
                    .idCliente(solicitud.getIdClienteExt())
                    .build());
        }

        return responseDTO;
    }

    // Devuelve la entidad JPA
    @Transactional(readOnly = true)
    public Solicitud obtenerSolicitudEntityPorId(Long numSolicitud) {
        log.info("Buscando entidad Solicitud con ID: {}", numSolicitud);

        return solicitudRepository.findById(numSolicitud)
                .orElseThrow(() -> {
                    log.warn("Solicitud no encontrada con ID: {}", numSolicitud);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No se encontró la solicitud con ID: " + numSolicitud);
                });
    }

    @Transactional
    public SolicitudResponseDTO cambiarEstadoSolicitud(Long numSolicitud, EstadoSolicitud nuevoEstado) {
        log.info("Solicitud {} - Cambiando estado a: {}", numSolicitud, nuevoEstado);

        // 1. Buscar la solicitud
        Solicitud solicitud = solicitudRepository.findById(numSolicitud)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"));

        // 2. Validar transición de estado
        validarTransicionEstado(solicitud.getEstadoSolicitud(), nuevoEstado);

        // 3. Guardar estado anterior para log
        EstadoSolicitud estadoAnterior = solicitud.getEstadoSolicitud();

        // 4. Actualizar estado
        solicitud.setEstadoSolicitud(nuevoEstado);
        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        log.info("Solicitud {} - Estado cambiado: {} -> {}",
                numSolicitud, estadoAnterior, nuevoEstado);

        return mapToSolicitudResponse(solicitudActualizada);
    }


    private void validarTransicionEstado(EstadoSolicitud estadoActual, EstadoSolicitud nuevoEstado) {
        // No se puede modificar solicitudes finalizadas o canceladas
        if (estadoActual == EstadoSolicitud.ENTREGADA || estadoActual == EstadoSolicitud.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede modificar una solicitud en estado: " + estadoActual);
        }
        // Validaciones específicas de transición
        if (estadoActual == EstadoSolicitud.BORRADOR) {
            // De BORRADOR solo se puede pasar a PROGRAMADA o CANCELADA
            if (nuevoEstado != EstadoSolicitud.PROGRAMADA && nuevoEstado != EstadoSolicitud.CANCELADA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "De BORRADOR solo se puede pasar a PROGRAMADA o CANCELADA");
            }
        }
        // Puedes agregar más validaciones según tu lógica de negocio
        log.debug("Transición válida: {} -> {}", estadoActual, nuevoEstado);
    }

    private String calcularTiempoEstimado(Solicitud solicitud) {
        // Lógica simple para calcular tiempo estimado
        int diasEstimados = solicitud.getRuta().getCantidadTramos() * 2; // 2 días por tramo
        return diasEstimados + " días";
    }

    private RutaResponseDTO mapRutaToDTO(Ruta ruta) {
        if (ruta == null) {
            return null;
        }
        return RutaResponseDTO.builder()
                .idRuta(ruta.getIdRuta())
                .cantidadTramos(ruta.getCantidadTramos())
                .cantidadDepositos(ruta.getCantidadDepositos())
                .tramos(
                        (ruta.getTramos() != null) ?
                                ruta.getTramos().stream()
                                        .map(this::mapTramoToDTO)
                                        .collect(Collectors.toList()) :
                                new ArrayList<>()
                )
                .build();
    }

    private TramoResponseDTO mapTramoToDTO(Tramo tramo) {
        if (tramo == null) {
            return null;
        }
        return TramoResponseDTO.builder()
                .idTramo(tramo.getIdTramo())
                .orden(tramo.getOrden())
                .origen(mapUbicacionToDTO(tramo.getOrigen()))
                .destino(mapUbicacionToDTO(tramo.getDestino()))
                .estadoTramo(tramo.getEstadoTramo() != null ? tramo.getEstadoTramo().name() : null)
                .patenteCamionExt(tramo.getPatenteCamionExt())
                .distanciaKmEstimada(tramo.getDistanciaKmEstimada())
                .costoEstimado(tramo.getCostoEstimado())
                .costoEstadiaDeposito(tramo.getCostoEstadiaDeposito())
                .fechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada())
                .fechaHoraFinEstimada(tramo.getFechaHoraFinEstimada())
                .fechaHoraInicioReal(tramo.getFechaHoraInicioReal())
                .fechaHoraFinReal(tramo.getFechaHoraFinReal())
                .build();
    }

    private UbicacionResponseDTO mapUbicacionToDTO(Ubicacion u) {
        if (u == null) return null;
        return UbicacionResponseDTO.builder()
                .idUbicacion(u.getIdUbicacion())
                .direccion(u.getDireccion())
                .latitud(u.getLatitud())
                .longitud(u.getLongitud())
                .tipoUbicacion(u.getTipo() != null ? u.getTipo().getNombre() : "DESCONOCIDO")
                .build();
    }

    @Transactional
    public SolicitudResponseDTO finalizarSolicitud(Long numSolicitud) {
        log.info("Iniciando finalización de solicitud {}", numSolicitud);

        // 1. Obtener solicitud y validar que existe
        Solicitud solicitud = solicitudRepository.findById(numSolicitud)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada con ID: " + numSolicitud));

        // 2. Validar que tiene ruta asignada
        if (solicitud.getRuta() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud no tiene una ruta asignada");
        }

        List<Tramo> tramos = solicitud.getRuta().getTramos();
        if (tramos == null || tramos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud no tiene tramos para calcular");
        }

        // 3. Validar que todos los tramos están finalizados
        boolean todosFinalizados = tramos.stream()
                .allMatch(t -> t.getFechaHoraInicioReal() != null && t.getFechaHoraFinReal() != null);

        if (!todosFinalizados) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No todos los tramos han sido finalizados (faltan fechas reales)");
        }

        // 4. Calcular costo real sumando costoReal de todos los tramos
        Double costoRealTotal = tramos.stream()
                .map(Tramo::getCostoReal)
                .filter(costo -> costo != null)
                .reduce(0.0, Double::sum);

        // 5. Calcular tiempo real total
        LocalDateTime inicioReal = tramos.stream()
                .map(Tramo::getFechaHoraInicioReal)
                .filter(fecha -> fecha != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime finReal = tramos.stream()
                .map(Tramo::getFechaHoraFinReal)
                .filter(fecha -> fecha != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        String tiempoReal = null;
        if (inicioReal != null && finReal != null) {
            long dias = java.time.Duration.between(inicioReal, finReal).toDays();
            long horas = java.time.Duration.between(inicioReal, finReal).toHours() % 24;
            long minutos = java.time.Duration.between(inicioReal, finReal).toMinutes() % 60;
            tiempoReal = String.format("%d días, %d horas, %d minutos", dias, horas, minutos);
        }

        // 6. Actualizar solicitud
        solicitud.setCostoReal(costoRealTotal);
        solicitud.setTiempoReal(tiempoReal);
        solicitud.setEstadoSolicitud(EstadoSolicitud.FINALIZADA);

        Solicitud solicitudFinalizada = solicitudRepository.save(solicitud);

        log.info("Solicitud {} finalizada exitosamente. Costo real: ${}, Tiempo real: {}",
                numSolicitud, costoRealTotal, tiempoReal);

        return mapToSolicitudResponse(solicitudFinalizada);
    }

}
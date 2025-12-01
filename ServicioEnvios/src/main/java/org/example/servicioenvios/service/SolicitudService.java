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
        log.info("Iniciando registro de solicitud para contenedor nuevo");

        // 1. Generar ID de contenedor único: nombreCliente + apellidoCliente + índice
        String idContenedor = generarIdContenedor(dto.getNombreCliente(), dto.getApellidoCliente());
        log.info("ID de contenedor generado: {}", idContenedor);

        // 2. Registrar/obtener cliente
        ClienteRegistroRequestDTO clienteRequest = mapToClienteRegistroRequest(dto);
        ClienteInternoResponseDTO clienteRegistrado;
        try {
            clienteRegistrado = clienteFeignClient.registrarOObtenerCliente(clienteRequest);
        } catch (Exception e) {
            log.error("Error al llamar a ServicioCliente: {} - {}", e.getClass().getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo validar al cliente, el servicio no está disponible. Error: " + e.getMessage());
        }

        // 3. Crear contenedor en ServicioFlota
        ContenedorResponseDTO contenedorCreado;
        try {
            contenedorCreado = flotaFeignClient.crearContenedor(
                    mapToContenedorCreacionRequest(dto, clienteRegistrado.getIdCliente(), idContenedor));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo crear el contenedor, el servicio de Flota no está disponible.");
        }

        // 4. Crear solicitud
        Solicitud nuevaSolicitud = Solicitud.builder()
                .idClienteExt(clienteRegistrado.getIdCliente())
                .idContenedorExt(idContenedor)
                .peso(dto.getPeso())
                .volumen(dto.getVolumen())
                .estadoSolicitud(EstadoSolicitud.BORRADOR)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Solicitud solicitudGuardada = solicitudRepository.save(nuevaSolicitud);

        // 5. Crear ubicaciones
        Ubicacion origen = crearUbicacion(solicitudGuardada, dto.getOrigenDireccion(),
                dto.getOrigenLatitud(), dto.getOrigenLongitud(), "CLIENTE-ORIGEN");
        Ubicacion destino = crearUbicacion(solicitudGuardada, dto.getDestinoDireccion(),
                dto.getDestinoLatitud(), dto.getDestinoLongitud(), "CLIENTE-DESTINO");

        // 6. No crear ruta ni tramos en esta etapa; el administrador los asignará luego
        log.info("Solicitud {} creada sin ruta asignada (BORRADOR). El administrador definirá la ruta luego.",
                solicitudGuardada.getNumSolicitud());

        // 7. Mapear a DTO de respuesta (sin ruta ni tramos por ahora)
        SolicitudResponseDTO responseDTO = mapToSolicitudResponse(solicitudGuardada);
        responseDTO.setCliente(clienteRegistrado);

        return responseDTO;
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Ubicacion crearUbicacion(
            Solicitud solicitud, // <-- Acepta la Solicitud
            String direccion, Double latitud, Double longitud, String tipoNombre) {

        // Preservar el formato con guiones tal como está en la BD.
        // Normalizamos espacios y mayúsculas para hacer la búsqueda más robusta.
        String tipoNombreDb = tipoNombre != null ? tipoNombre.trim().toUpperCase() : null;
        TipoUbicacion tipo = tipoUbicacionRepository.findByNombre(tipoNombreDb)
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
            SolicitudRequestDTO dto, Long idCliente, String idContenedor) {

        return contenedorRequestDTO.builder()
                .idContenedor(idContenedor)
                .peso(dto.getPeso().intValue())
                .volumen(dto.getVolumen().intValue())
                .idClienteExt(idCliente.intValue())
                .build();
    }

//  ID único de contenedor en formato: NombreApellido-índice
    private String generarIdContenedor(String nombre, String apellido) {
        // Normalizar nombre y apellido
        String baseId = normalizarTexto(nombre) + normalizarTexto(apellido);
        
        // Buscar contenedores con este prefijo
        long count = solicitudRepository.countByIdContenedorExtStartingWith(baseId);
        
        // Generar ID con índice incrementado
        return baseId + "-" + (count + 1);
    }

//     Normaliza texto eliminando espacios, tildes y caracteres especiales
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        
        return texto.trim()
                .replaceAll("\\s+", "")
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ÁÀÄÂ]", "A")
                .replaceAll("[ÉÈËÊ]", "E")
                .replaceAll("[ÍÌÏÎ]", "I")
                .replaceAll("[ÓÒÖÔ]", "O")
                .replaceAll("[ÚÙÜÛ]", "U")
                .replaceAll("[^a-zA-Z0-9]", "");
    }


    @Transactional(readOnly = true)
    public SeguimientoDTO consultarSeguimiento(String idContenedor) {
        Solicitud solicitud = solicitudRepository.findByIdContenedorExt(idContenedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la solicitud para el contenedor " + idContenedor));

        // Construir cronología de eventos basada en la información disponible
        java.util.List<org.example.servicioenvios.dto.response.EventoSeguimientoDTO> eventos = new java.util.ArrayList<>();

        // 1) Evento de creación de la solicitud
        if (solicitud.getFechaCreacion() != null) {
            eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                    .tipo("SOLICITUD_CREADA")
                    .descripcion("Solicitud creada")
                    .fecha(solicitud.getFechaCreacion())
                    .build());
        }

        // 2) Eventos por cada tramo (estimados y reales, asignaciones)
        if (solicitud.getRuta() != null && solicitud.getRuta().getTramos() != null) {
            for (org.example.servicioenvios.entity.Tramo tramo : solicitud.getRuta().getTramos()) {
                String baseDesc = String.format("Tramo %d (origen: %s -> destino: %s)",
                        tramo.getOrden(),
                        tramo.getOrigen() != null ? tramo.getOrigen().getDireccion() : "-",
                        tramo.getDestino() != null ? tramo.getDestino().getDireccion() : "-");

                // Asignación de camión (si existe patente)
                if (tramo.getPatenteCamionExt() != null) {
                    eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                            .tipo("TRAMO_ASIGNADO")
                            .descripcion(baseDesc + " - Camión asignado: " + tramo.getPatenteCamionExt())
                            .fecha(tramo.getFechaHoraInicioEstimada() != null ? tramo.getFechaHoraInicioEstimada() : solicitud.getFechaCreacion())
                            .build());
                }

                // Fecha estimada inicio
                if (tramo.getFechaHoraInicioEstimada() != null) {
                    eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                            .tipo("TRAMO_INICIO_ESTIMADO")
                            .descripcion(baseDesc + " - Inicio estimado")
                            .fecha(tramo.getFechaHoraInicioEstimada())
                            .build());
                }

                // Fecha estimada fin
                if (tramo.getFechaHoraFinEstimada() != null) {
                    eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                            .tipo("TRAMO_FIN_ESTIMADO")
                            .descripcion(baseDesc + " - Fin estimado")
                            .fecha(tramo.getFechaHoraFinEstimada())
                            .build());
                }

                // Fecha inicio real
                if (tramo.getFechaHoraInicioReal() != null) {
                    eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                            .tipo("TRAMO_INICIO_REAL")
                            .descripcion(baseDesc + " - Inicio real")
                            .fecha(tramo.getFechaHoraInicioReal())
                            .build());
                }

                // Fecha fin real
                if (tramo.getFechaHoraFinReal() != null) {
                    eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                            .tipo("TRAMO_FIN_REAL")
                            .descripcion(baseDesc + " - Fin real")
                            .fecha(tramo.getFechaHoraFinReal())
                            .build());
                }
            }
        }

        // 3) Estado actual de la solicitud como evento final de referencia
        eventos.add(org.example.servicioenvios.dto.response.EventoSeguimientoDTO.builder()
                .tipo("ESTADO_ACTUAL")
                .descripcion("Estado actual de la solicitud: " + solicitud.getEstadoSolicitud().name())
                .fecha(java.time.LocalDateTime.now())
                .build());

        // Ordenar cronología por fecha ascendente (nulos al final)
        eventos.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) return 0;
            if (a.getFecha() == null) return 1;
            if (b.getFecha() == null) return -1;
            return a.getFecha().compareTo(b.getFecha());
        });

        return SeguimientoDTO.builder()
                .idContenedor(solicitud.getIdContenedorExt())
                .estadoActual(solicitud.getEstadoSolicitud().name())
                .costoEstimado(solicitud.getCostoEstimado())
                .tiempoEstimado(solicitud.getTiempoEstimado())
                .eventos(eventos)
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
                // Estimación en base a distancia total y velocidad promedio
                if (solicitud.getRuta() == null || solicitud.getRuta().getTramos() == null || solicitud.getRuta().getTramos().isEmpty()) {
                        return "0 días";
                }

                // Configuración básica: velocidad promedio del camión (km/h) y tiempo fijo por depósito/entrega (horas)
                double velocidadPromedioKmH = 60.0; // configurable según negocio
                double tiempoManejoPorParadaHoras = 2.0; // carga/descarga, papeleo, etc.

                double distanciaTotalKm = 0.0;
                int paradas = 0;
                for (Tramo tramo : solicitud.getRuta().getTramos()) {
                        if (tramo.getDistanciaKmEstimada() != null) {
                                distanciaTotalKm += tramo.getDistanciaKmEstimada();
                        }
                        // Consideramos una parada por tramo (puede ajustarse si hay depósitos intermedios)
                        paradas++;
                }

                // Tiempo de viaje en horas
                double horasViaje = distanciaTotalKm / velocidadPromedioKmH;
                // Tiempo de paradas en horas
                double horasParadas = paradas * tiempoManejoPorParadaHoras;

                double horasTotales = horasViaje + horasParadas;

                long dias = (long) Math.floor(horasTotales / 24.0);
                long horas = (long) Math.floor(horasTotales % 24.0);
                long minutos = (long) Math.round((horasTotales * 60) % 60);

                return String.format("%d días, %d horas, %d minutos", dias, horas, minutos);
        }

    private RutaResponseDTO mapRutaToDTO(Ruta ruta) {
        if (ruta == null) {
            return null;
        }
                // Calcular distancia total estimada sumando distancias de los tramos
                double distanciaTotal = 0.0;
                List<Tramo> tramos = ruta.getTramos();
                if (tramos != null) {
                        for (Tramo t : tramos) {
                                if (t.getDistanciaKmEstimada() != null) {
                                        distanciaTotal += t.getDistanciaKmEstimada();
                                }
                        }
                }

                // Si no hay tramos, devolvemos null en lugar de lista vacía para evitar "tramos": []
                List<TramoResponseDTO> tramosDTO = null;
                if (tramos != null && !tramos.isEmpty()) {
                        tramosDTO = tramos.stream()
                                        .map(this::mapTramoToDTO)
                                        .collect(Collectors.toList());
                }

                return RutaResponseDTO.builder()
                .idRuta(ruta.getIdRuta())
                .cantidadTramos(ruta.getCantidadTramos())
                .cantidadDepositos(ruta.getCantidadDepositos())
                                .distanciaTotalKmEstimada(distanciaTotal > 0.0 ? distanciaTotal : null)
                                .tramos(tramosDTO)
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
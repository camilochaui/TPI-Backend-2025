package org.example.servicioenvios.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.servicioenvios.dto.request.SolicitudRequestDTO;
import org.example.servicioenvios.dto.response.SolicitudResponseDTO;
import org.example.servicioenvios.dto.response.SeguimientoDTO;
import org.example.servicioenvios.entity.EstadoSolicitud;
import org.example.servicioenvios.entity.Solicitud;
import org.example.servicioenvios.service.SolicitudService;
import org.example.servicioenvios.dto.feign.CotizacionResponseDTO;
import org.example.servicioenvios.service.CalcularCostosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/envios")
@Tag(name = "Gestión de Solicitudes", description = "Endpoints para crear y consultar solicitudes de envío")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final CalcularCostosService calcularCostosService;

    @Autowired
    public SolicitudController(SolicitudService solicitudService, CalcularCostosService calcularCostosService) {
        this.solicitudService = solicitudService;
        this.calcularCostosService = calcularCostosService;
    }

    @PostMapping
    @Operation(summary = "Registrar una nueva solicitud de envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<SolicitudResponseDTO> registrarNuevaSolicitud(
            @Valid @RequestBody SolicitudRequestDTO dto) {
        log.info("Recibida nueva solicitud de envío para contenedor {}", dto.getIdContenedor());
        try {
            SolicitudResponseDTO respuesta = solicitudService.registrarNuevaSolicitud(dto);
            log.info("Solicitud creada exitosamente con ID: {}", respuesta.getNumSolicitud());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (Exception e) {
            log.error("Error al crear solicitud para contenedor {}: {}", dto.getIdContenedor(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{idContenedor}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<SeguimientoDTO> obtenerSeguimiento(
            @PathVariable String idContenedor) {
        log.info("Recibida consulta de seguimiento interna para contenedor {}", idContenedor);
        try {
            SeguimientoDTO seguimiento = solicitudService.consultarSeguimiento(idContenedor);
            log.info("Seguimiento encontrado para contenedor {} - Estado: {}", idContenedor, seguimiento.getEstadoActual());
            return ResponseEntity.ok(seguimiento);
        } catch (Exception e) {
            log.warn("No se encontró seguimiento para contenedor {}: {}", idContenedor, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Obtener listado de solicitudes")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado) {
        log.info("Consultando solicitudes con filtro: {}", estado);
        List<SolicitudResponseDTO> solicitudes = solicitudService.obtenerSolicitudes(estado);
        return ResponseEntity.ok(solicitudes);
    }


    @Operation(summary = "Obtener solicitud por ID")
    @GetMapping("/{numSolicitud}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitud(@PathVariable Long numSolicitud) {
        log.info("Consultando solicitud ID: {}", numSolicitud);
        SolicitudResponseDTO solicitud = solicitudService.obtenerSolicitudPorId(numSolicitud);
        return ResponseEntity.ok(solicitud);
    }


    @Operation(summary = "Actualizar estado de una solicitud")
    @PutMapping("/{numSolicitud}/estado")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable Long numSolicitud,
            @RequestParam EstadoSolicitud nuevoEstado) {
        log.info("Cambiando estado de solicitud {} a {}", numSolicitud, nuevoEstado);
        SolicitudResponseDTO respuesta = solicitudService.cambiarEstadoSolicitud(numSolicitud, nuevoEstado);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/{numSolicitud}/calculo")
    @Operation(summary = "Calcular costo de una solicitud")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Costo calculado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")

    public ResponseEntity<CotizacionResponseDTO> calcularCostoSolicitud(@PathVariable Long numSolicitud) {
        log.info("Calculando costo para solicitud {}", numSolicitud);

        try {
            // 1. Obtener Solicitud: Se usa el numSolicitud desde el endpoint.
            Solicitud solicitud = solicitudService.obtenerSolicitudEntityPorId(numSolicitud);

            // 2. Calcular costo: Se lleva el objeto Solicitud como parametro.
            CotizacionResponseDTO cotizacion = calcularCostosService.calcularCostoSolicitud(solicitud);
            log.info("Costo calculado para solicitud {}: ${}", numSolicitud, cotizacion.getCostoTotal());
            return ResponseEntity.ok(cotizacion);
        } catch (Exception e) {
            log.error("Error al calcular costo para solicitud {}: {}", numSolicitud, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{numSolicitud}/distancia-total")
    @Operation(summary = "Obtener distancia total de todos los tramos de una solicitud")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Distancia total calculada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<Map<String, Object>> obtenerDistanciaTotal(@PathVariable Long numSolicitud) {
        log.info("Calculando distancia total para solicitud {}", numSolicitud);

        try {
            Solicitud solicitud = solicitudService.obtenerSolicitudEntityPorId(numSolicitud);
            Double distanciaTotal = calcularCostosService.calcularDistanciaTotal(solicitud);

            Map<String, Object> response = new HashMap<>();
            response.put("numSolicitud", numSolicitud);
            response.put("idContenedor", solicitud.getIdContenedorExt());
            response.put("distanciaTotalKm", Math.round(distanciaTotal * 100.0) / 100.0);
            response.put("cantidadTramos", solicitud.getRuta().getTramos().size());
            response.put("mensaje", "Distancia total de todos los tramos calculada");

            log.info("Distancia total calculada para solicitud {}: {} km", numSolicitud, distanciaTotal);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al calcular distancia total para solicitud {}: {}", numSolicitud, e.getMessage());
            throw e;
        }
    }

}


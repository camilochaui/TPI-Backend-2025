package org.example.serviciocliente.service;

import lombok.extern.slf4j.Slf4j;
import org.example.serviciocliente.dto.feign.SeguimientoDTO;
import org.example.serviciocliente.feign.EnvioFeignCliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Servicio de Integración u Orquestación.
 * Responsabilidad: Coordinar acciones que involucran a este microservicio
 * (ClienteService) y a otros (EnvioFeignClient).
 *
 * Cumple con los requisitos 2 y 3 del rol "Cliente".
 */
@Slf4j
@Service
public class SolicitudClienteService {

    private final ClienteService clienteService; // Servicio de Dominio (Local)
    private final EnvioFeignCliente envioFeignClient; // Cliente Feign (Remoto)

    @Autowired
    public SolicitudClienteService(ClienteService clienteService, EnvioFeignCliente envioFeignCliente) {
        this.clienteService = clienteService;
        this.envioFeignClient = envioFeignCliente;
    }

    // =====================================================
    // === REQ 2 y 3: CONSULTAR SEGUIMIENTO COMPLETO ======
    // =====================================================

    /**
     * Consulta la información de seguimiento unificada (estado, costo, tiempo)
     * para un contenedor específico, validando que la consulta la hace un cliente válido.
     */
    public SeguimientoDTO consultarSeguimiento(Long idCliente, String idContenedor) {
        log.info("[ORQUESTADOR] Iniciando consulta de seguimiento del contenedor {} para cliente {}", idContenedor, idCliente);

        // 1. Usa el servicio de dominio para validar que el cliente existe
        clienteService.findClienteById(idCliente);
        log.info("[ORQUESTADOR] Cliente {} validado. Procediendo a consultar 'servicio-envios'.", idCliente);

        // 2. Llama al microservicio externo
        try {
            SeguimientoDTO seguimiento = envioFeignClient.obtenerSeguimiento(idContenedor);
            log.info("[ORQUESTADOR] Seguimiento recibido para {}: {}", idContenedor, seguimiento.getEstadoActual());
            return seguimiento;

        } catch (Exception e) {
            log.error("[ORQUESTADOR] Error al obtener el seguimiento desde 'servicio-envios': {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error al consultar el servicio de seguimiento.");
        }
    }

}
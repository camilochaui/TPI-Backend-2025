package org.example.serviciocliente.service;

import lombok.extern.slf4j.Slf4j;
import org.example.serviciocliente.dto.ClienteRequestDTO;
import org.example.serviciocliente.dto.ClienteResponseDTO;
import org.example.serviciocliente.dto.CiudadDTO;
import org.example.serviciocliente.dto.ProvinciaDTO;
import org.example.serviciocliente.entity.ClienteEntity;
import org.example.serviciocliente.entity.CiudadEntity;
import org.example.serviciocliente.entity.ProvinciaEntity;
import org.example.serviciocliente.repository.ClienteRepository;
import org.example.serviciocliente.repository.CiudadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de Dominio (Core) para la gestión de Clientes.
 * Responsabilidad: Lógica de negocio pura de Clientes (CRUD, validaciones de negocio).
 * NO conoce a otros microservicios.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CiudadRepository ciudadRepository;

    @Autowired
    public ClienteService(
            ClienteRepository clienteRepository,
            CiudadRepository ciudadRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.ciudadRepository = ciudadRepository;
    }

    // =====================================================
    // =============== REGISTRO DE CLIENTE =================
    // =====================================================
    @Transactional
    public ClienteResponseDTO registrarCliente(ClienteRequestDTO dto) {
        log.info("Iniciando registro de cliente con DNI: {}", dto.getDni());

        // 1. Validación de Reglas de Negocio
        if (clienteRepository.existsByDni(dto.getDni())) {
            log.warn("Cliente con DNI {} ya existe.", dto.getDni());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con el DNI proporcionado");
        }
        if (clienteRepository.existsByMail(dto.getMail())) {
            log.warn("Cliente con email {} ya existe.", dto.getMail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con el email proporcionado");
        }

        // 2. Obtener Entidades Relacionadas
        CiudadEntity ciudad = ciudadRepository.findById(dto.getIdCiudad())
                .orElseThrow(() -> {
                    log.error("Ciudad con ID {} no encontrada.", dto.getIdCiudad());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ciudad no encontrada");
                });

        // 3. Mapear y Guardar
        ClienteEntity cliente = mapToEntity(dto, ciudad);
        clienteRepository.save(cliente);

        log.info("Cliente {} {} registrado correctamente con ID {}", cliente.getNombre(), cliente.getApellido(), cliente.getIdCliente());
        return mapToResponseDTO(cliente);
    }

    // =====================================================
    // =============== EDICIÓN DE CLIENTE ==================
    // =====================================================
    @Transactional
    public ClienteResponseDTO editarCliente(Long idCliente, ClienteRequestDTO dto) {
        log.info("Iniciando edición del cliente con ID {}", idCliente);

        // 1. Buscar Cliente Existente
        ClienteEntity clienteExistente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> {
                    log.error("Cliente con ID {} no encontrado.", idCliente);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
                });

        // 2. Validación de Reglas de Negocio (Conflictos)
        // Revisa si el NUEVO DNI ya pertenece a OTRO cliente
        clienteRepository.findByDni(dto.getDni()).ifPresent(cliente -> {
            if (!cliente.getIdCliente().equals(idCliente)) {
                log.warn("El DNI {} ya pertenece a otro cliente (ID {}).", dto.getDni(), cliente.getIdCliente());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está en uso por otro cliente");
            }
        });

        // Revisa si el NUEVO Email ya pertenece a OTRO cliente
        // (Aquí asumimos que el repo tiene `findByMail`)
        // clienteRepository.findByMail(dto.getMail()).ifPresent(cliente -> ... );


        // 3. Obtener Entidades Relacionadas
        CiudadEntity ciudad = ciudadRepository.findById(dto.getIdCiudad())
                .orElseThrow(() -> {
                    log.error("Ciudad con ID {} no encontrada.", dto.getIdCiudad());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ciudad no encontrada");
                });

        // 4. Actualizar y Guardar
        clienteExistente.setNombre(dto.getNombre());
        clienteExistente.setApellido(dto.getApellido());
        clienteExistente.setDni(dto.getDni());
        clienteExistente.setTelefono(dto.getTelefono());
        clienteExistente.setMail(dto.getMail());
        clienteExistente.setCalle(dto.getCalle());
        clienteExistente.setAltura(dto.getAltura());
        clienteExistente.setCiudad(ciudad);

        clienteRepository.save(clienteExistente);
        log.info("Cliente con ID {} actualizado correctamente.", idCliente);

        return mapToResponseDTO(clienteExistente);
    }

    // =====================================================
    // =============== ELIMINACIÓN DE CLIENTE ==============
    // =====================================================
    @Transactional
    public void eliminarCliente(Long idCliente) {
        log.info("Solicitando eliminación del cliente con ID {}", idCliente);

        if (!clienteRepository.existsById(idCliente)) {
            log.error("No se puede eliminar: cliente con ID {} no encontrado.", idCliente);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }

        // Aquí podrías añadir lógica de negocio (ej. no eliminar si tiene envíos activos)
        // Por ahora, lo eliminamos directamente.
        clienteRepository.deleteById(idCliente);
        log.info("Cliente con ID {} eliminado correctamente.", idCliente);
    }

    // =====================================================
    // =============== CONSULTAS DE CLIENTE ================
    // =====================================================

    // (Método público para ser usado por el servicio de integración)
    public ClienteEntity findClienteById(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> {
                    log.error("Cliente con ID {} no encontrado.", idCliente);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
                });
    }

    public List<ClienteResponseDTO> listarClientes() {
        log.info("Listando todos los clientes...");
        return clienteRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ClienteResponseDTO obtenerClientePorId(Long idCliente) {
        log.info("Consultando cliente por ID {}", idCliente);
        ClienteEntity cliente = findClienteById(idCliente);
        return mapToResponseDTO(cliente);
    }

    /**
     * Lógica ESPECIAL para ServicioEnvios:
     * - Si el cliente NO existe → Lo crea
     * - Si el cliente YA existe → Lo retorna (NO error)
     */
    @Transactional
    public ClienteResponseDTO registrarOObtenerCliente(ClienteRequestDTO dto) {
        log.info("Buscando cliente existente con DNI: {}", dto.getDni());

        // 1. Buscar si el cliente ya existe
        Optional<ClienteEntity> clienteExistente = clienteRepository.findByDni(dto.getDni());

        if (clienteExistente.isPresent()) {
            // ✅ CLIENTE EXISTE - Retornar sus datos (SIN error)
            log.info("Cliente con DNI {} ya existe. Retornando datos existentes.", dto.getDni());
            return mapToResponseDTO(clienteExistente.get());
        } else {
            // ✅ CLIENTE NUEVO - Crearlo
            log.info("Cliente con DNI {} no existe. Creando nuevo registro.", dto.getDni());
            return registrarCliente(dto);
        }
    }

    // =====================================================
    // =============== MÉTODOS PRIVADOS (MAPPERS) ==========
    // =====================================================

    private ClienteEntity mapToEntity(ClienteRequestDTO dto, CiudadEntity ciudad) {
        return ClienteEntity.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .mail(dto.getMail())
                .calle(dto.getCalle())
                .altura(dto.getAltura())
                .ciudad(ciudad)
                .build();
    }

    // Mapper principal que usa los DTOs anidados
    private ClienteResponseDTO mapToResponseDTO(ClienteEntity entity) {
        return ClienteResponseDTO.builder()
                .idCliente(entity.getIdCliente())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .dni(entity.getDni())
                .telefono(entity.getTelefono())
                .mail(entity.getMail())
                .calle(entity.getCalle())
                .altura(entity.getAltura())
                .ciudad(mapCiudadToDTO(entity.getCiudad())) // DTO Anidado
                .build();
    }

    // Mapper auxiliar para Ciudad
    private CiudadDTO mapCiudadToDTO(CiudadEntity entity) {
        return CiudadDTO.builder()
                .idCiudad(entity.getIdCiudad())
                .nombre(entity.getNombre())
                .provincia(mapProvinciaToDTO(entity.getProvincia())) // DTO Anidado
                .build();
    }

    // Mapper auxiliar para Provincia
    private ProvinciaDTO mapProvinciaToDTO(ProvinciaEntity entity) {
        return ProvinciaDTO.builder()
                .idProvincia(entity.getIdProvincia())
                .nombre(entity.getNombre())
                .build();
    }
}
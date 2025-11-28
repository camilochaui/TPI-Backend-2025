package org.example.servicioflota.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.servicioflota.client.TarifaApiClient;
import org.example.servicioflota.dto.CamionDTO;
import org.example.servicioflota.model.Camion;
import org.example.servicioflota.model.Contenedor;
import org.example.servicioflota.model.Transportista;
import org.example.servicioflota.repository.CamionRepository;
import org.example.servicioflota.repository.ContenedorRepository;
import org.example.servicioflota.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private TransportistaRepository transportistaRepository;

    @Autowired
    private TarifaApiClient tarifaApiClient;

    @Transactional
    public Camion saveCamion(CamionDTO camionDTO) {
        System.out.println("=== INICIO saveCamion ===");
        System.out.println("Capacidad Peso: " + camionDTO.getCapacidadPeso());
        System.out.println("Capacidad Volumen: " + camionDTO.getCapacidadVolumen());
        System.out.println("Contenedores a asignar: " + camionDTO.getContenedorIds());
        
        Camion camion = new Camion();
        // Primero guardar el camión sin contenedores para que tenga un ID en la BD
        camion.setPatente(camionDTO.getPatente());
        camion.setCapacidadPeso(camionDTO.getCapacidadPeso());
        camion.setCapacidadVolumen(camionDTO.getCapacidadVolumen());
        camion.setDisponibilidad(camionDTO.isDisponibilidad());
        
        if (camionDTO.getTransportistaId() != null) {
            Transportista transportista = transportistaRepository.findById(camionDTO.getTransportistaId())
                    .orElseThrow(() -> new RuntimeException(
                            "Transportista no encontrado con ID: " + camionDTO.getTransportistaId()));
            camion.setTransportista(transportista);
        }
        
        actualizarCostoBaseDesdeTarifa(camion);
        camion = camionRepository.save(camion);
        System.out.println("Camión guardado con patente: " + camion.getPatente());
        
        // Ahora asignar los contenedores
        if (camionDTO.getContenedorIds() != null && !camionDTO.getContenedorIds().isEmpty()) {
            System.out.println("=== ASIGNANDO CONTENEDORES ===");
            // Asignar todos los contenedores
            for (String idContenedor : camionDTO.getContenedorIds()) {
                Contenedor contenedor = contenedorRepository.findById(idContenedor)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Contenedor no encontrado con ID: " + idContenedor));
                System.out.println("Contenedor encontrado: " + idContenedor + " - Peso: " + contenedor.getPeso() + " - Volumen: " + contenedor.getVolumen());
                contenedor.setCamion(camion);
                contenedorRepository.save(contenedor);
            }
            
            // IMPORTANTE: Obtener la lista actualizada de contenedores desde el repositorio
            List<Contenedor> contenedoresCargados = contenedorRepository.findByCamionPatente(camion.getPatente());
            System.out.println("Contenedores cargados: " + contenedoresCargados.size());
            
            // Validar la carga después de asignar los contenedores (lanzará excepción si excede capacidad)
            System.out.println("=== INICIANDO VALIDACIÓN ===");
            validarCargaTotalConLista(camion, contenedoresCargados);
            System.out.println("=== VALIDACIÓN PASÓ ===");
            
            // Si la validación pasa y hay contenedores asignados, marcar como no disponible
            camion.setDisponibilidad(false);
            camion = camionRepository.save(camion);
        }
        
        // Refrescar una vez más para asegurar que tenemos todos los datos actualizados con contenedores
        return camionRepository.findWithContenedoresByPatente(camion.getPatente())
                .orElse(camion);
    }

    @Transactional
    public Camion updateCamion(String patente, CamionDTO camionDTO) {
        Camion camion = camionRepository.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Camión no encontrado con patente: " + patente));

        convertDtoToEntity(camionDTO, camion);
        actualizarCostoBaseDesdeTarifa(camion);
        
        // Actualizar contenedores si vienen en el DTO
        if (camionDTO.getContenedorIds() != null) {
            // Primero, liberar los contenedores actuales
            List<Contenedor> contenedoresActuales = camion.getContenedores();
            if (contenedoresActuales != null) {
                for (Contenedor cont : contenedoresActuales) {
                    cont.setCamion(null);
                    contenedorRepository.save(cont);
                }
            }
            
            // Luego, asignar los nuevos contenedores
            if (!camionDTO.getContenedorIds().isEmpty()) {
                for (String idContenedor : camionDTO.getContenedorIds()) {
                    Contenedor contenedor = contenedorRepository.findById(idContenedor)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Contenedor no encontrado con ID: " + idContenedor));
                    contenedor.setCamion(camion);
                    contenedorRepository.save(contenedor);
                }
                
                // IMPORTANTE: Obtener la lista actualizada de contenedores desde el repositorio
                List<Contenedor> contenedoresCargados = contenedorRepository.findByCamionPatente(camion.getPatente());
                
                // Validar la carga después de asignar los contenedores (lanzará excepción si excede capacidad)
                validarCargaTotalConLista(camion, contenedoresCargados);
            }
        }

        return camionRepository.save(camion);
    }

    @Transactional(readOnly = true)
    public Optional<Camion> getCamionById(String patente) {
        return camionRepository.findById(patente);
    }

    @Transactional(readOnly = true)
    public List<Camion> getAllCamiones(Boolean disponible, Float minPeso, Float minVolumen) {
        List<Camion> camiones = camionRepository.findAll();
        if (disponible != null) {
            camiones = camiones.stream()
                    .filter(c -> c.isDisponibilidad() == disponible)
                    .collect(Collectors.toList());
        }
        if (minPeso != null) {
            camiones = camiones.stream()
                    .filter(c -> c.getCapacidadPeso() >= minPeso)
                    .collect(Collectors.toList());
        }
        if (minVolumen != null) {
            camiones = camiones.stream()
                    .filter(c -> c.getCapacidadVolumen() >= minVolumen)
                    .collect(Collectors.toList());
        }
        return camiones;
    }

    @Transactional
    public void asignarCamion(String patente) {
        Camion camion = camionRepository.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Camión no encontrado con patente: " + patente));

        // 1. VALIDACIÓN DE CAPACIDAD ACTUAL
        validarCargaTotal(camion);

        // 2. Si la validación pasa, cambia la disponibilidad
        camion.setDisponibilidad(false);
        camionRepository.save(camion);
    }

    @Autowired
    private ContenedorRepository contenedorRepository; // Asegúrate de tenerlo

    // ...

    // ¡NUEVO MÉTODO DE SERVICIO!
    @Transactional
    public void vincularContenedor(String patente, String idContenedor) {

        // 1. Buscar las entidades
        Camion camion = camionRepository.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Camión no encontrado: " + patente));

        Contenedor contenedor = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new EntityNotFoundException("Contenedor no encontrado: " + idContenedor));

        // 2. Vincular
        contenedor.setCamion(camion);
        contenedorRepository.save(contenedor); // ¡Esto guarda la FK!

        // 3. Ahora SÍ, validar la carga (porque el contenedor ya está en la lista
        // camion.getContenedores())
        // Esta validación ahora SÍ fallará si se excede el peso/volumen.
        validarCargaTotal(camion);

        // 4. Marcar camión como ocupado (si la validación pasó)
        camion.setDisponibilidad(false);
        camionRepository.save(camion);
    }

    @Transactional
    public void liberarCamion(String patente) {
        Camion camion = camionRepository.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Camión no encontrado con patente: " + patente));
        camion.setDisponibilidad(true);
        camionRepository.save(camion);
    }

    private void actualizarCostoBaseDesdeTarifa(Camion camion) {
        if (camion.getCapacidadVolumen() != null) {
            try {
                Float costoBase = tarifaApiClient.getTarifaBaseKm(camion.getCapacidadVolumen());
                if (costoBase != null) {
                    camion.setCostoBaseXKm(costoBase);
                }
            } catch (Exception e) {
                System.err.println("No se pudo actualizar el costo base desde el ServicioTarifa: " + e.getMessage());
            }
        }
    }

    private void convertDtoToEntity(CamionDTO dto, Camion entity) {
        entity.setPatente(dto.getPatente());
        entity.setCapacidadPeso(dto.getCapacidadPeso());
        entity.setCapacidadVolumen(dto.getCapacidadVolumen());
        entity.setDisponibilidad(dto.isDisponibilidad());

        if (dto.getTransportistaId() != null) {
            Transportista transportista = transportistaRepository.findById(dto.getTransportistaId())
                    .orElseThrow(() -> new RuntimeException(
                            "Transportista no encontrado con ID: " + dto.getTransportistaId()));
            entity.setTransportista(transportista);
        }
    }

    /**
     * Validar que un camión no supere su capacidad máxima en peso ni volumen.
     * Lanza IllegalArgumentException si se excede alguna capacidad.
     */
    private void validarCargaTotal(Camion camion) {
        List<Contenedor> contenedoresAsignados = camion.getContenedores();

        // Si la lista es nula o vacía, la carga total es 0, y la validación pasa.
        if (contenedoresAsignados == null || contenedoresAsignados.isEmpty()) {
            System.out.println("VALIDACIÓN: No hay contenedores asignados");
            return;
        }

        validarCargaTotalConLista(camion, contenedoresAsignados);
    }

    /**
     * Validar que un camión no supere su capacidad máxima en peso ni volumen usando una lista específica.
     * Lanza IllegalArgumentException si se excede alguna capacidad.
     */
    private void validarCargaTotalConLista(Camion camion, List<Contenedor> contenedoresAsignados) {
        // Si la lista es nula o vacía, la carga total es 0, y la validación pasa.
        if (contenedoresAsignados == null || contenedoresAsignados.isEmpty()) {
            System.out.println("VALIDACIÓN: No hay contenedores asignados");
            return;
        }

        // Calcular el peso y volumen total de la carga actual
        float pesoTotalActual = 0f;
        float volumenTotalActual = 0f;

        for (Contenedor c : contenedoresAsignados) {
            // Usamos .floatValue() o forzamos la suma a float si los campos son Integer
            float peso = c.getPeso() != null ? c.getPeso().floatValue() : 0f;
            float volumen = c.getVolumen() != null ? c.getVolumen().floatValue() : 0f;
            pesoTotalActual += peso;
            volumenTotalActual += volumen;
            System.out.println("Contenedor " + c.getIdContenedor() + ": peso=" + peso + ", volumen=" + volumen);
        }

        // Obtener las capacidades máximas del camión (Float)
        Float capacidadPesoMax = camion.getCapacidadPeso();
        Float capacidadVolumenMax = camion.getCapacidadVolumen();

        System.out.println("TOTALES - Peso: " + pesoTotalActual + "/" + capacidadPesoMax + " - Volumen: " + volumenTotalActual + "/" + capacidadVolumenMax);

        // Validar el peso
        if (capacidadPesoMax != null && pesoTotalActual > capacidadPesoMax) {
            String errorMsg = String.format(
                    "Error de Capacidad de Peso: El peso total de los contenedores (%.2f kg) excede la capacidad máxima del camión (%.2f kg). " +
                    "Contenedores asignados: %d",
                    pesoTotalActual, capacidadPesoMax, contenedoresAsignados.size());
            System.out.println("VALIDACIÓN FALLÓ: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Validar el volumen
        if (capacidadVolumenMax != null && volumenTotalActual > capacidadVolumenMax) {
            String errorMsg = String.format(
                    "Error de Capacidad de Volumen: El volumen total de los contenedores (%.2f m³) excede la capacidad máxima del camión (%.2f m³). " +
                    "Contenedores asignados: %d",
                    volumenTotalActual, capacidadVolumenMax, contenedoresAsignados.size());
            System.out.println("VALIDACIÓN FALLÓ: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        System.out.println("VALIDACIÓN EXITOSA: Capacidades dentro de los límites");
    }
}

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
        Camion camion = new Camion();
        convertDtoToEntity(camionDTO, camion);
        actualizarCostoBaseDesdeTarifa(camion);
        return camionRepository.save(camion);
    }

    @Transactional
    public Camion updateCamion(String patente, CamionDTO camionDTO) {
        Camion camion = camionRepository.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Camión no encontrado con patente: " + patente));

        convertDtoToEntity(camionDTO, camion);
        actualizarCostoBaseDesdeTarifa(camion);

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

    // Validar que un camión no supere su capacidad máxima en peso ni volumen.

    private void validarCargaTotal(Camion camion) {
        List<Contenedor> contenedoresAsignados = camion.getContenedores();

        // Si la lista es nula o vacía, la carga total es 0, y la validación pasa.
        if (contenedoresAsignados == null || contenedoresAsignados.isEmpty()) {
            return;
        }

        // Calcular el peso y volumen total de la carga actual
        float pesoTotalActual = 0f;
        float volumenTotalActual = 0f;

        for (Contenedor c : contenedoresAsignados) {
            // Usamos .floatValue() o forzamos la suma a float si los campos son Integer
            pesoTotalActual += c.getPeso() != null ? c.getPeso().floatValue() : 0f;
            volumenTotalActual += c.getVolumen() != null ? c.getVolumen().floatValue() : 0f;
        }

        // Obtener las capacidades máximas del camión (Float)
        Float capacidadPesoMax = camion.getCapacidadPeso();
        Float capacidadVolumenMax = camion.getCapacidadVolumen();

        // Validar el peso
        if (capacidadPesoMax != null && pesoTotalActual > capacidadPesoMax) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error de Capacidad: El peso total de la carga (%.2f) excede la capacidad máxima del camión (%.2f).",
                            pesoTotalActual, capacidadPesoMax));
        }

        // Validar el volumen
        if (capacidadVolumenMax != null && volumenTotalActual > capacidadVolumenMax) {
            throw new IllegalArgumentException(
                    String.format(
                            "Error de Capacidad: El volumen total de la carga (%.2f) excede la capacidad máxima del camión (%.2f).",
                            volumenTotalActual, capacidadVolumenMax));
        }
    }
}

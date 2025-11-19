
package org.example.serviciotarifa.service;

import org.example.serviciotarifa.dto.CalculoTarifaRequest;
import org.example.serviciotarifa.dto.CalculoTarifaResponse;
import org.example.serviciotarifa.dto.EstadiaRequest;
import org.example.serviciotarifa.entity.Calculo;
import org.example.serviciotarifa.entity.Combustible;
import org.example.serviciotarifa.entity.TarifaBaseKm;
import org.example.serviciotarifa.entity.TarifaEstadia;
import org.example.serviciotarifa.repository.*;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class CalculadoraTarifasService {

    private final CombustibleRepository combustibleRepository;
    private final TarifaBaseKmRepository tarifaBaseKmRepository;
    private final TarifaEstadiaRepository tarifaEstadiaRepository;
    private final CalculoRepository calculoRepository;

    public CalculadoraTarifasService(CombustibleRepository combustibleRepository,
                                     TarifaBaseKmRepository tarifaBaseKmRepository,
                                     TarifaEstadiaRepository tarifaEstadiaRepository,
                                     CalculoRepository calculoRepository) {
        this.combustibleRepository = combustibleRepository;
        this.tarifaBaseKmRepository = tarifaBaseKmRepository;
        this.tarifaEstadiaRepository = tarifaEstadiaRepository;
        this.calculoRepository = calculoRepository;
    }


    public Float obtenerPrecioCombustible(String tipoCombustible) {
        return combustibleRepository.findByNombre(tipoCombustible)
                .map(Combustible::getPrecioXLitro)
                .orElseThrow(() -> new IllegalArgumentException("Combustible no encontrado: " + tipoCombustible));
    }

    public Float obtenerTarifaBaseKm(Float volumenContenedor) {
        return tarifaBaseKmRepository.findByVolumen(volumenContenedor)
                .map(TarifaBaseKm::getCostoBaseKm)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró tarifa para el volumen: " + volumenContenedor));
    }

    public Float obtenerCostoEstadia(Long idDeposito) {
        Integer idDepositoInt = idDeposito.intValue();


        return tarifaEstadiaRepository.findByIdDepositoExt(idDepositoInt)
                .map(TarifaEstadia::getCostoDiario)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa de estadía no encontrada para Depósito ID: " + idDeposito));
    }


    public CalculoTarifaResponse calcularTarifas(CalculoTarifaRequest request) {
        try {
            // 1. Calcular costo de combustible
            Float precioCombustible = obtenerPrecioCombustible(request.getTipoCombustible());
            Float costoCombustible = request.getDistanciaTotalKm() * request.getConsumoCamionLitroKm() * precioCombustible;

            // 2. Calcular costo de tarifa base por km
            Float tarifaBaseKm = obtenerTarifaBaseKm(request.getVolumenContenedor());
            Float costoTarifaBase = request.getDistanciaTotalKm() * tarifaBaseKm;

            // 3. Calcular costo de estadías
            Float costoEstadias = 0.0f;
            if (request.getEstadias() != null) {
                for (EstadiaRequest estadia : request.getEstadias()) {

                    Float costoDiario = obtenerCostoEstadia(estadia.getIdDeposito());

                    long diasEstadia = java.time.temporal.ChronoUnit.DAYS.between(
                            estadia.getFechaEntrada(), estadia.getFechaSalida());
                    costoEstadias += costoDiario * diasEstadia;
                }
            }

            // 4. Calcular tarifa de gestión (si aplica)
            Float costoTarifaGestion = request.getTarifaGestion() != null ? request.getTarifaGestion() : 0.0f;

            // 5. Calcular costo total
            Float costoTotal = costoCombustible + costoTarifaBase + costoEstadias + costoTarifaGestion;

            // 6. Calcular consumo promedio general
            Float consumoPromedioGeneral = request.getConsumoCamionLitroKm();

            // 7. Preparar detalles
            Map<String, Object> details = new HashMap<>();
            details.put("costoCombustible", costoCombustible);
            details.put("costoTarifaBase", costoTarifaBase);
            details.put("costoEstadias", costoEstadias);
            details.put("costoTarifaGestion", costoTarifaGestion);
            details.put("precioCombustible", precioCombustible);
            details.put("tarifaBaseKm", tarifaBaseKm);
            details.put("distanciaTotal", request.getDistanciaTotalKm());

            // 8. Generar ID de cálculo
            Integer idCalculo = generarIdCalculo();

            // 9. Crear y retornar respuesta
            CalculoTarifaResponse response = new CalculoTarifaResponse(
                    idCalculo,
                    request.getIdSolicitud(),
                    consumoPromedioGeneral,
                    costoTotal,
                    details
            );

            // 10. Guardar en BD
            guardarCalculoEnBD(response);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error calculando tarifas: " + e.getMessage(), e);
        }
    }

    private Integer generarIdCalculo() {

        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void guardarCalculoEnBD(CalculoTarifaResponse response) {
        try {
            Calculo calculo = new Calculo();
            calculo.setIdCalculo(response.getIdCalculo());
            calculo.setIdSolicitudExt(response.getIdSolicitud());
            calculo.setTipoCalculo("TARIFA_TRANSPORTE");
            calculo.setConsumoPromedioGeneral(response.getConsumoPromedioGeneral());
            calculo.setCostoTotal(response.getCostoTotal());

            // Convertir details a JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String datableJson = objectMapper.writeValueAsString(response.getDetails());
            calculo.setDetalle(datableJson);

            guardarCalculo(calculo);
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo
            System.err.println("Error guardando cálculo en BD: " + e.getMessage());
        }
    }

    public Calculo guardarCalculo(Calculo calculo) {
        return calculoRepository.save(calculo);
    }


    public List<Calculo> obtenerTodosLosCalculos() {
        return calculoRepository.findAll();
    }


    public Calculo obtenerCalculoPorId(Integer id) {
        return calculoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void actualizarCostoEstadia(Long idDeposito, Float nuevoCostoDiario) {
        Integer idDepositoInt = idDeposito.intValue();
        TarifaEstadia tarifa = tarifaEstadiaRepository.findByIdDepositoExt(idDepositoInt)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa de estadía no encontrada para Depósito ID: " + idDeposito));

        tarifa.setCostoDiario(nuevoCostoDiario);
        tarifaEstadiaRepository.save(tarifa);
    }

    @Transactional
    public void actualizarPrecioCombustible(String tipoCombustible, Float nuevoPrecioLitro) {
        Combustible combustible = combustibleRepository.findByNombre(tipoCombustible)
                .orElseThrow(() -> new IllegalArgumentException("Combustible no encontrado: " + tipoCombustible));

        combustible.setPrecioXLitro(nuevoPrecioLitro);
        combustibleRepository.save(combustible);
    }

}

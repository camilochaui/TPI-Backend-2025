
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
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.example.serviciotarifa.dto.TramoRequest;
import org.example.serviciotarifa.dto.CamionCandidate;

@Service
public class CalculadoraTarifasService {

    private final CombustibleRepository combustibleRepository;
    private final TarifaBaseKmRepository tarifaBaseKmRepository;
    private final TarifaEstadiaRepository tarifaEstadiaRepository;
    private final CalculoRepository calculoRepository;
    private final JdbcTemplate jdbcTemplate;

    public CalculadoraTarifasService(CombustibleRepository combustibleRepository,
                                     TarifaBaseKmRepository tarifaBaseKmRepository,
                                     TarifaEstadiaRepository tarifaEstadiaRepository,
                                     CalculoRepository calculoRepository,
                                     JdbcTemplate jdbcTemplate) {
        this.combustibleRepository = combustibleRepository;
        this.tarifaBaseKmRepository = tarifaBaseKmRepository;
        this.tarifaEstadiaRepository = tarifaEstadiaRepository;
        this.calculoRepository = calculoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }


    public Float obtenerPrecioCombustible(String tipoCombustible) {
        // Devolver precio por nombre si existe; si no, lanzar excepción para que el llamador trate el error.
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
            // Obtener precio de combustible
            Float precioCombustible = obtenerPrecioCombustible(request.getTipoCombustible());

            // Calcular costo de estadías
            Float costoEstadias = 0.0f;
            if (request.getEstadias() != null) {
                for (EstadiaRequest estadia : request.getEstadias()) {
                    Float costoDiario = obtenerCostoEstadia(estadia.getIdDeposito());
                    long diasEstadia = java.time.temporal.ChronoUnit.DAYS.between(
                            estadia.getFechaEntrada(), estadia.getFechaSalida());
                    costoEstadias += costoDiario * diasEstadia;
                }
            }

            // Tarifa de gestión: si se proporciona se interpreta como valor por tramo; si se dio cantidadTramos la multiplicamos
            Float tarifaGestionUnitaria = request.getTarifaGestion() != null ? request.getTarifaGestion() : 0.0f;
            int cantidadTramos = request.getCantidadTramos() != null ? request.getCantidadTramos() : 0;
            Float costoTarifaGestion = tarifaGestionUnitaria * Math.max(1, cantidadTramos);

            Float costoTotal = 0.0f;
            Float consumoPromedioGeneral = 0.0f;

            // 1) Si vienen tramos con camión asignado -- calculamos por tramo (más preciso)
            if (request.getTramos() != null && !request.getTramos().isEmpty()) {
                for (TramoRequest tramo : request.getTramos()) {
                    // Validar capacidades si se conocen y si se pasó el peso/volumen del contenedor
                    if (request.getPesoContenedor() != null && tramo.getCapacidadPeso() != null) {
                        if (request.getPesoContenedor() > tramo.getCapacidadPeso()) {
                            throw new IllegalArgumentException("Camión asignado no soporta el peso del contenedor en un tramo");
                        }
                    }
                    if (request.getVolumenContenedor() != null && tramo.getCapacidadVolumen() != null) {
                        if (request.getVolumenContenedor() > tramo.getCapacidadVolumen()) {
                            throw new IllegalArgumentException("Camión asignado no soporta el volumen del contenedor en un tramo");
                        }
                    }

                    Float distancia = tramo.getDistanciaKm();
                    Float costoKm = tramo.getCostoKmCamion() != null ? tramo.getCostoKmCamion() : 0.0f;
                    Float consumoTramo = tramo.getConsumoCamionLitroKm() != null ? tramo.getConsumoCamionLitroKm() : 0.0f;

                    Float costoPorKm = distancia * costoKm;
                    Float costoCombustibleTramo = distancia * consumoTramo * precioCombustible;

                    costoTotal += costoPorKm + costoCombustibleTramo;
                    consumoPromedioGeneral += consumoTramo * distancia; // para promedio ponderado
                }

                // consumoPromedioGeneral -> convertir a L/km promedio ponderado
                Float distanciaTotal = 0.0f;
                for (TramoRequest t : request.getTramos()) distanciaTotal += t.getDistanciaKm();
                if (distanciaTotal > 0) consumoPromedioGeneral = consumoPromedioGeneral / distanciaTotal;
                else consumoPromedioGeneral = request.getConsumoCamionLitroKm();

                // sumar estadías y gestión
                costoTotal += costoEstadias + costoTarifaGestion;

            } else if (request.getCamionElegibles() != null && !request.getCamionElegibles().isEmpty()) {
                // 2) Si no hay tramos pero hay camiones elegibles -> calcular tarifa aproximada como promedio
                Float distanciaTotal = request.getDistanciaTotalKm() != null ? request.getDistanciaTotalKm() : 0.0f;
                float sumaCostos = 0.0f;
                float sumaConsumos = 0.0f;
                int count = 0;
                for (CamionCandidate c : request.getCamionElegibles()) {
                    // considerar solo camiones que soporten peso/volumen si esos datos están presentes
                    if (request.getPesoContenedor() != null && c.getCapacidadPeso() != null) {
                        if (request.getPesoContenedor() > c.getCapacidadPeso()) continue;
                    }
                    if (request.getVolumenContenedor() != null && c.getCapacidadVolumen() != null) {
                        if (request.getVolumenContenedor() > c.getCapacidadVolumen()) continue;
                    }

                    Float costoKm = c.getCostoKm() != null ? c.getCostoKm() : 0.0f;
                    Float consumo = c.getConsumoPromedioLitroKm() != null ? c.getConsumoPromedioLitroKm() : 0.0f;

                    Float costoPorKm = distanciaTotal * costoKm;
                    Float costoCombustible = distanciaTotal * consumo * precioCombustible;
                    Float costoEstimadoCamion = costoPorKm + costoCombustible + costoEstadias + costoTarifaGestion;
                    sumaCostos += costoEstimadoCamion;
                    sumaConsumos += consumo;
                    count++;
                }
                if (count == 0) throw new IllegalArgumentException("No hay camiones elegibles que soporten el contenedor");
                costoTotal = sumaCostos / count;
                consumoPromedioGeneral = sumaConsumos / count;

            } else {
                // 3) Fallback: comportamiento original basado en tarifaBaseKm y consumo proporcionado
                Float costoCombustible = request.getDistanciaTotalKm() * request.getConsumoCamionLitroKm() * precioCombustible;
                Float tarifaBaseKm = obtenerTarifaBaseKm(request.getVolumenContenedor());
                Float costoTarifaBase = request.getDistanciaTotalKm() * tarifaBaseKm;
                costoTotal = costoCombustible + costoTarifaBase + costoEstadias + costoTarifaGestion;
                consumoPromedioGeneral = request.getConsumoCamionLitroKm();
            }

            // 7. Preparar detalles
            Map<String, Object> details = new HashMap<>();
            details.put("costoEstadias", costoEstadias);
            details.put("costoTarifaGestion", costoTarifaGestion);
            details.put("precioCombustible", precioCombustible);
            details.put("distanciaTotal", request.getDistanciaTotalKm());
            details.put("costoTotalCalculadoAntesDeGestionYEstadias", costoTotal);
            details.put("consumoPromedio", consumoPromedioGeneral);

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

                // Insert using JdbcTemplate and cast the parameter to JSON to avoid PostgreSQL type error
                String sql = "INSERT INTO calculo (idcalculo, idsolicitud_ext, tipocalculo, consumopromediogeneral, costototal, detalle) VALUES (?, ?, ?, ?, ?, ?::json)";
                jdbcTemplate.update(sql,
                    calculo.getIdCalculo(),
                    calculo.getIdSolicitudExt(),
                    calculo.getTipoCalculo(),
                    calculo.getConsumoPromedioGeneral(),
                    calculo.getCostoTotal(),
                    datableJson
                );
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

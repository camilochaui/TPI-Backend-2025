package org.example.serviciotarifa.service;

import org.example.serviciotarifa.dto.CalculoTarifaRequest;
import org.example.serviciotarifa.dto.EstadiaRequest;
import org.example.serviciotarifa.entity.Combustible;
import org.example.serviciotarifa.entity.TarifaBaseKm;
import org.example.serviciotarifa.entity.TarifaEstadia;
import org.example.serviciotarifa.repository.CalculoRepository;
import org.example.serviciotarifa.repository.CombustibleRepository;
import org.example.serviciotarifa.repository.TarifaBaseKmRepository;
import org.example.serviciotarifa.repository.TarifaEstadiaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculadoraTarifasServiceTest {

    @Mock
    CombustibleRepository combustibleRepository;

    @Mock
    TarifaBaseKmRepository tarifaBaseKmRepository;

    @Mock
    TarifaEstadiaRepository tarifaEstadiaRepository;

    @Mock
    CalculoRepository calculoRepository;

    @Mock
    JdbcTemplate jdbcTemplate;

    @Test
    public void testCalculoTarifas_Basico() {
        // Mocks
        when(combustibleRepository.findByNombre(anyString()))
                .thenReturn(Optional.of(new Combustible(1, "Gasoil", 200.0f)));

        when(tarifaBaseKmRepository.findByVolumen(20.0f))
                .thenReturn(Optional.of(new TarifaBaseKm(1, 0f, 50f, 10.0f)));

        when(tarifaEstadiaRepository.findByIdDepositoExt(1))
                .thenReturn(Optional.of(new TarifaEstadia(1, 50.0f, 1, "Dep1")));

        when(jdbcTemplate.update(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(1);

        // Servicio bajo prueba
        CalculadoraTarifasService service = new CalculadoraTarifasService(
                combustibleRepository,
                tarifaBaseKmRepository,
                tarifaEstadiaRepository,
                calculoRepository,
                jdbcTemplate
        );

        // Request de ejemplo
        CalculoTarifaRequest req = new CalculoTarifaRequest();
        req.setIdSolicitud(123);
        req.setConsumoCamionLitroKm(0.2f); // 0.2 litros por km
        req.setTipoCombustible("Gasoil");
        req.setDistanciaTotalKm(100.0f);
        req.setVolumenContenedor(20.0f);
        req.setTarifaGestion(100.0f);
        req.setEstadias(List.of(new EstadiaRequest(1L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,3)))); // 2 días

        var resp = service.calcularTarifas(req);

        // Cálculo esperado:
        // combustible: 100 km * 0.2 L/km * $200 = 4.000
        // tarifa base km: 100 km * $10 = 1.000
        // estadía: 2 días * $50 = 100
        // gestión: 100
        float esperado = 4000f + 1000f + 100f + 100f;

        assertEquals(esperado, resp.getCostoTotal(), 0.001f);
    }

    @Test
    public void testCalculoTarifas_MultiTramo() {
        when(combustibleRepository.findByNombre(anyString()))
                .thenReturn(Optional.of(new Combustible(1, "Gasoil", 200.0f)));

        when(tarifaEstadiaRepository.findByIdDepositoExt(1))
                .thenReturn(Optional.of(new TarifaEstadia(1, 50.0f, 1, "Dep1")));

        when(jdbcTemplate.update(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(1);

        CalculadoraTarifasService service = new CalculadoraTarifasService(
                combustibleRepository,
                tarifaBaseKmRepository,
                tarifaEstadiaRepository,
                calculoRepository,
                jdbcTemplate
        );

        CalculoTarifaRequest req = new CalculoTarifaRequest();
        req.setIdSolicitud(200);
        req.setTipoCombustible("Gasoil");
        req.setVolumenContenedor(10.0f);
        req.setPesoContenedor(1000f);
        req.setTarifaGestion(50.0f);
        req.setCantidadTramos(2);
        // dos tramos
        org.example.serviciotarifa.dto.TramoRequest t1 = new org.example.serviciotarifa.dto.TramoRequest();
        t1.setDistanciaKm(60.0f);
        t1.setCostoKmCamion(12.0f);
        t1.setConsumoCamionLitroKm(0.25f);
        t1.setCapacidadPeso(2000f);
        t1.setCapacidadVolumen(30f);

        org.example.serviciotarifa.dto.TramoRequest t2 = new org.example.serviciotarifa.dto.TramoRequest();
        t2.setDistanciaKm(40.0f);
        t2.setCostoKmCamion(15.0f);
        t2.setConsumoCamionLitroKm(0.3f);
        t2.setCapacidadPeso(1500f);
        t2.setCapacidadVolumen(30f);

        req.setTramos(List.of(t1, t2));
        req.setEstadias(List.of(new org.example.serviciotarifa.dto.EstadiaRequest(1L, java.time.LocalDate.of(2025,1,1), java.time.LocalDate.of(2025,1,2)))); // 1 dia

        var resp = service.calcularTarifas(req);

        // Cálculo esperado:
        // tramo1: 60km * 12 = 720 ; combustible: 60 * 0.25 * 200 = 3000 => 3720
        // tramo2: 40km * 15 = 600 ; combustible: 40 * 0.3 * 200 = 2400 => 3000
        // subtotal tramos = 6720
        // estadía = 1 * 50 = 50
        // gestión = tarifaGestionUnitaria * cantidadTramos = 50 * 2 = 100
        float esperado = 3720f + 3000f + 50f + 100f; // note: previous line double-counted; correct: 3720 + 3000 = 6720
        esperado = 6720f + 50f + 100f;

        assertEquals(esperado, resp.getCostoTotal(), 0.001f);
    }

    @Test
    public void testCalculoTarifas_PromedioCamionesElegibles() {
        when(combustibleRepository.findByNombre(anyString()))
                .thenReturn(Optional.of(new Combustible(1, "Gasoil", 100.0f)));

        when(jdbcTemplate.update(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(1);

        CalculadoraTarifasService service = new CalculadoraTarifasService(
                combustibleRepository,
                tarifaBaseKmRepository,
                tarifaEstadiaRepository,
                calculoRepository,
                jdbcTemplate
        );

        CalculoTarifaRequest req = new CalculoTarifaRequest();
        req.setIdSolicitud(300);
        req.setTipoCombustible("Gasoil");
        req.setDistanciaTotalKm(200.0f);
        req.setVolumenContenedor(10.0f);
        req.setPesoContenedor(800f);
        req.setTarifaGestion(20.0f);
        req.setCantidadTramos(1);

        org.example.serviciotarifa.dto.CamionCandidate c1 = new org.example.serviciotarifa.dto.CamionCandidate();
        c1.setCapacidadPeso(1000f);
        c1.setCapacidadVolumen(20f);
        c1.setConsumoPromedioLitroKm(0.2f);
        c1.setCostoKm(8.0f);

        org.example.serviciotarifa.dto.CamionCandidate c2 = new org.example.serviciotarifa.dto.CamionCandidate();
        c2.setCapacidadPeso(1200f);
        c2.setCapacidadVolumen(25f);
        c2.setConsumoPromedioLitroKm(0.25f);
        c2.setCostoKm(10.0f);

        req.setCamionElegibles(List.of(c1, c2));

        var resp = service.calcularTarifas(req);

        // Calculo por camion:
        // c1: distancia 200 * costoKm 8 = 1600 ; combustible 200 * 0.2 * 100 = 4000 ; gestion 20*1=20 => total 5620
        // c2: 200 * 10 = 2000 ; combustible 200 * 0.25 * 100 = 5000 ; gestion 20 => total 7020
        // promedio = (5620+7020)/2 = 6320
        float esperado = (5620f + 7020f) / 2f;

        assertEquals(esperado, resp.getCostoTotal(), 0.001f);
    }
}

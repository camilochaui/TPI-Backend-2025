# Auditoría de Cumplimiento - Criterios de Evaluación

## Fecha: 2025-11-21
## Estado: Pre-testing (OSRM en preparación)

---

## ✅ CRITERIOS CUMPLIDOS (Código implementado)

### 3) Keycloak como proveedor de identidad
**Estado:** ✅ Implementado correctamente
- **Evidencia:**
  - `docker-compose.yml`: Keycloak 25.0 configurado
  - Todos los servicios tienen `issuer-uri: http://keycloak:8080/realms/transporte-realm`
  - `SecurityConfig.java` en cada servicio con JWT resource server
  - Roles definidos: CLIENTE, ADMIN, TRANSPORTISTA, OPERADOR
- **Registro de clientes:** 
  - `ServicioCliente/controller/ClienteController.java` tiene endpoint `/registro`
  - `ClienteService.registrarCliente()` crea cliente en BD
  - **FALTA:** Integración automática con Keycloak Admin API para crear usuario
  - Existe config `keycloak.admin.*` en `application.yml` pero no se usa en código
- **Puntuación estimada:** 2/3 (falta automatizar alta en Keycloak)

---

### 4) Creación de solicitudes con Cliente y Contenedor
**Estado:** ✅ Implementado
- **Evidencia:**
  - `SolicitudController.registrarNuevaSolicitud()` (línea 48)
  - `SolicitudService.registrarNuevaSolicitud()` crea:
    - Contenedor (via `idContenedorExt`)
    - Cliente (via Feign a ServicioCliente)
    - Solicitud con ubicaciones (ORIGEN, DESTINO)
    - Ruta automática con tramos
  - Validación de contenedor único (línea 68)
  - Estados: BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
- **Puntuación estimada:** 3/3

---

### 5) Asignación de Rutas mediante Rutas Alternativas
**Estado:** ⚠️ Implementado parcialmente
- **Evidencia:**
  - `RutaController`:
    - `GET /api/v1/rutas/tentativas/{numSolicitud}` genera múltiples rutas
    - `POST /api/v1/rutas/asignacion/{numSolicitud}` asigna ruta seleccionada
  - `RutaService.consultarRutasTentativas()` implementado
  - `RutaService.seleccionarRuta()` persiste ruta elegida
- **Limitaciones:**
  - No se evidenció generación de **depósitos intermedios** (solo origen→destino directo en código visto)
  - Falta lógica de búsqueda de depósitos cercanos con `desvio-maximo-km`
  - **Rutas tentativas:** No se probó si efectivamente genera múltiples opciones
- **Puntuación estimada:** 2/3 (implementado pero sin depósitos intermedios verificables)

---

### 6) Asignación de camión y operatoria del transportista
**Estado:** ✅ Implementado
- **Evidencia:**
  - `CamionController` (`ServicioFlota`):
    - `GET /camiones?disponible=true` filtra por disponibilidad
    - `POST /camiones/{patente}/camion-asignado` marca camión ocupado
    - `POST /camiones/{patente}/vincular-contenedor/{id}` asocia contenedor
    - `POST /camiones/{patente}/camion-libre` libera camión
  - `TramoTransportistaController` (`ServicioEnvios`):
    - `POST /api/v1/transportista/tramos/{idTramo}/iniciar` registra inicio tramo
    - `POST /api/v1/transportista/tramos/{idTramo}/finalizar` registra fin tramo
  - `TramoService` actualiza estados: ESTIMADO → ASIGNADO → INICIADO → FINALIZADO
  - Actualiza estado de Solicitud al iniciar primer tramo (EN_TRANSITO) y finalizar último (ENTREGADA)
- **Validaciones:**
  - Verifica capacidad de peso/volumen (en `CamionService.vincularContenedor`)
- **Puntuación estimada:** 3/3

---

### 7) Seguimiento de la solicitud para el cliente
**Estado:** ✅ Implementado
- **Evidencia:**
  - `SolicitudController.obtenerSeguimiento()` (línea 70)
  - `SeguimientoController` (`ServicioCliente`) proxy al endpoint anterior
  - `SolicitudService.consultarSeguimiento()` retorna:
    - Estado actual solicitud
    - Lista de tramos con fechas inicio/fin reales
    - Ubicación actual (último depósito o en tránsito)
  - DTO: `SeguimientoDTO` con `estadoActual`, `tramos[]`, `ubicacionActual`
- **Puntuación estimada:** 3/3

---

### 8) Cálculos de costos y tiempos
**Estado:** ✅ Implementado
- **Evidencia:**
  - `CalcularCostosService.calcularCostoSolicitud()` (línea 91)
  - `ServicioTarifa` expone `POST /api/v1/tarifas/cotizacion` con:
    - Costo por km (tarifas base según volumen)
    - Combustible (consumo camión × valor litro)
    - Estadías en depósitos (días × tarifa diaria)
    - Cargo por gestión (cantidad tramos)
  - Integración OSRM para distancias reales
  - `SolicitudService` calcula `tiempoEstimado` basado en distancia total
  - Persistencia de `costoEstimado`, `tiempoEstimado`, `costoFinal`, `tiempoReal`
- **Limitaciones:**
  - Falta endpoint para **calcular costo final** tras finalizar todos los tramos (se calcula estimado al crear)
  - `costoFinal` y `tiempoReal` no se actualizan automáticamente al cerrar solicitud
- **Puntuación estimada:** 2/3 (cálculo estimado ok, falta flujo de costo final)

---

## 🔴 GAPS CRÍTICOS PARA MÁXIMA NOTA

### 1. Registro automático en Keycloak
**Impacto:** -1 punto (Criterio 3)
- **Problema:** Cliente se crea en BD pero no en Keycloak automáticamente
- **Solución:** Usar Keycloak Admin REST API en `ClienteService.registrarCliente()`
- **Prioridad:** ALTA

### 2. Cálculo y persistencia de costo/tiempo final
**Impacto:** -1 punto (Criterio 8)
- **Problema:** Al finalizar último tramo, no se recalcula costo real ni se persiste
- **Solución:** Listener o endpoint POST-finalization que invoque cálculo con datos reales
- **Prioridad:** ALTA

### 3. Generación de rutas con depósitos intermedios
**Impacto:** -1 punto (Criterio 5)
- **Problema:** No se evidencia búsqueda de depósitos cercanos ni rutas multi-tramo con paradas
- **Solución:** Implementar algoritmo en `RutaService.consultarRutasTentativas()` que:
  - Busque depósitos dentro del `desvio-maximo-km`
  - Genere 2-3 alternativas (directa, 1 depósito, 2 depósitos)
- **Prioridad:** MEDIA-ALTA

### 4. Documentación Swagger completa
**Impacto:** Evaluación general
- **Problema:** Algunos endpoints sin anotaciones `@Operation`, `@ApiResponse`
- **Solución:** Revisar todos los controllers y agregar docs
- **Prioridad:** MEDIA

### 5. Colección de pruebas automatizable (Postman/Bruno)
**Impacto:** Evaluación general + criterio 3 (ejecutabilidad)
- **Problema:** No existe colección exportable
- **Solución:** Crear colección con:
  - Pre-request script para obtener token Keycloak
  - Variables de entorno (base URLs, realm, client)
  - Flujo completo: crear solicitud → asignar ruta → asignar camión → iniciar/finalizar tramos → seguimiento
- **Prioridad:** ALTA

### 6. Validación de capacidad de camión en asignación
**Impacto:** Reglas de negocio
- **Problema:** La validación existe en `vincularContenedor` pero no está siendo llamada en el flujo de asignación de tramos
- **Solución:** Integrar `CamionService.validarCapacidad()` en `TramoAdminController.asignarCamionATramo()`
- **Prioridad:** MEDIA

### 7. Unificación de SecurityConfig en todos los servicios
**Impacto:** Criterio 3 (consistencia)
- **Problema:** `ServicioEnvios/SecurityConfig` usa `realm_access` incorrecto, otros servicios no mapean roles
- **Solución:** Aplicar mismo `JwtAuthenticationConverter` con `realm_access.roles` en todos
- **Prioridad:** ALTA

### 8. Logs estructurados y correlacionados
**Impacto:** Evaluación general (logs solicitados)
- **Problema:** Logs básicos sin trace ID
- **Solución:** Añadir `spring-cloud-sleuth` o `micrometer-tracing` para correlación
- **Prioridad:** BAJA

### 9. Health checks y Actuator expuesto
**Impacto:** Evaluación general
- **Problema:** Actuator configurado pero no expuesto en algunos servicios
- **Solución:** Verificar `management.endpoints.web.exposure.include=health,info,metrics` en todos
- **Prioridad:** BAJA

### 10. Scripts de inicialización de datos (seeds)
**Impacto:** Criterio 3 (ejecutabilidad directa)
- **Problema:** BD se crea vacía, falta data de referencia (tarifas, combustible, depósitos ejemplo, camiones)
- **Solución:** Flyway/Liquibase con migrations + seeds SQL
- **Prioridad:** MEDIA

---

## 📊 RESUMEN PUNTUACIÓN ESTIMADA (sin testing real)

| Criterio | Puntos Max | Estimado | Comentario |
|----------|-----------|----------|------------|
| 3. Keycloak + Registro | 3 | **2** | Falta integración automática usuario Keycloak |
| 4. Solicitud + Cliente + Contenedor | 3 | **3** | ✅ Completo |
| 5. Rutas alternativas | 3 | **2** | Falta depósitos intermedios |
| 6. Asignación camión + transportista | 3 | **3** | ✅ Completo |
| 7. Seguimiento cliente | 3 | **3** | ✅ Completo |
| 8. Cálculos costo/tiempo | 3 | **2** | Falta costo final real |
| **SUBTOTAL funcional** | **18** | **15** | **83%** |

**Apreciación final esperada:** 5-8 puntos (depende de testing y presentación)

---

## 🎯 PLAN DE ACCIÓN INMEDIATO (Post-OSRM)

### Fase 1: Smoke testing (1-2h)
1. Levantar `servicio-envios` y validar con token
2. Crear solicitud y verificar persistencia
3. Probar seguimiento
4. Validar cálculo de costos estimados

### Fase 2: Correcciones críticas (3-4h)
1. **Fix `SecurityConfig` en todos los servicios** (realm_access.roles)
2. **Implementar endpoint POST-finalización** para costo/tiempo final
3. **Añadir lógica de depósitos intermedios** en `RutaService`
4. **Integrar Keycloak Admin API** en registro cliente

### Fase 3: Pulido (2-3h)
1. Generar colección Postman con flujo completo
2. Completar anotaciones Swagger
3. Crear seeds de datos (tarifas, depósitos, camiones ejemplo)
4. Documentar decisiones en README

### Fase 4: Testing E2E (1-2h)
1. Ejecutar flujo completo via Postman
2. Validar con distintos roles
3. Verificar logs y errores
4. Ajustar según hallazgos

---

## 📝 NOTAS ADICIONALES

- **OSRM:** Procesando, ~30-60min restantes
- **API externa:** Usando OSRM (aceptable según enunciado "o similar")
- **Base de datos:** Unificada Postgres (cumple enunciado)
- **Gateway:** Configurado con rutas a todos los servicios
- **Docker compose:** Orquestación completa definida

**Próximos pasos:** Validar con testing real y aplicar correcciones según prioridades.

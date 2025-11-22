# BACKLOG PRIORIZADO - TPI Backend 2025

## 🔥 PRIORIDAD CRÍTICA (Pre-entrega obligatorio)

### 1. Unificar SecurityConfig en todos los servicios ⏱️ 30min
**Impacto:** +1 punto (Criterio 3 - Keycloak consistente)  
**Archivos:**
- `ServicioEnvios/config/SecurityConfig.java`
- `ServicioTarifa/config/SecurityConfig.java`
- `ServicioCliente/config/SecurityConfig.java`
- `ServicioFlota/config/SecurityConfig.java` (crear si no existe)

**Cambio:**
```java
JwtGrantedAuthoritiesConverter conv = new JwtGrantedAuthoritiesConverter();
conv.setAuthoritiesClaimName("realm_access.roles"); // ← FIX clave
conv.setAuthorityPrefix("ROLE_");
```

**Validación:**
- Token JWT debe mapear roles correctamente
- Endpoints con `@PreAuthorize("hasRole('ADMIN')")` deben funcionar

---

### 2. Implementar cálculo y persistencia de costo/tiempo final ⏱️ 1h
**Impacto:** +1 punto (Criterio 8)  
**Archivos:**
- `ServicioEnvios/service/SolicitudService.java`
- `ServicioEnvios/controller/SolicitudController.java`

**Tarea:**
1. Crear endpoint `POST /api/v1/envios/{numSolicitud}/finalizar`
2. Llamar a `CalcularCostosService.calcularCostoSolicitud()` con datos reales (fechas de tramos)
3. Actualizar `Solicitud.costoFinal` y `Solicitud.tiempoReal`
4. Persistir cambios

**Validación:**
- Al finalizar último tramo, solicitud debe tener `costoFinal` y `tiempoReal` != null

---

### 3. Colección Postman/Bruno completa ⏱️ 1.5h
**Impacto:** +1-2 puntos (Criterio 3 - Ejecutabilidad + Evaluación general)  
**Entregable:** `TPI_Backend_2025.postman_collection.json`

**Flujo mínimo:**
1. **Auth:** Pre-request script obtener token Keycloak
2. **Crear solicitud** (POST /envios) - rol CLIENTE
3. **Consultar rutas tentativas** (GET /rutas/tentativas/{id}) - rol ADMIN
4. **Asignar ruta** (POST /rutas/asignacion/{id}) - rol ADMIN
5. **Asignar camión a tramo** (POST /tramos/{id}/asignar-camion) - rol ADMIN
6. **Iniciar tramo** (POST /transportista/tramos/{id}/iniciar) - rol TRANSPORTISTA
7. **Finalizar tramo** (POST /transportista/tramos/{id}/finalizar) - rol TRANSPORTISTA
8. **Seguimiento** (GET /envios/{idContenedor}/seguimiento) - rol CLIENTE
9. **Finalizar solicitud** (POST /envios/{id}/finalizar) - rol ADMIN

**Variables:**
- `{{base_url}}`: http://localhost:9000 (Gateway)
- `{{keycloak_url}}`: http://localhost:8088
- `{{access_token}}`: auto desde pre-request

---

### 4. Integración Keycloak Admin API para registro automático ⏱️ 1h
**Impacto:** +1 punto (Criterio 3)  
**Archivos:**
- `ServicioCliente/service/KeycloakAdminService.java` (nuevo)
- `ServicioCliente/service/ClienteService.java`

**Tarea:**
1. Crear cliente REST para Keycloak Admin API (`/admin/realms/transporte-realm/users`)
2. En `ClienteService.registrarCliente()`:
   - Crear usuario en Keycloak con email, password temporal
   - Asignar rol `CLIENTE`
   - Enviar email de activación (opcional)
3. Manejar errores (usuario ya existe en Keycloak)

**Validación:**
- Cliente registrado puede hacer login en Keycloak
- Token incluye rol CLIENTE

---

## 🟠 PRIORIDAD ALTA (Post-smoke test)

### 5. Lógica de depósitos intermedios en rutas alternativas ⏱️ 2h
**Impacto:** +1 punto (Criterio 5)  
**Archivos:**
- `ServicioEnvios/service/RutaService.java`
- `ServicioFlota/controller/DepositoController.java`

**Tarea:**
1. En `RutaService.consultarRutasTentativas()`:
   - Buscar depósitos en BD dentro de radio `desvio-maximo-km` desde línea origen-destino
   - Generar 3 alternativas:
     - Directa (origen → destino)
     - Con 1 depósito (origen → depósito → destino)
     - Con 2 depósitos (origen → dep1 → dep2 → destino)
2. Calcular distancias OSRM para cada tramo
3. Estimar costos y tiempos por alternativa

**Validación:**
- Endpoint `/rutas/tentativas/{id}` retorna array con 2-3 opciones
- Cada opción tiene lista de tramos con distancia y costo

---

### 6. Seeds de datos iniciales (Flyway/Liquibase) ⏱️ 1h
**Impacto:** +1 punto (Criterio 3 - Ejecutabilidad)  
**Archivos:**
- `docker/initdb/V2__insert_seed_data.sql` (nuevo)

**Datos mínimos:**
- 3-5 depósitos (distintas provincias)
- 5-10 camiones (variedad capacidad peso/volumen)
- 2-3 transportistas
- Tarifas base por rango de volumen
- Valor combustible actual
- Tarifas de estadía por depósito

**Validación:**
- Tras `docker compose up`, BD tiene datos listos para probar

---

### 7. Validación de capacidad en asignación de camión ⏱️ 30min
**Impacto:** Reglas de negocio críticas  
**Archivos:**
- `ServicioEnvios/service/TramoService.java`
- `ServicioEnvios/controller/TramoAdminController.java`

**Tarea:**
1. En `TramoAdminController.asignarCamionATramo()`:
   - Llamar a `CamionService.validarCapacidad(patente, idContenedor)` vía Feign
   - Si falla, retornar 400 con mensaje claro
2. Manejar excepción `IllegalArgumentException`

**Validación:**
- Asignar contenedor 2000kg a camión 1500kg → error 400
- Asignar contenedor 1000kg a camión 1500kg → 200 OK

---

## 🟡 PRIORIDAD MEDIA (Mejora nota final)

### 8. Documentación Swagger completa ⏱️ 1h
**Impacto:** Evaluación general  
**Archivos:** Todos los `*Controller.java`

**Tarea:**
- Revisar cada endpoint y agregar:
  - `@Operation(summary="...", description="...")`
  - `@ApiResponses` con códigos 200, 400, 404, 409, 500
  - `@Parameter` para path/query params
  - `@SecurityRequirement` donde aplique
- Verificar ejemplos de request/response en Swagger UI

---

### 9. Logs estructurados con trace ID ⏱️ 45min
**Impacto:** Evaluación general (logs requeridos)  
**Archivos:**
- `pom.xml` de cada servicio
- `application.yml` de cada servicio

**Tarea:**
1. Añadir dependencias:
   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-sleuth</artifactId>
   </dependency>
   ```
2. Configurar pattern en `application.yml`:
   ```yaml
   logging:
     pattern:
       console: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n"
   ```

**Validación:**
- Logs muestran `[traceId,spanId]` en cada línea
- Mismo traceId se propaga entre microservicios

---

### 10. Health checks y métricas expuestas ⏱️ 30min
**Impacto:** Evaluación general  
**Archivos:** `application.yml` de cada servicio

**Tarea:**
1. Agregar:
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,metrics,prometheus
     endpoint:
       health:
         show-details: always
   ```
2. Verificar endpoints:
   - `/actuator/health` → UP + componentes (DB, Keycloak)
   - `/actuator/metrics` → métricas básicas

---

### 11. Manejo de errores global unificado ⏱️ 45min
**Impacto:** Evaluación general  
**Archivos:**
- `*/config/GlobalExceptionHandler.java` (nuevo en cada servicio)

**Tarea:**
1. Crear `@RestControllerAdvice` con handlers para:
   - `EntityNotFoundException` → 404
   - `IllegalArgumentException` → 400
   - `DataIntegrityViolationException` → 409
   - `Exception` genérica → 500
2. Retornar DTOs de error estandarizados:
   ```json
   {
     "timestamp": "2025-11-21T00:00:00Z",
     "status": 404,
     "error": "Not Found",
     "message": "Solicitud no encontrada",
     "path": "/api/v1/envios/999"
   }
   ```

---

## 🟢 PRIORIDAD BAJA (Opcional para máxima nota)

### 12. Tests unitarios críticos ⏱️ 2h
**Cobertura mínima:**
- `CalcularCostosService.calcularCostoSolicitud()` → Mock tarifas y distancias
- `RutaService.consultarRutasTentativas()` → Mock depósitos
- `TramoService.finalizarTramo()` → Validar actualización estados

---

### 13. README completo con guía de despliegue ⏱️ 1h
**Secciones:**
- Arquitectura (diagrama de contenedores)
- Prerrequisitos (Docker, JDK, Maven)
- Configuración Keycloak (roles, clients, mappers)
- Comandos de despliegue paso a paso
- Endpoints principales por servicio
- Decisiones de diseño (OSRM vs Google, BD única, etc.)

---

### 14. CI/CD básico (GitHub Actions) ⏱️ 1h
**Pipeline:**
1. Build de todos los servicios
2. Ejecución de tests
3. Build de imágenes Docker
4. Push a Docker Hub (opcional)

---

### 15. Externalización de secretos ⏱️ 30min
**Problema:** Password Postgres y Keycloak en claro  
**Solución:**
- Docker secrets
- Variables de entorno sin defaults hardcodeados
- `.env.example` sin valores reales

---

## 📊 ESTIMACIÓN TOTAL

| Prioridad | Horas | Impacto Puntuación |
|-----------|-------|-------------------|
| Crítica | 4.0h | +4-5 puntos |
| Alta | 5.0h | +2-3 puntos |
| Media | 3.0h | +1-2 puntos (calidad) |
| Baja | 4.0h | Opcional |
| **TOTAL** | **16h** | **15→20+ puntos (de 18 funcionales + apreciación)** |

---

## 🎯 RECOMENDACIÓN DE EJECUCIÓN

### Escenario 1: Tiempo limitado (4-6h disponibles)
**Orden sugerido:**
1. SecurityConfig unificado (30min)
2. Cálculo costo final (1h)
3. Colección Postman (1.5h)
4. Seeds de datos (1h)
5. Documentación Swagger (1h)
**→ Resultado esperado:** 17-18 pts funcionales

### Escenario 2: Tiempo moderado (8-12h)
**Agregar a Escenario 1:**
6. Integración Keycloak Admin (1h)
7. Depósitos intermedios (2h)
8. Validación capacidad (30min)
9. Logs estructurados (45min)
10. Manejo errores global (45min)
**→ Resultado esperado:** 20-22 pts (nota máxima posible)

### Escenario 3: Pulido completo (16h+)
**Todo lo anterior + prioridad baja**

---

## ✅ PRÓXIMOS PASOS INMEDIATOS

1. **Esperar OSRM** (~30min restantes)
2. **Smoke test servicio-envios** (30min)
3. **Aplicar fix SecurityConfig** (30min) ← **EMPEZAR AQUÍ**
4. **Crear colección Postman básica** (1h)
5. **Testing E2E del flujo completo** (1h)
6. **Iterar según hallazgos**

¿Quieres que empiece a aplicar el fix de `SecurityConfig` mientras termina OSRM?

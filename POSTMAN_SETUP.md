# Guía de Uso - Colección Postman TPI Backend 2025

## 📥 Importar la Colección

1. Abrir Postman
2. Click en **Import** (esquina superior izquierda)
3. Seleccionar `TPI_Backend_2025.postman_collection.json`
4. Confirmar importación

## ⚙️ Configuración Inicial

### 1. Crear Environment en Postman

Crear un nuevo environment con las siguientes variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost` | `http://localhost` |
| `api_gateway_port` | `9000` | `9000` |
| `keycloak_port` | `8088` | `8088` |
| `realm` | `transporte-realm` | `transporte-realm` |
| `client_id` | `transporte-client` | `transporte-client` |
| `access_token` | *(vacío - se auto-completa)* | *(vacío)* |
| `token_expires_at` | *(vacío - se auto-completa)* | *(vacío)* |
| `num_solicitud` | *(vacío - se auto-completa)* | *(vacío)* |
| `ruta_id` | *(vacío - se auto-completa)* | *(vacío)* |
| `tramo_id` | *(vacío - se auto-completa)* | *(vacío)* |

**Nota:** Las variables con "(vacío - se auto-completa)" se llenan automáticamente mediante los scripts de test de cada request.

### 2. Activar el Environment

- Seleccionar el environment creado en el dropdown (esquina superior derecha)

### 3. Prerequisitos en Keycloak

Antes de ejecutar la colección, asegurar que en Keycloak (`http://localhost:8088`) estén configurados:

#### Realm: `transporte-realm`

#### Client: `transporte-client`
- **Access Type:** public
- **Direct Access Grants Enabled:** ON
- **Valid Redirect URIs:** `*`

#### Roles (Realm Roles):
- `CLIENTE`
- `ADMIN`
- `TRANSPORTISTA`
- `OPERADOR`

#### Usuarios de prueba:

| Username | Password | Rol | Uso |
|----------|----------|-----|-----|
| `cliente1` | `password123` | CLIENTE | Crear solicitudes, ver seguimiento |
| `admin` | `admin123` | ADMIN | Asignar rutas y camiones |
| `transportista1` | `password123` | TRANSPORTISTA | Iniciar/finalizar tramos |

**Crear usuarios:**
1. Ir a **Users** → **Add user**
2. Ingresar username, guardar
3. Tab **Credentials** → Set password (Temporary: OFF)
4. Tab **Role Mappings** → Asignar rol correspondiente

### 4. Prerequisitos de Datos

Asegurar que la base de datos tenga datos seed:

#### Provincias y Ciudades
```sql
INSERT INTO provincia (nombre) VALUES ('Córdoba'), ('Buenos Aires'), ('Santa Fe');
INSERT INTO ciudad (nombre, id_provincia) VALUES 
  ('Córdoba Capital', 1),
  ('Buenos Aires', 2),
  ('Rosario', 3);
```

#### Depósitos
```sql
INSERT INTO deposito (nombre, latitud, longitud, ciudad_id) VALUES
  ('Depósito Córdoba', -31.4201, -64.1888, 1),
  ('Depósito Buenos Aires', -34.6037, -58.3816, 2),
  ('Depósito Rosario', -32.9442, -60.6505, 3),
  ('Depósito Intermedio Campana', -34.1681, -58.9630, 2),
  ('Depósito Intermedio Pergamino', -33.8896, -60.5737, 2);
```

#### Contenedores
```sql
INSERT INTO contenedor (tipo, peso_maximo_kg, descripcion) VALUES
  ('20_PIES', 20000, 'Contenedor estándar 20 pies'),
  ('40_PIES', 30000, 'Contenedor estándar 40 pies');
```

#### Camiones
```sql
INSERT INTO camion (patente, marca, modelo, capacidad_kg, estado, transportista_id) VALUES
  ('AB123CD', 'Mercedes-Benz', 'Actros 2651', 25000, 'DISPONIBLE', 1),
  ('EF456GH', 'Scania', 'R500', 30000, 'DISPONIBLE', 1);
```

#### Tarifas Base
```sql
INSERT INTO tarifa (tipo_contenedor, costo_por_km, recargo_peso, fecha_vigencia) VALUES
  ('20_PIES', 150.00, 0.05, CURRENT_DATE),
  ('40_PIES', 200.00, 0.07, CURRENT_DATE);
```

## 🚀 Flujo de Ejecución Completo

### Paso 1: Autenticación

Ejecutar cualquiera de los requests de la carpeta **"1. Autenticación"** según el rol necesario:

- **Login CLIENTE** → Para crear solicitudes
- **Login ADMIN** → Para asignar rutas/camiones
- **Login TRANSPORTISTA** → Para operar tramos

✅ El token se guarda automáticamente en `{{access_token}}`

### Paso 2: Verificar Servicios (Opcional)

Ejecutar requests de la carpeta **"Health Checks"** para validar que todos los servicios estén corriendo:

- Servicio Cliente (8081)
- Servicio Envíos (8082)
- Servicio Tarifa (8083)
- Servicio Flota (8085)
- OSRM (5000)

### Paso 3: Registrar Cliente

**Carpeta:** "2. Gestión de Clientes"

1. **Registrar Cliente** (POST)
   - Crea un nuevo cliente en el sistema
   - Si hay integración con Keycloak Admin API, también crea el usuario

2. **Obtener Cliente por DNI** (GET)
   - Verifica que el cliente fue creado correctamente

### Paso 4: Crear Solicitud de Envío

**Carpeta:** "3. Gestión de Solicitudes"

1. **Autenticarse como CLIENTE** (si no lo hiciste)

2. **Crear Solicitud de Envío** (POST)
   - Body ejemplo:
   ```json
   {
     "clienteDni": "38456789",
     "contenedorId": 1,
     "depositoOrigenId": 1,
     "depositoDestinoId": 2,
     "fechaRetiroEstimada": "2025-12-01T10:00:00",
     "observaciones": "Frágil"
   }
   ```
   - ✅ `{{num_solicitud}}` se guarda automáticamente

### Paso 5: Consultar y Asignar Ruta

1. **Consultar Rutas Tentativas** (GET)
   - Genera 2-3 rutas alternativas usando OSRM
   - ✅ `{{ruta_id}}` se guarda automáticamente con la primera ruta

2. **Autenticarse como ADMIN**

3. **Asignar Ruta a Solicitud** (POST)
   - Usa `{{ruta_id}}` automáticamente
   - Estado cambia a `RUTA_ASIGNADA`

### Paso 6: Asignar Camión

**Carpeta:** "4. Gestión de Flota"

1. **Listar Camiones Disponibles** (GET)
   - Ver qué camiones están libres

2. **Asignar Camión a Tramo** (POST)
   - Body:
   ```json
   {
     "tramoId": 1,
     "patente": "AB123CD"
   }
   ```
   - Camión pasa a estado `ASIGNADO`

3. **Vincular Contenedor a Camión** (POST)

### Paso 7: Operatoria del Transportista

**Carpeta:** "5. Operatoria Transportista"

1. **Autenticarse como TRANSPORTISTA**

2. **Listar Tramos del Transportista** (GET)
   - ✅ `{{tramo_id}}` se guarda automáticamente

3. **Iniciar Tramo** (POST)
   - Registra `fechaHoraInicioReal`
   - Estado → `EN_PROCESO`

4. **Finalizar Tramo** (POST)
   - Registra `fechaHoraFinReal`
   - Estado → `FINALIZADO`

### Paso 8: Seguimiento y Finalización

**Carpeta:** "3. Gestión de Solicitudes"

1. **Seguimiento de Solicitud** (GET)
   - Ver estado actual, tramos, ubicación, costos

2. **Calcular Costos de Solicitud** (POST)
   - Calcula costo estimado

3. **Finalizar Solicitud** (POST)
   - Calcula costo final y tiempo real
   - ✅ Persiste `costoReal` y `tiempoReal`
   - Estado → `FINALIZADA`

## 📊 Mapeo con Criterios de Evaluación

| Criterio | Requests Involucrados |
|----------|----------------------|
| **3) Keycloak** | Todos los requests de autenticación |
| **4) Crear solicitudes** | "Crear Solicitud de Envío" |
| **5) Rutas alternativas** | "Consultar Rutas Tentativas", "Asignar Ruta" |
| **6) Asignación camión y transportista** | "Asignar Camión", "Iniciar/Finalizar Tramo" |
| **7) Seguimiento** | "Seguimiento de Solicitud" |
| **8) Cálculos costos/tiempos** | "Calcular Costos", "Finalizar Solicitud" |

## 🐛 Troubleshooting

### Error 401 Unauthorized
- **Causa:** Token expirado o inválido
- **Solución:** Re-ejecutar el request de Login correspondiente

### Error 403 Forbidden
- **Causa:** Rol insuficiente para el endpoint
- **Solución:** Autenticarse con el usuario correcto (ej: ADMIN para asignar rutas)

### Error 404 Not Found en OSRM
- **Causa:** OSRM no está corriendo o no terminó de procesar datos
- **Solución:** 
  ```bash
  docker logs osrm
  # Debe mostrar: "running and waiting for requests"
  ```

### Variables no se auto-completan
- **Causa:** Scripts de test no se ejecutaron
- **Solución:** Verificar que la pestaña "Tests" del request tenga código JavaScript

### Base de datos sin datos
- **Causa:** No se ejecutó `init.sql` o falta seed data
- **Solución:** 
  ```bash
  docker exec -it postgres psql -U postgres -d tpi_db -f /docker-entrypoint-initdb.d/init.sql
  ```

## 📝 Notas Adicionales

- **Token TTL:** Los tokens expiran en ~5 minutos (configuración por defecto de Keycloak)
- **Orden de ejecución:** Los requests están ordenados según el flujo lógico
- **Scripts automáticos:** Los scripts de "Test" extraen y guardan automáticamente IDs para uso posterior
- **Environment:** No olvidar activar el environment antes de empezar

## 🎯 Testing para Evaluación

Para demostrar el cumplimiento de todos los criterios, ejecutar en orden:

1. ✅ Health Checks (validar servicios UP)
2. ✅ Login CLIENTE → Registrar Cliente → Crear Solicitud
3. ✅ Consultar Rutas Tentativas (2-3 alternativas)
4. ✅ Login ADMIN → Asignar Ruta
5. ✅ Asignar Camión a Tramo
6. ✅ Login TRANSPORTISTA → Iniciar Tramo → Finalizar Tramo
7. ✅ Seguimiento de Solicitud (estado EN_PROCESO → FINALIZADO)
8. ✅ Calcular Costos → Finalizar Solicitud (costo/tiempo final)

**Tiempo estimado del flujo completo:** ~5-7 minutos

---

**Archivo generado:** `TPI_Backend_2025.postman_collection.json`  
**Autor:** TPI Backend 2025  
**Fecha:** Noviembre 2025

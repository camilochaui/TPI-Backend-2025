# TPI Backend 2025

Proyecto TPI Backend 2025 - Sistema de logística de transporte de contenedores.

Resumen rápido
- Microservicios Java Spring Boot: `ApiGateway`, `Eureka`, `ServicioCliente`, `ServicioEnvios`, `ServicioFlota`, `ServicioTarifa`.
- Orquestación: `docker-compose.yml` con Postgres por servicio, Keycloak y (opcional) OSRM.
- Integración de rutas: Google Maps Directions (configurable via `GOOGLE_MAPS_API_KEY`) con fallback a OSRM o cálculo Haversine.

Cómo probar localmente (resumen)
1. Compilar jars (necesario antes de construir imágenes Docker):

```powershell
Push-Location .\ServicioTarifa; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ServicioFlota; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ServicioEnvios; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ServicioCliente; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ApiGateway; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\Eureka; .\mvnw.cmd -DskipTests package; Pop-Location
```

2. (Opcional) Exportar tu API Key de Google Maps en PowerShell (si la tenés):

```powershell
$env:GOOGLE_MAPS_API_KEY = 'TU_API_KEY_DE_GOOGLE_MAPS'
```

3. Levantar todo con Docker Compose:

```powershell
docker compose up --build -d
```

Endpoints importantes
- Eureka UI: `http://localhost:8761`
- Keycloak admin: `http://localhost:8088`
- API Gateway: `http://localhost:9000`
- Servicio Envios: `http://localhost:8082`

Notas importantes
- No subas claves (API keys) al repositorio. Usa variables de entorno o GitHub Secrets.
- Si no querés usar Google Maps, el sistema usará OSRM (requiere `.pbf` preparado) o cálculo Haversine como fallback.

Preparar repo remoto (GitHub)
1. Crea un repo nuevo en GitHub llamado `TPI Backend 2025` (privado o público según prefieras).
2. Luego ejecuta (desde la raíz del proyecto):

```powershell
git remote add origin https://github.com/TU_USUARIO/TPI-Backend-2025.git
git branch -M main
git push -u origin main
```

Si prefieres, puedo ayudarte a crear el repo remoto si me proporcionas un token de GitHub con permisos (opcional).

Contacto y siguientes pasos
- Puedo seguir implementando mejoras, tests, y generar la colección Postman y documentación OpenAPI.

---
Generado automáticamente: instrucciones y archivos base para inicializar el repo.

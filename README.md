# TPI Backend 2025

Proyecto TPI Backend 2025 - Sistema de logística de transporte de contenedores.

Resumen rápido
- Microservicios Java Spring Boot: `ApiGateway`, `ServicioCliente`, `ServicioEnvios`, `ServicioFlota`, `ServicioTarifa`.
 - Microservicios Java Spring Boot: `ApiGateway`, `ServicioCliente`, `ServicioEnvios`, `ServicioFlota`, `ServicioTarifa`.
 - Orquestación: `docker-compose.yml` con una única base de datos Postgres, Keycloak y OSRM.
 - Orquestación: `docker-compose.yml` con una única base de datos Postgres, Keycloak y OSRM.
 - Integración de rutas: usamos OSRM para routing .

Cómo probar localmente (resumen)
1. Compilar jars (necesario antes de construir imágenes Docker):

```powershell
Push-Location .\ServicioTarifa; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ServicioFlota; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ServicioEnvios; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ServicioCliente; .\mvnw.cmd -DskipTests package; Pop-Location
Push-Location .\ApiGateway; .\mvnw.cmd -DskipTests package; Pop-Location
```

2. OSRM: el proyecto usa OSRM para cálculo de rutas. Asegurate de tener el `.pbf` correspondiente y que el servicio `osrm-prepare`/`osrm` esté configurado en `docker-compose.yml`.

Configuración de datos OSRM (recomendado)
- Copia `.env.example` a `.env` y ajusta `OSRM_PBF_DIR` con la ruta absoluta donde colocaste el archivo `.pbf` (evita colocar archivos grandes dentro del repo).
- Ejemplo (PowerShell): `Copy-Item .\.env.example .\.env; (Get-Content .\.env) -replace './osrm/data','C:/Users/TU_USUARIO/osrm-data' | Set-Content .\.env`

Comandos útiles para generar los archivos OSRM y levantar el servicio (PowerShell):

1) Preparar datos (ejecuta desde la raíz del repo):

```powershell
docker compose up --build --force-recreate osrm-prepare
```

2) Cuando termine correctamente, levantar el servicio OSRM:

```powershell
docker compose up -d osrm
```

Si no quieres esperar al preprocesado dentro de Docker, ejecuta `osrm-extract` / `osrm-partition` / `osrm-contract` localmente y copia los archivos resultantes a la carpeta apuntada por `OSRM_PBF_DIR`.

3. Levantar todo con Docker Compose:

```powershell
docker compose up --build -d
```

- Keycloak admin: `http://localhost:8088`
- API Gateway: `http://localhost:9000`
- Servicio Envios: `http://localhost:8082`
- API Gateway: `http://localhost:9000`
- Servicio Envios: `http://localhost:8082`

Notas importantes
- No subas claves (API keys) al repositorio. Usa variables de entorno o GitHub Secrets.
- La inicialización de la base de datos está consolidada en `docker/initdb/init.sql`. Se eliminaron los scripts `schema-*.sql` y `data-*.sql` dentro de cada servicio para evitar duplicados.
- Para reinicializar la base de datos desde cero (BORRARÁ datos actuales), ejecutar:

```powershell
docker compose down -v --remove-orphans
docker compose up --build -d
```

- OSRM: el servicio `osrm` requiere el archivo `.pbf` adecuado; `osrm-prepare` ejecuta el preprocesado en el contenedor si está configurado.

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

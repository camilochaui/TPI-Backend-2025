cd C:\Users\Usuario\OneDrive\Escritorio\TPI-BACKEND-2025\ServicioFlota 
./mvnw clean package -DskipTests

docker compose up -d --build servicio-envios servicio-flota servicio-tarifa servicio-cliente api-gateway

Cómo comprobar la ultima regla de negocio (paso a paso en Postman)

Crear una solicitud (si no tenés una ya)
Endpoint: POST http://localhost:8082/api/v1/envios

Headers:

Authorization: Bearer <TOKEN_CLIENTE o ADMIN>
Content-Type: application/json
Body (ejemplo):
{
"dniCliente": 30123456,
"nombreCliente": "Juan",
"apellidoCliente": "Pérez",
"emailCliente": "juan.perez@email.com",
"telefonoCliente": 1155551234,
"calleCliente": "Av. Corrientes",
"alturaCliente": 1234,
"idCiudadCliente": 1,
"idContenedor": "MSKU111111",
"peso": 15000.0,
"volumen": 50.0,
"origenDireccion": "Av. Corrientes 1234, La Plata",
"origenLatitud": -34.9215,
"origenLongitud": -57.9545,
"destinoDireccion": "Depósito Buenos Aires - Av. Corrientes 1234",
"destinoLatitud": -34.6037,
"destinoLongitud": -58.3816
}

Respuesta: SolicitudResponseDTO con numSolicitud (guárdalo).

Obtener rutas tentativas (opcional)
GET http://localhost:8082/api/v1/rutas/tentativas/{numSolicitud}
Esto devuelve opciones calculadas (DTO) con tramos y costos estimados para elegir.
Asignar la ruta seleccionada a la solicitud
Endpoint: POST http://localhost:8082/api/v1/rutas/asignacion/{numSolicitud}/{rutaId}
Nota: {rutaId} debe existir en la BD (si la ruta proviene de datos persistidos). Si usás una ruta ya persistida, úsala; si tu flujo crea la ruta automáticamente en DB, usa ese id.
Resultado: la Solicitud queda con estadoSolicitud = PROGRAMADA y ahora (gracias al cambio) cada Tramo de ruta tendrá fechaHoraInicioEstimada y fechaHoraFinEstimada guardadas en BD.
Verificar las fechas estimadas en la solicitud
GET http://localhost:8082/api/v1/envios/{numSolicitud}
En la respuesta mira dentro de ruta.tramos[*] los campos:
fechaHoraInicioEstimada — debe tener un LocalDateTime ISO.
fechaHoraFinEstimada — idem.
También en el endpoint de seguimiento (GET /api/v1/envios/{idContenedor}/seguimiento) ahora verás eventos TRAMO_INICIO_ESTIMADO/TRAMO_FIN_ESTIMADO en eventos.
Asignar camión a un tramo (Admin) — si querés simular el flujo real:
POST http://localhost:8082/api/v1/tramos/{idTramo}/camion-asignacion
Body: { "patenteCamion": "AA123BB" } (adapta al DTO AsignarCamionRequestDTO)
Resultado: patenteCamionExt queda en el tramo y se genera evento TRAMO_ASIGNADO (seguimiento).
Iniciar tramo (Transportista)
POST http://localhost:8082/api/v1/transportista/tramos/{idTramo}/inicio
Requiere token de transportista (claim id_transportista).
Resultado: fechaHoraInicioReal se guarda.
Finalizar tramo (Transportista)
POST http://localhost:8082/api/v1/transportista/tramos/{idTramo}/fin
Resultado: fechaHoraFinReal se guarda; además el servicio calcula distancia real y costo real por tramo.
Finalizar solicitud (Admin)
POST http://localhost:8082/api/v1/envios/{numSolicitud}/finalizar (este endpoint existe)
Validación: la operación exige que todos los tramos tengan fechaHoraInicioReal y fechaHoraFinReal. Si faltan, devuelve 400.
Si están todos, el método calcula:
costoRealTotal = suma(costoReal de tramos)
tiempoReal = diferencia entre primer inicio real (min) y último fin real (max)
Actualiza solicitud.costoReal, solicitud.tiempoReal, estadoSolicitud = FINALIZADA.
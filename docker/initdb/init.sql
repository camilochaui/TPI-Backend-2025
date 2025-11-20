-- Init script for unified tpi_db
-- Combines schema and seed data from ServicioCliente, ServicioEnvios, ServicioFlota and ServicioTarifa

-- =====================================
-- ServicioCliente schema + data
-- =====================================

DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS ciudad;
DROP TABLE IF EXISTS provincia;

CREATE TABLE provincia (
  id_provincia BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE ciudad (
  id_ciudad BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  id_provincia BIGINT NOT NULL,
  CONSTRAINT fk_ciudad_provincia
    FOREIGN KEY (id_provincia)
      REFERENCES provincia (id_provincia)
);

CREATE TABLE cliente (
  id_cliente BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  apellido VARCHAR(255) NOT NULL,
  dni BIGINT NOT NULL UNIQUE,
  telefono BIGINT NOT NULL,
  mail VARCHAR(255) NOT NULL UNIQUE,
  calle VARCHAR(255) NOT NULL,
  altura INT NOT NULL,
  id_localidad BIGINT NOT NULL,
  CONSTRAINT fk_cliente_ciudad
    FOREIGN KEY (id_localidad)
      REFERENCES ciudad (id_ciudad)
);

CREATE INDEX IF NOT EXISTS idx_cliente_dni ON cliente(dni);
CREATE INDEX IF NOT EXISTS idx_cliente_mail ON cliente(mail);
CREATE INDEX IF NOT EXISTS idx_ciudad_provincia_id ON ciudad(id_provincia);

-- seed data clientes
INSERT INTO provincia (nombre) VALUES
('Buenos Aires'),('Catamarca'),('Chaco'),('Chubut'),('Córdoba'),('Corrientes'),('Entre Ríos'),('Formosa'),('Jujuy'),('La Pampa'),('La Rioja'),('Mendoza'),('Misiones'),('Neuquén'),('Río Negro'),('Salta'),('San Juan'),('San Luis'),('Santa Cruz'),('Santa Fe'),('Santiago del Estero'),('Tierra del Fuego'),('Tucumán');

INSERT INTO ciudad (nombre, id_provincia) VALUES
('La Plata', (SELECT id_provincia FROM provincia WHERE nombre = 'Buenos Aires')),
('Mar del Plata', (SELECT id_provincia FROM provincia WHERE nombre = 'Buenos Aires')),
('Bahía Blanca', (SELECT id_provincia FROM provincia WHERE nombre = 'Buenos Aires')),
('Córdoba', (SELECT id_provincia FROM provincia WHERE nombre = 'Córdoba')),
('Río Cuarto', (SELECT id_provincia FROM provincia WHERE nombre = 'Córdoba')),
('Villa María', (SELECT id_provincia FROM provincia WHERE nombre = 'Córdoba')),
('Rosario', (SELECT id_provincia FROM provincia WHERE nombre = 'Santa Fe')),
('Santa Fe', (SELECT id_provincia FROM provincia WHERE nombre = 'Santa Fe')),
('Rafaela', (SELECT id_provincia FROM provincia WHERE nombre = 'Santa Fe')),
('Mendoza', (SELECT id_provincia FROM provincia WHERE nombre = 'Mendoza')),
('San Rafael', (SELECT id_provincia FROM provincia WHERE nombre = 'Mendoza')),
('San Miguel de Tucumán', (SELECT id_provincia FROM provincia WHERE nombre = 'Tucumán')),
('Salta', (SELECT id_provincia FROM provincia WHERE nombre = 'Salta')),
('Paraná', (SELECT id_provincia FROM provincia WHERE nombre = 'Entre Ríos')),
('Posadas', (SELECT id_provincia FROM provincia WHERE nombre = 'Misiones')),
('Neuquén', (SELECT id_provincia FROM provincia WHERE nombre = 'Neuquén')),
('San Carlos de Bariloche', (SELECT id_provincia FROM provincia WHERE nombre = 'Río Negro'));

INSERT INTO cliente (nombre, apellido, dni, telefono, mail, calle, altura, id_localidad)
VALUES
('Juan','Pérez',30123456,1155551234,'juan.perez@email.com','Av. Corrientes',1234,
  (SELECT id_ciudad FROM ciudad WHERE nombre = 'La Plata' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Buenos Aires'))
),
('María','García',32654987,3515556789,'maria.garcia@email.com','Av. Colón',500,
  (SELECT id_ciudad FROM ciudad WHERE nombre = 'Córdoba' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Córdoba'))
),
('Carlos','López',28999111,3415554321,'carlos.lopez@email.com','Bv. Oroño',810,
  (SELECT id_ciudad FROM ciudad WHERE nombre = 'Rosario' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Santa Fe'))
),
('Ana','Martínez',35111222,2615559876,'ana.martinez@email.com','Av. San Martín',2020,
  (SELECT id_ciudad FROM ciudad WHERE nombre = 'Mendoza' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Mendoza'))
),
('Luis','Rodríguez',31555888,2944551122,'luis.rodriguez@email.com','Av. Bustillo',7500,
  (SELECT id_ciudad FROM ciudad WHERE nombre = 'San Carlos de Bariloche' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Río Negro'))
);

-- =====================================
-- ServicioEnvios schema + data
-- =====================================

DROP TABLE IF EXISTS tramo CASCADE;
DROP TABLE IF EXISTS ruta CASCADE;
DROP TABLE IF EXISTS solicitud CASCADE;
DROP TABLE IF EXISTS ubicacion CASCADE;
DROP TABLE IF EXISTS tipo_ubicacion CASCADE;

CREATE TABLE tipo_ubicacion (
  id_tipo_ubicacion BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE solicitud (
  num_solicitud BIGSERIAL PRIMARY KEY,
  id_contenedor_ext VARCHAR(100) NOT NULL UNIQUE,
  id_cliente_ext BIGINT NOT NULL,
  peso DOUBLE PRECISION NOT NULL,
  volumen DOUBLE PRECISION NOT NULL,
  estado_solicitud VARCHAR(255) NOT NULL,
  fecha_creacion TIMESTAMP NOT NULL,
  costo_estimado DOUBLE PRECISION,
  tiempo_estimado VARCHAR(255),
  costo_real DOUBLE PRECISION,
  tiempo_real VARCHAR(255)
);

CREATE TABLE ubicacion (
  id_ubicacion BIGSERIAL PRIMARY KEY,
  direccion TEXT,
  latitud DOUBLE PRECISION NOT NULL,
  longitud DOUBLE PRECISION NOT NULL,
  id_tipo_ubicacion BIGINT NOT NULL REFERENCES tipo_ubicacion(id_tipo_ubicacion),
  num_solicitud BIGINT REFERENCES solicitud(num_solicitud) ON DELETE CASCADE
);

CREATE TABLE ruta (
  id_ruta BIGSERIAL PRIMARY KEY,
  id_solicitud BIGINT NOT NULL UNIQUE REFERENCES solicitud(num_solicitud) ON DELETE CASCADE,
  cantidad_tramos INTEGER,
  cantidad_depositos INTEGER
);

CREATE TABLE tramo (
  id_tramo BIGSERIAL PRIMARY KEY,
  id_ruta BIGINT NOT NULL REFERENCES ruta(id_ruta) ON DELETE CASCADE,
  orden INTEGER NOT NULL,
  origen_id BIGINT NOT NULL REFERENCES ubicacion(id_ubicacion),
  destino_id BIGINT NOT NULL REFERENCES ubicacion(id_ubicacion),
  estado_tramo VARCHAR(255) NOT NULL,
  fecha_hora_inicio_estimada TIMESTAMP,
  fecha_hora_fin_estimada TIMESTAMP,
  fecha_hora_inicio_real TIMESTAMP,
  fecha_hora_fin_real TIMESTAMP,
  patente_camion_ext VARCHAR(255),
  distancia_km_estimada DOUBLE PRECISION,
  costo_estimado DOUBLE PRECISION,
  costo_real DOUBLE PRECISION,
  costo_estadia_deposito DOUBLE PRECISION,
  UNIQUE (id_ruta, orden)
);

CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitud(estado_solicitud);
CREATE INDEX IF NOT EXISTS idx_solicitud_cliente ON solicitud(id_cliente_ext);
CREATE INDEX IF NOT EXISTS idx_tramo_ruta ON tramo(id_ruta);
CREATE INDEX IF NOT EXISTS idx_tramo_estado ON tramo(estado_tramo);
CREATE INDEX IF NOT EXISTS idx_ubicacion_tipo ON ubicacion(id_tipo_ubicacion);

-- seed data envios
INSERT INTO tipo_ubicacion (nombre) VALUES ('CLIENTE_ORIGEN'),('DEPOSITO'),('CLIENTE_DESTINO');

-- =====================================
-- ServicioFlota schema + data
-- =====================================

DROP TABLE IF EXISTS cambio_estado;
DROP TABLE IF EXISTS contenedor;
DROP TABLE IF EXISTS camion;
DROP TABLE IF EXISTS transportista;
DROP TABLE IF EXISTS deposito;
DROP TABLE IF EXISTS estado;

CREATE TABLE deposito (
  id_deposito SERIAL PRIMARY KEY,
  nombre VARCHAR(255),
  direccion VARCHAR(255),
  latitud VARCHAR(255),
  longitud VARCHAR(255)
);

CREATE TABLE transportista (
  id_transportista SERIAL PRIMARY KEY,
  nombre VARCHAR(255),
  apellido VARCHAR(255),
  dni VARCHAR(20),
  telefono VARCHAR(20),
  disponibilidad BOOLEAN
);

CREATE TABLE estado (
  id_estado SERIAL PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL
);

CREATE TABLE camion (
  patente VARCHAR(255) PRIMARY KEY,
  capacidad_peso FLOAT,
  capacidad_volumen FLOAT,
  disponibilidad BOOLEAN,
  consumo_xkm FLOAT,
  costo_base_xkm FLOAT,
  id_combustible_ext INT,
  id_transportista_fk INT,
  FOREIGN KEY (id_transportista_fk) REFERENCES transportista(id_transportista)
);

CREATE TABLE contenedor (
  id_contenedor VARCHAR(255) PRIMARY KEY,
  peso INT,
  volumen INT,
  id_cliente_ext INT,
  id_deposito_fk INT,
  id_camion_fk VARCHAR(255),
  FOREIGN KEY (id_deposito_fk) REFERENCES deposito(id_deposito),
  FOREIGN KEY (id_camion_fk) REFERENCES camion(patente)
);

CREATE TABLE cambio_estado (
  id_cambio_estado SERIAL PRIMARY KEY,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE,
  id_estado_fk INT NOT NULL,
  id_contenedor_fk VARCHAR(255) NOT NULL,
  FOREIGN KEY (id_estado_fk) REFERENCES estado(id_estado),
  FOREIGN KEY (id_contenedor_fk) REFERENCES contenedor(id_contenedor)
);

-- seed data flota
INSERT INTO deposito (id_deposito, nombre, direccion, latitud, longitud) VALUES
(1, 'Depósito Buenos Aires', 'Av. Corrientes 1234', '-34.6037', '-58.3816'),
(2, 'Depósito Córdoba', 'Calle Falsa 123', '-31.4201', '-64.1888'),
(3, 'Depósito Santa Fe', 'Av. Pellegrini 2500', '-32.9468', '-60.6393'),
(4, 'Depósito Mendoza', 'Av. San Martín 1456', '-32.8895', '-68.8458'),
(5, 'Depósito Tucumán', 'Calle 24 de Septiembre 565', '-26.8083', '-65.2176'),
(6, 'Depósito Salta', 'Av. Belgrano 750', '-24.7889', '-65.4106'),
(7, 'Depósito Entre Ríos', 'Av. Urquiza 1450', '-31.7326', '-60.5290'),
(8, 'Depósito Misiones', 'Av. Quaranta 2350', '-27.3621', '-55.9009'),
(9, 'Depósito Chaco', 'Av. 9 de Julio 1250', '-27.4512', '-58.9866'),
(10, 'Depósito Corrientes', 'Av. 3 de Abril 850', '-27.4692', '-58.8304'),
(11, 'Depósito Santiago del Estero', 'Av. Roca 650', '-27.7874', '-64.2673'),
(12, 'Depósito San Juan', 'Av. Libertador 1250', '-31.5375', '-68.5364'),
(13, 'Depósito Jujuy', 'Av. Fascio 850', '-24.1858', '-65.2995'),
(14, 'Depósito Río Negro', 'Av. Roca 350', '-39.0281', '-67.5705'),
(15, 'Depósito Neuquén', 'Av. Olascoaga 450', '-38.9516', '-68.0591'),
(16, 'Depósito Formosa', 'Av. 25 de Mayo 950', '-26.1849', '-58.1731'),
(17, 'Depósito Chubut', 'Av. 9 de Julio 350', '-43.3002', '-65.1023'),
(18, 'Depósito San Luis', 'Av. Illia 250', '-33.3017', '-66.3378'),
(19, 'Depósito Catamarca', 'Av. Güemes 550', '-28.4696', '-65.7795'),
(20, 'Depósito La Rioja', 'Av. Ortiz de Ocampo 350', '-29.4135', '-66.8565'),
(21, 'Depósito La Pampa', 'Calle 9 1250', '-36.6167', '-64.2833'),
(22, 'Depósito Santa Cruz', 'Av. Roca 150', '-51.6230', '-69.2168'),
(23, 'Depósito Tierra del Fuego', 'Av. San Martín 850', '-54.8064', '-68.3070'),
(24, 'Depósito Buenos Aires Norte', 'Panamericana KM 25', '-34.5000', '-58.5333');

INSERT INTO transportista (nombre, apellido, dni, telefono, disponibilidad) VALUES
('Juan', 'Perez', '30123456', '1122334455', true),
('Ana', 'Gomez', '32654321', '1166778899', false),
('Carlos', 'Rodriguez', '28999888', '1133445566', true);

INSERT INTO estado (nombre) VALUES ('En Depósito'),('En Tránsito'),('Entregado'),('Retrasado');

INSERT INTO camion (patente, capacidad_peso, capacidad_volumen, disponibilidad, consumo_xkm, costo_base_xkm, id_combustible_ext, id_transportista_fk) VALUES
('AA123BB', 25000.5, 90.0, true, 0.3, 50.0, 1, 1),
('AC456DD', 28000.0, 100.0, true, 0.35, 55.0, 2, 3),
('AE789FF', 24000.0, 85.5, false, 0.28, 48.0, 1, 2);

INSERT INTO contenedor (id_contenedor, peso, volumen, id_cliente_ext, id_deposito_fk, id_camion_fk) VALUES
('MSKU111111', 15000, 50, 101, 1, 'AA123BB'),
('HLCU222222', 18000, 65, 102, 1, NULL),
('CMAU333333', 20000, 70, 103, 2, 'AC456DD');

INSERT INTO cambio_estado (fecha_inicio, fecha_fin, id_estado_fk, id_contenedor_fk) VALUES
('2024-05-01', '2024-05-10', 1, 'MSKU111111'),
(CURRENT_DATE, NULL, 2, 'MSKU111111');

INSERT INTO cambio_estado (fecha_inicio, fecha_fin, id_estado_fk, id_contenedor_fk) VALUES
(CURRENT_DATE, NULL, 1, 'HLCU222222');

INSERT INTO cambio_estado (fecha_inicio, fecha_fin, id_estado_fk, id_contenedor_fk) VALUES
('2024-04-20', '2024-04-25', 1, 'CMAU333333'),
('2024-04-25', '2024-05-05', 2, 'CMAU333333'),
('2024-05-05', NULL, 3, 'CMAU333333');

-- =====================================
-- ServicioTarifa schema + data
-- =====================================

CREATE TABLE IF NOT EXISTS combustible (
  idcombustible INTEGER PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  precio_x_litro NUMERIC(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS tarifabasekm (
  idtarifakm INTEGER PRIMARY KEY,
  volumenmin FLOAT NOT NULL,
  volumenmax FLOAT NOT NULL,
  costobasekm NUMERIC(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS tarifaestadia (
  idestadia INTEGER PRIMARY KEY,
  costodiario NUMERIC(10,2) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  iddeposito_ext INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS tarifagestion (
  idtarifagestion INTEGER PRIMARY KEY,
  costofijotramo FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS calculo (
  idcalculo INTEGER PRIMARY KEY,
  idsolicitud_ext INTEGER,
  tipocalculo VARCHAR(50),
  consumopromediogeneral FLOAT,
  costototal FLOAT,
  detalle JSON
);

-- Optional: seed tarifa data can be added here if needed

-- End of init script

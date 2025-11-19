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
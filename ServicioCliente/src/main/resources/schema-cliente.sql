
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
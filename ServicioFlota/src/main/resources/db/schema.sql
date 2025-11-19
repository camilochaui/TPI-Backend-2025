
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
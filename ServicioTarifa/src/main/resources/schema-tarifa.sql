
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
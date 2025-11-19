
INSERT INTO provincia (nombre) VALUES
                                   ('Buenos Aires'),
                                   ('Catamarca'),
                                   ('Chaco'),
                                   ('Chubut'),
                                   ('Córdoba'),
                                   ('Corrientes'),
                                   ('Entre Ríos'),
                                   ('Formosa'),
                                   ('Jujuy'),
                                   ('La Pampa'),
                                   ('La Rioja'),
                                   ('Mendoza'),
                                   ('Misiones'),
                                   ('Neuquén'),
                                   ('Río Negro'),
                                   ('Salta'),
                                   ('San Juan'),
                                   ('San Luis'),
                                   ('Santa Cruz'),
                                   ('Santa Fe'),
                                   ('Santiago del Estero'),
                                   ('Tierra del Fuego'),
                                   ('Tucumán');


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
    (
        'Juan', 'Pérez', 30123456, 1155551234, 'juan.perez@email.com', 'Av. Corrientes', 1234,
        (SELECT id_ciudad FROM ciudad WHERE nombre = 'La Plata' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Buenos Aires'))
    ),
    (
        'María', 'García', 32654987, 3515556789, 'maria.garcia@email.com', 'Av. Colón', 500,
        (SELECT id_ciudad FROM ciudad WHERE nombre = 'Córdoba' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Córdoba'))
    ),
    (
        'Carlos', 'López', 28999111, 3415554321, 'carlos.lopez@email.com', 'Bv. Oroño', 810,
        (SELECT id_ciudad FROM ciudad WHERE nombre = 'Rosario' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Santa Fe'))
    ),
    (
        'Ana', 'Martínez', 35111222, 2615559876, 'ana.martinez@email.com', 'Av. San Martín', 2020,
        (SELECT id_ciudad FROM ciudad WHERE nombre = 'Mendoza' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Mendoza'))
    ),
    (
        'Luis', 'Rodríguez', 31555888, 2944551122, 'luis.rodriguez@email.com', 'Av. Bustillo', 7500,
        (SELECT id_ciudad FROM ciudad WHERE nombre = 'San Carlos de Bariloche' AND id_provincia = (SELECT id_provincia FROM provincia WHERE nombre = 'Río Negro'))
    );
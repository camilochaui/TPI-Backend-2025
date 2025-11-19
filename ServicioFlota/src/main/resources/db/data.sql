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

INSERT INTO estado (nombre) VALUES
                                ('En Depósito'),
                                ('En Tránsito'),
                                ('Entregado'),
                                ('Retrasado');


INSERT INTO camion (patente, capacidad_peso, capacidad_volumen, disponibilidad, consumo_xkm, costo_base_xkm, id_combustible_ext, id_transportista_fk) VALUES
                                                                                                                                                          ('AA123BB', 25000.5, 90.0, true, 0.3, 50.0, 1, 1),
                                                                                                                                                          ('AC456DD', 28000.0, 100.0, true, 0.35, 55.0, 2, 3),
                                                                                                                                                          ('AE789FF', 24000.0, 85.5, false, 0.28, 48.0, 1, 2); -- ID 2 para respetar OneToOne


INSERT INTO contenedor (id_contenedor, peso, volumen, id_cliente_ext, id_deposito_fk, id_camion_fk) VALUES
                                                                                                        ('MSKU111111', 15000, 50, 101, 1, 'AA123BB'),
                                                                                                        ('HLCU222222', 18000, 65, 102, 1, NULL),
                                                                                                        ('CMAU333333', 20000, 70, 103, 2, 'AC456DD');


INSERT INTO cambio_estado (fecha_inicio, fecha_fin, id_estado_fk, id_contenedor_fk) VALUES
                                                                                        ('2024-05-01', '2024-05-10', 1, 'MSKU111111'), -- Estuvo en depósito
                                                                                        (CURRENT_DATE, NULL, 2, 'MSKU111111'); -- Ahora en tránsito


INSERT INTO cambio_estado (fecha_inicio, fecha_fin, id_estado_fk, id_contenedor_fk) VALUES
    (CURRENT_DATE, NULL, 1, 'HLCU222222');


INSERT INTO cambio_estado (fecha_inicio, fecha_fin, id_estado_fk, id_contenedor_fk) VALUES
                                                                                        ('2024-04-20', '2024-04-25', 1, 'CMAU333333'), -- Estuvo en depósito
                                                                                        ('2024-04-25', '2024-05-05', 2, 'CMAU333333'), -- Estuvo en tránsito
                                                                                        ('2024-05-05', NULL, 3, 'CMAU333333'); -- Entregado                                                                                    ('2024-05-05', NULL, 3, 'CMAU333333'); -- Entregado
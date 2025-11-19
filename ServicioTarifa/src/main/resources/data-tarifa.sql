
INSERT INTO combustible (idcombustible, nombre, precio_x_litro) VALUES
                                                                    (1, 'Economico', 1000.00),
                                                                    (2, 'Estandar', 1500.00),
                                                                    (3, 'Premium', 2000.00)
    ON CONFLICT (idcombustible) DO UPDATE SET
    nombre = EXCLUDED.nombre,
                                       precio_x_litro = EXCLUDED.precio_x_litro;


INSERT INTO tarifabasekm (idtarifakm, volumenmin, volumenmax, costobasekm) VALUES
                                                                               (1, 0.00, 10.00, 100.00),
                                                                               (2, 11.00, 25.00, 150.00),
                                                                               (3, 26.00, 50.00, 200.00),
                                                                               (4, 51.00, 9999.00, 300.00)
    ON CONFLICT (idtarifakm) DO UPDATE SET
    volumenmin = EXCLUDED.volumenmin,
                                    volumenmax = EXCLUDED.volumenmax,
                                    costobasekm = EXCLUDED.costobasekm;


INSERT INTO tarifaestadia (idestadia, costodiario, nombre, iddeposito_ext) VALUES
                                                                               (1, 5000.00, 'Tarifa Buenos Aires', 1),
                                                                               (2, 4500.00, 'Tarifa Córdoba', 2),
                                                                               (3, 4200.00, 'Tarifa Santa Fe', 3),
                                                                               (4, 4800.00, 'Tarifa Mendoza', 4),
                                                                               (5, 3800.00, 'Tarifa Tucumán', 5),
                                                                               (6, 4000.00, 'Tarifa Salta', 6),
                                                                               (7, 3500.00, 'Tarifa Entre Ríos', 7),
                                                                               (8, 3700.00, 'Tarifa Misiones', 8),
                                                                               (9, 3200.00, 'Tarifa Chaco', 9),
                                                                               (10, 3400.00, 'Tarifa Corrientes', 10),
                                                                               (11, 3300.00, 'Tarifa Santiago del Estero', 11),
                                                                               (12, 4600.00, 'Tarifa San Juan', 12),
                                                                               (13, 3900.00, 'Tarifa Jujuy', 13),
                                                                               (14, 4200.00, 'Tarifa Río Negro', 14),
                                                                               (15, 4400.00, 'Tarifa Neuquén', 15),
                                                                               (16, 3100.00, 'Tarifa Formosa', 16),
                                                                               (17, 4300.00, 'Tarifa Chubut', 17),
                                                                               (18, 3600.00, 'Tarifa San Luis', 18),
                                                                               (19, 3400.00, 'Tarifa Catamarca', 19),
                                                                               (20, 3300.00, 'Tarifa La Rioja', 20),
                                                                               (21, 3200.00, 'Tarifa La Pampa', 21),
                                                                               (22, 4500.00, 'Tarifa Santa Cruz', 22),
                                                                               (23, 5200.00, 'Tarifa Tierra del Fuego', 23),
                                                                               (24, 4700.00, 'Tarifa Buenos Aires Norte', 24)
    ON CONFLICT (idestadia) DO UPDATE SET
    costodiario = EXCLUDED.costodiario,
                                   nombre = EXCLUDED.nombre,
                                   iddeposito_ext = EXCLUDED.iddeposito_ext;


INSERT INTO tarifagestion (idtarifagestion, costofijotramo) VALUES
    (1, 500.00)
    ON CONFLICT (idtarifagestion) DO UPDATE SET
    costofijotramo = EXCLUDED.costofijotramo;
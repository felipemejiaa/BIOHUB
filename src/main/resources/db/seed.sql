INSERT INTO especie (id, nombre_cientifico, nombre_comun, estado_conservacion, es_sensible)
VALUES
(1, 'Dendrobates truncatus', 'Rana venenosa del Chocó', 'EN', true),
(2, 'Setophaga cerulea', 'Reinita cerúlea', 'VU', true),
(3, 'Crocodylus intermedius', 'Caimán llanero', 'CR', true),
(4, 'Quercus humboldtii', 'Roble colombiano', 'VU', false),
(5, 'Podocnemis expansa', 'Tortuga charapa', 'EN', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hallazgo (id, especie_id, latitud, longitud, departamento, fecha_observacion, investigador_id, grupo_taxonomico, atributos)
VALUES
(1, 1, 5.6922, -76.6581, 'Chocó', '2026-01-15 09:30:00', 'INV-001', 'ANFIBIO',
 '{"temperaturaAgua": "18.5", "grabacionAcustica": "canto_rana_001.mp3"}'),
(2, 2, 7.0847, -73.1227, 'Santander', '2026-02-20 06:15:00', 'INV-002', 'AVE_MIGRATORIA',
 '{"rutaGps": "ruta_norte.gpx", "muestraAdn": "ADN-4521"}'),
(3, 3, 5.3507, -69.1849, 'Vichada', '2026-03-10 14:00:00', 'INV-003', 'REPTIL',
 '{"longitudCorporal": "3.2", "fotoEscamas": "caiman_001.jpg"}'),
(4, 4, 4.8133, -75.6961, 'Risaralda', '2026-04-05 11:00:00', 'INV-001', 'PLANTA',
 '{"alturaTallo": "12.5", "muestraHerbario": "HERB-2201"}'),
(5, 5, -1.6417, -69.9400, 'Amazonas', '2026-05-18 07:45:00', 'INV-004', 'REPTIL',
 '{"longitudCorporal": "0.85", "fotoEscamas": "charapa_002.jpg"}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO configuracion_grupo_taxonomico (grupo_taxonomico, atributos_requeridos)
VALUES
('ANFIBIO', '{"temperaturaAgua": "DECIMAL", "grabacionAcustica": "TEXTO"}'::jsonb),
('AVE_MIGRATORIA', '{"rutaGps": "TEXTO", "muestraAdn": "TEXTO"}'::jsonb),
('PLANTA', '{"alturaTallo": "DECIMAL", "muestraHerbario": "TEXTO"}'::jsonb),
('REPTIL', '{"longitudCorporal": "DECIMAL", "fotoEscamas": "TEXTO"}'::jsonb)
ON CONFLICT (grupo_taxonomico) DO NOTHING;
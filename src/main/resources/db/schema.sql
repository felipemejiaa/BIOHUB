CREATE TABLE IF NOT EXISTS especie (
    id              BIGSERIAL PRIMARY KEY,
    nombre_cientifico VARCHAR(200) NOT NULL,
    nombre_comun    VARCHAR(200),
    estado_conservacion VARCHAR(10),
    es_sensible     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS hallazgo (
    id              BIGSERIAL PRIMARY KEY,
    especie_id      BIGINT NOT NULL REFERENCES especie(id),
    latitud         DOUBLE PRECISION NOT NULL,
    longitud        DOUBLE PRECISION NOT NULL,
    departamento    VARCHAR(100) NOT NULL,
    fecha_observacion TIMESTAMP NOT NULL,
    investigador_id VARCHAR(50) NOT NULL,
    grupo_taxonomico VARCHAR(50) NOT NULL,
    atributos       JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS version_hallazgo (
    id              BIGSERIAL PRIMARY KEY,
    hallazgo_id     BIGINT NOT NULL REFERENCES hallazgo(id),
    numero_version  INTEGER NOT NULL,
    fecha_version   TIMESTAMP NOT NULL,
    especie_id      BIGINT NOT NULL REFERENCES especie(id),
    autorizado_por  VARCHAR(50) NOT NULL,
    rol_autorizador VARCHAR(50) NOT NULL,
    motivo          TEXT NOT NULL,
    UNIQUE(hallazgo_id, numero_version)
);

CREATE TABLE IF NOT EXISTS usuario (
    id              VARCHAR(50) PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS reporte_deforestacion (
    id              BIGSERIAL PRIMARY KEY,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT NOW(),
    region_analizada VARCHAR(200) NOT NULL,
    formato         VARCHAR(20) NOT NULL DEFAULT 'PDF',
    estado_procesamiento VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    solicitado_por  VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS configuracion_grupo_taxonomico (
    grupo_taxonomico     VARCHAR(50) PRIMARY KEY,
    atributos_requeridos JSONB NOT NULL DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_hallazgo_especie ON hallazgo(especie_id);
CREATE INDEX IF NOT EXISTS idx_hallazgo_departamento ON hallazgo(departamento);
CREATE INDEX IF NOT EXISTS idx_hallazgo_fecha ON hallazgo(fecha_observacion);
CREATE INDEX IF NOT EXISTS idx_hallazgo_grupo ON hallazgo(grupo_taxonomico);
CREATE INDEX IF NOT EXISTS idx_version_hallazgo ON version_hallazgo(hallazgo_id);
CREATE INDEX IF NOT EXISTS idx_hallazgo_atributos ON hallazgo USING GIN(atributos);
CREATE INDEX IF NOT EXISTS idx_config_grupo_atributos ON configuracion_grupo_taxonomico USING GIN(atributos_requeridos);
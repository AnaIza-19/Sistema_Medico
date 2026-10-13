-- =========================================================
-- CU-12
-- AGENDAMIENTO DE CITA DE SEGUIMIENTO
-- =========================================================


-- ---------------------------------------------------------
-- TIPO DE SEGUIMIENTO
-- ---------------------------------------------------------

ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS tipo_seguimiento VARCHAR(50);


-- ---------------------------------------------------------
-- CONSULTA PADRE
-- ---------------------------------------------------------

ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS consulta_padre_id BIGINT;


-- ---------------------------------------------------------
-- RECORDATORIO
-- ---------------------------------------------------------

ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS recordatorio_programado_para TIMESTAMP;


ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS recordatorio_enviado BOOLEAN NOT NULL DEFAULT FALSE;


-- ---------------------------------------------------------
-- FOREIGN KEY CONSULTA PADRE
-- ---------------------------------------------------------

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_cita_consulta_padre'
    ) THEN

ALTER TABLE citas
    ADD CONSTRAINT fk_cita_consulta_padre
        FOREIGN KEY (consulta_padre_id)
            REFERENCES consulta_medica(id);

END IF;

END $$;


-- ---------------------------------------------------------
-- VALIDACIÓN DEL TIPO DE SEGUIMIENTO
-- ---------------------------------------------------------

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_tipo_seguimiento'
    ) THEN

ALTER TABLE citas
    ADD CONSTRAINT chk_tipo_seguimiento
        CHECK (
            tipo_seguimiento IS NULL
                OR tipo_seguimiento IN (
                                        'MONITOREO_TRATAMIENTO',
                                        'REVISION_RESULTADOS_LABORATORIO'
                )
            );

END IF;

END $$;
-- ============================================================
-- Migración: nueva estructura de store_settings
-- Reemplaza la tabla anterior con campos jsonb flexibles
-- ============================================================

-- Si ya existe la tabla anterior, renombrarla como backup
ALTER TABLE IF EXISTS store_settings RENAME TO store_settings_old;

CREATE TABLE store_settings (
    store_id        UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    completed_step  INT,
    logo_url        VARCHAR(500),
    plan            JSONB,
    basic           JSONB,
    components      JSONB,
    layout          JSONB,
    legal           JSONB,
    payment         JSONB,
    preview         JSONB,
    styles          JSONB,
    updated_at      TIMESTAMPTZ DEFAULT now()
);

-- Índices útiles sobre campos jsonb
CREATE INDEX idx_ss_plan_id     ON store_settings ((plan->>'id'));
CREATE INDEX idx_ss_layout_id   ON store_settings ((layout->>'id'));
CREATE INDEX idx_ss_payment_method ON store_settings ((payment->>'paymentMethod'));

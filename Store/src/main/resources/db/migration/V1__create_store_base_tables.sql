-- V1 — Schema base del Store
-- Tablas creadas originalmente por Hibernate (ddl-auto: update).
-- Ahora gestionadas por Flyway para que V2 pueda referenciar store(store_id).

CREATE TABLE IF NOT EXISTS store (
    store_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS store_settings (
    store_id        UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    completed_step  INT,
    logo_url        VARCHAR(255),
    plan            JSONB,
    basic           JSONB,
    components      JSONB,
    layout          JSONB,
    legal           JSONB,
    payment         JSONB,
    styles          JSONB,
    updated_at      TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS store_user (
    store_id    UUID NOT NULL REFERENCES store(store_id) ON DELETE CASCADE,
    user_id     UUID NOT NULL,
    role        VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN')),
    PRIMARY KEY (store_id, user_id)
);

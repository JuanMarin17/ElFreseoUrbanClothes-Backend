-- ─────────────────────────────────────────────────────────────────────────────
-- V2 — Módulo CMS de tienda
-- Todas las tablas referencian store(store_id) con ON DELETE CASCADE
-- ─────────────────────────────────────────────────────────────────────────────

-- Función trigger compartida para updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ── cms_about ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_about (
    store_id    UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    headline    VARCHAR(255),
    story       TEXT,
    mission     TEXT,
    vision      TEXT,
    founded     VARCHAR(10),
    team_size   VARCHAR(50),
    show_team   BOOLEAN DEFAULT TRUE,
    show_timeline BOOLEAN DEFAULT TRUE,
    updated_at  TIMESTAMPTZ
);

CREATE TRIGGER trg_cms_about_updated_at
    BEFORE UPDATE ON cms_about
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── cms_contact ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_contact (
    store_id        UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    whatsapp        VARCHAR(50),
    instagram       VARCHAR(100),
    tiktok          VARCHAR(100),
    hours           VARCHAR(255),
    form_title      VARCHAR(255),
    form_subtitle   VARCHAR(255),
    show_form       BOOLEAN,
    show_socials    BOOLEAN,
    updated_at      TIMESTAMPTZ
);

CREATE TRIGGER trg_cms_contact_updated_at
    BEFORE UPDATE ON cms_contact
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── cms_locations_config ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_locations_config (
    store_id    UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    show_map    BOOLEAN DEFAULT TRUE,
    updated_at  TIMESTAMPTZ
);

CREATE TRIGGER trg_cms_locations_config_updated_at
    BEFORE UPDATE ON cms_locations_config
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── cms_location (ítems individuales) ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_location (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES store(store_id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    address     TEXT,
    city        VARCHAR(100),
    phone       VARCHAR(50),
    hours       VARCHAR(255),
    map_url     TEXT,
    is_primary  BOOLEAN,
    sort_order  INT DEFAULT 0
);

-- ── cms_returns ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_returns (
    store_id        UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    title           VARCHAR(255),
    intro           TEXT,
    days            SMALLINT DEFAULT 30,
    conditions      TEXT,
    process         TEXT,
    exceptions      TEXT,
    refund_method   VARCHAR(20) CHECK (refund_method IN ('original','store','both')) DEFAULT 'original',
    allow_exchanges BOOLEAN,
    allow_refunds   BOOLEAN,
    require_receipt BOOLEAN,
    contact_email   VARCHAR(255),
    updated_at      TIMESTAMPTZ
);

CREATE TRIGGER trg_cms_returns_updated_at
    BEFORE UPDATE ON cms_returns
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── cms_faq_config ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_faq_config (
    store_id        UUID PRIMARY KEY REFERENCES store(store_id) ON DELETE CASCADE,
    page_title      VARCHAR(255),
    page_subtitle   VARCHAR(255),
    show_search     BOOLEAN DEFAULT TRUE,
    updated_at      TIMESTAMPTZ
);

CREATE TRIGGER trg_cms_faq_config_updated_at
    BEFORE UPDATE ON cms_faq_config
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── cms_faq_item ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cms_faq_item (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES store(store_id) ON DELETE CASCADE,
    question    TEXT NOT NULL,
    answer      TEXT NOT NULL,
    category    VARCHAR(50) CHECK (category IN ('general','pedidos','pagos','envios','productos')) DEFAULT 'general',
    sort_order  INT DEFAULT 0
);

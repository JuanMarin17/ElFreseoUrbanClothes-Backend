-- ============================================================================
-- Esquema de referencia: base de datos "transaction" (módulo Transaction)
-- Generado por reverse-engineering de las entidades JPA en Transaction/src/main/java/com/api/Transaction/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Enums (com.api.Transaction.enums), almacenados como varchar(50) via @Enumerated(EnumType.STRING):
--   PlanName:            GRATUITO, BASICO, PRO, PREMIUM
--   PlanChangeReason:    UPGRADE, DOWNGRADE, RENEWAL, CANCELLED
--   SubscriptionStatus:  ACTIVE, EXPIRED, CANCELLED, PENDING
--   TransactionStatus:   PENDING, APPROVED, REJECTED, REFUNDED
--   TransactionType:     NEW, RENEWAL, UPGRADE, DOWNGRADE

CREATE TABLE IF NOT EXISTS plan (
    plan_id       uuid PRIMARY KEY,
    name          varchar(50) NOT NULL UNIQUE,
    price         numeric(19,2) NOT NULL,
    max_products  integer,
    max_pages     integer,
    max_ai_calls  integer,
    features      jsonb
);

CREATE TABLE IF NOT EXISTS plan_change_history (
    history_id    uuid PRIMARY KEY,
    store_id      uuid NOT NULL,
    from_plan_id  uuid REFERENCES plan(plan_id),
    to_plan_id    uuid NOT NULL REFERENCES plan(plan_id),
    changed_at    timestamp,
    reason        varchar(50)
);

CREATE TABLE IF NOT EXISTS store_subscription (
    subscription_id  uuid PRIMARY KEY,
    store_id         uuid NOT NULL,
    plan_id          uuid NOT NULL REFERENCES plan(plan_id),
    status           varchar(50) NOT NULL,
    started_at       timestamp,
    expires_at       timestamp,
    renewal_at       timestamp
);

CREATE TABLE IF NOT EXISTS transaction (
    transaction_id  uuid PRIMARY KEY,
    store_id        uuid NOT NULL,
    plan_id         uuid NOT NULL REFERENCES plan(plan_id),
    mp_payment_id   varchar(255),
    amount          numeric(19,2) NOT NULL,
    status          varchar(50) NOT NULL,
    type            varchar(50) NOT NULL,
    created_at      timestamp
);

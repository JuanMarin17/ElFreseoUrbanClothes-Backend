-- ============================================================================
-- Esquema de referencia: base de datos "loyalty" (módulo LoyalCustomer)
-- Generado por reverse-engineering de las entidades JPA en LoyalCustomer/src/main/java/com/api/LoyalCustomer/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Tabla: loyalty_account
-- Entidad: LoyaltyAccount.java
CREATE TABLE IF NOT EXISTS loyalty_account (
    account_id  uuid PRIMARY KEY,
    user_id     uuid,
    store_id    uuid,
    points      integer
);

-- Tabla: loyalty_ledger
-- Entidad: LoyaltyLedger.java
-- type (enum LedgerType, almacenado como varchar): EARN, REDEEM, EXPIRE
CREATE TABLE IF NOT EXISTS loyalty_ledger (
    ledger_id   uuid PRIMARY KEY,
    account_id  uuid,
    points      integer,
    type        varchar(50),
    created_at  timestamp,
    expires_at  timestamp
);

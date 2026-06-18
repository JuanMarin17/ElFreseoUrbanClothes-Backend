-- ============================================================================
-- Esquema de referencia: base de datos "payments" (módulo Payment)
-- Generado por reverse-engineering de las entidades JPA en Payment/src/main/java/com/api/payments/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: PaymentTransaction (payment_transactions)
-- Enums: PaymentType (SUBSCRIPTION, STORE_SALE, STORE_SALE_PRO, STORE_SALE_API) -> varchar(50)
--        PaymentStatus (PENDING, APPROVED, REJECTED, CANCELLED, REFUNDED, IN_PROCESS, AUTHORIZED) -> varchar(50)
CREATE TABLE IF NOT EXISTS payment_transactions (
    id                     uuid PRIMARY KEY,
    tenant_id              varchar(255),
    mp_payment_id          varchar(255) UNIQUE,
    mp_preference_id       varchar(255),
    mp_merchant_order_id   varchar(255),
    payment_type           varchar(50) NOT NULL,
    status                 varchar(50) NOT NULL,
    amount                 numeric(15,2) NOT NULL,
    platform_fee           numeric(15,2),
    net_amount             numeric(15,2),
    currency               varchar(3),
    external_reference     varchar(255),
    payment_method_id      varchar(255),
    payment_type_id        varchar(255),
    description            varchar(255),
    payer_email            varchar(255),
    created_at             timestamp NOT NULL,
    updated_at             timestamp,
    approved_at            timestamp
);

-- Entidad: CommissionRecord (commission_records)
-- Relación lógica 1:1 con PaymentTransaction (payment_transaction_id), sin @ManyToOne/@JoinColumn explícito
CREATE TABLE IF NOT EXISTS commission_records (
    id                       uuid PRIMARY KEY,
    tenant_id                varchar(255) NOT NULL,
    payment_transaction_id   uuid NOT NULL,
    mp_payment_id            varchar(255),
    sale_amount              numeric(15,2) NOT NULL,
    commission_rate          numeric(5,4) NOT NULL,
    commission_amount        numeric(15,2) NOT NULL,
    currency                 varchar(3),
    created_at               timestamp NOT NULL
);

-- Entidad: TenantMpCredential (tenant_mp_credentials)
CREATE TABLE IF NOT EXISTS tenant_mp_credentials (
    id              uuid PRIMARY KEY,
    tenant_id       varchar(255) NOT NULL UNIQUE,
    access_token    varchar(512) NOT NULL,
    refresh_token   varchar(512),
    mp_user_id      varchar(255),
    expires_at      timestamp,
    is_active       boolean,
    created_at      timestamp NOT NULL,
    updated_at      timestamp
);

-- Entidad: TenantSubscription (tenant_subscriptions)
-- Enums: SubscriptionPlan (BASIC, PRO, ENTERPRISE) -> varchar(50)
--        PaymentStatus -> varchar(50)
CREATE TABLE IF NOT EXISTS tenant_subscriptions (
    id                       uuid PRIMARY KEY,
    tenant_id                varchar(255) NOT NULL UNIQUE,
    plan                     varchar(50) NOT NULL,
    status                   varchar(50) NOT NULL,
    mp_subscription_id       varchar(255),
    mp_preapproval_plan_id   varchar(255),
    starts_at                timestamp,
    expires_at               timestamp,
    next_billing_at          timestamp,
    created_at               timestamp NOT NULL,
    updated_at               timestamp
);

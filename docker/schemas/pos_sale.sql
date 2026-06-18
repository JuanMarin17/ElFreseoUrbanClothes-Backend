-- ============================================================================
-- Esquema de referencia: base de datos "pos_sale" (módulo PosSale)
-- Generado por reverse-engineering de las entidades JPA en PosSale/src/main/java/com/api/PosSale/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: PosSale (pos_sales)
-- Enums: PosSaleStatus (COMPLETED, CANCELLED, REFUNDED) -> varchar(50)
--        PosPaymentMethod (CASH, CARD, TRANSFER, MIXED) -> varchar(50)
CREATE TABLE IF NOT EXISTS pos_sales (
    sale_id           uuid PRIMARY KEY,
    store_id          uuid NOT NULL,
    employee_id       uuid,
    customer_id       uuid,
    sale_number       varchar(255) NOT NULL UNIQUE,
    status            varchar(50) NOT NULL,
    subtotal          numeric(12,2) NOT NULL,
    discount          numeric(12,2) NOT NULL,
    tax               numeric(12,2) NOT NULL,
    total             numeric(12,2) NOT NULL,
    payment_method    varchar(50) NOT NULL,
    amount_received   numeric(12,2),
    change            numeric(12,2) NOT NULL,
    notes             varchar(500),
    created_at        timestamp NOT NULL,
    updated_at        timestamp,
    cancelled_at      timestamp
);

-- Entidad: PosSaleItem (pos_sale_items) - @ManyToOne PosSale (sale_id)
CREATE TABLE IF NOT EXISTS pos_sale_items (
    item_id        uuid PRIMARY KEY,
    sale_id        uuid NOT NULL REFERENCES pos_sales(sale_id),
    product_id     uuid NOT NULL,
    variant_id     uuid,
    product_name   varchar(255) NOT NULL,
    quantity       integer NOT NULL,
    unit_price     numeric(12,2) NOT NULL,
    discount       numeric(12,2),
    subtotal       numeric(12,2) NOT NULL
);

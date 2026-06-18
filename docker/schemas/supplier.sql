-- ============================================================================
-- Esquema de referencia: base de datos "supplier" (módulo Supplier)
-- Generado por reverse-engineering de las entidades JPA en Supplier/src/main/java/com/api/Supplier/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: Supplier
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id   uuid PRIMARY KEY,
    name          varchar(255) NOT NULL,
    contact_name  varchar(255),
    phone         varchar(255),
    email         varchar(255),
    is_active     boolean,
    created_at    timestamp
);

-- Entidad: StoreSupplier (@EmbeddedId StoreSuppliedId: store_id + supplier_id)
CREATE TABLE IF NOT EXISTS store_supplier (
    store_id     uuid NOT NULL,
    supplier_id  uuid NOT NULL REFERENCES supplier(supplier_id),
    created_at   timestamp,
    PRIMARY KEY (store_id, supplier_id)
);

-- Entidad: SupplierProduct (@EmbeddedId SupplierProductId: supplier_id + product_id)
CREATE TABLE IF NOT EXISTS supplier_product (
    supplier_id  uuid NOT NULL REFERENCES supplier(supplier_id),
    product_id   uuid NOT NULL,
    store_id     uuid NOT NULL,
    created_at   timestamp,
    PRIMARY KEY (supplier_id, product_id)
);

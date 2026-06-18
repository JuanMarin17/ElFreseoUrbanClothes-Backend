-- ============================================================================
-- Esquema de referencia: base de datos "inventory" (módulo Inventory)
-- Generado por reverse-engineering de las entidades JPA en Inventory/src/main/java/com/api/Inventory/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Tabla: location
-- Entidad: Location.java
CREATE TABLE IF NOT EXISTS location (
    location_id  uuid PRIMARY KEY,
    name         varchar(255),
    store_id     uuid
);

-- Tabla: inventory_balance
-- Entidad: InventoryBalance.java
CREATE TABLE IF NOT EXISTS inventory_balance (
    balance_id   uuid PRIMARY KEY,
    variant_id   uuid,
    location_id  uuid,
    store_id     uuid,
    quantity     integer
);

-- Tabla: inventory_movement
-- Entidad: InventoryMovement.java
-- movement_type (enum MovementType, almacenado como varchar): IN, OUT, ADJUSTMENT
CREATE TABLE IF NOT EXISTS inventory_movement (
    movement_id    uuid PRIMARY KEY,
    variant_id     uuid,
    store_id       uuid,
    quantity       integer,
    movement_type  varchar(50),
    created_at     timestamp
);

-- ============================================================================
-- Esquema de referencia: base de datos "customer" (módulo Customer)
-- Generado por reverse-engineering de las entidades JPA en Customer/src/main/java/com/api/Customer/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Tabla: customers
-- Entidad: Customer.java
CREATE TABLE IF NOT EXISTS customers (
    customer_id  uuid PRIMARY KEY,
    store_id     uuid NOT NULL,
    first_name   varchar(100) NOT NULL,
    last_name    varchar(100),
    email        varchar(150),
    phone        varchar(20),
    document     varchar(50),
    notes        varchar(500),
    created_at   timestamp NOT NULL,
    updated_at   timestamp,
    CONSTRAINT uk_customers_store_id_email UNIQUE (store_id, email)
);

-- Tabla: customer_addresses
-- Entidad: CustomerAddress.java
-- alias: etiqueta descriptiva (Casa, Trabajo, Otro)
-- is_default: indica si es la dirección principal del cliente
CREATE TABLE IF NOT EXISTS customer_addresses (
    address_id   uuid PRIMARY KEY,
    customer_id  uuid NOT NULL REFERENCES customers (customer_id),
    alias        varchar(50),
    street       varchar(250) NOT NULL,
    city         varchar(100),
    state        varchar(100),
    country      varchar(100),
    postal_code  varchar(20),
    is_default   boolean NOT NULL,
    created_at   timestamp NOT NULL
);

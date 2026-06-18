-- ============================================================================
-- Esquema de referencia: base de datos "cart" (módulo Cart)
-- Generado por reverse-engineering de las entidades JPA en Cart/src/main/java/com/api/Cart/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: Cart
-- Carrito de compras de un usuario en una tienda. Máximo un carrito activo por
-- tienda/usuario (unique constraint). created_at/updated_at son inicializados
-- por la aplicación (@Builder.Default), no por la base de datos.
CREATE TABLE IF NOT EXISTS cart (
    cart_id     uuid PRIMARY KEY,
    store_id    uuid NOT NULL,
    user_id     uuid NOT NULL,
    created_at  timestamp,
    updated_at  timestamp,
    CONSTRAINT uq_cart_store_user UNIQUE (store_id, user_id)
);

-- Entidad: CartItem
-- @ManyToOne hacia Cart (FK cart_id). Snapshot de datos de producto al
-- momento de agregarlo al carrito. Único por (cart_id, product_id).
CREATE TABLE IF NOT EXISTS cart_item (
    cart_item_id      uuid PRIMARY KEY,
    cart_id           uuid NOT NULL REFERENCES cart(cart_id),
    product_id        uuid NOT NULL,
    product_name      varchar(255) NOT NULL,
    product_sku       varchar(255),
    product_image_url varchar(255),
    quantity          integer NOT NULL,
    unit_price        numeric(12,2) NOT NULL,
    added_at          timestamp,
    CONSTRAINT uq_cart_item_cart_product UNIQUE (cart_id, product_id)
);

-- ============================================================================
-- Esquema de referencia: base de datos "promotion" (módulo Promotion)
-- Generado por reverse-engineering de las entidades JPA en Promotion/src/main/java/com/api/Promotion/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: Coupon
-- discount_type: enum DiscountType (PERCENTAGE, FIXED) -> varchar(50)
CREATE TABLE IF NOT EXISTS coupon (
    coupon_id      uuid PRIMARY KEY,
    code           varchar(255) UNIQUE,
    discount       numeric(19,2),
    discount_type  varchar(50),
    store_id       uuid,
    is_active      boolean NOT NULL,
    created_at     timestamp
);

-- Entidad: CouponRedemption
CREATE TABLE IF NOT EXISTS coupon_redemption (
    redemption_id  uuid PRIMARY KEY,
    coupon_id      uuid,
    user_id        uuid,
    used_at        timestamp
);

-- Entidad: Promotion
-- discount_type: enum DiscountType (PERCENTAGE, FIXED) -> varchar(50)
-- product_id: null = aplica a toda la orden; not-null = solo a este producto
CREATE TABLE IF NOT EXISTS promotion (
    promotion_id   uuid PRIMARY KEY,
    name           varchar(255),
    discount       numeric(19,2),
    discount_type  varchar(50),
    store_id       uuid,
    product_id     uuid,
    is_active      boolean NOT NULL,
    created_at     timestamp
);

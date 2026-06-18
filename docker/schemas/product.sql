-- ============================================================================
-- Esquema de referencia: base de datos "product" (módulo Product)
-- Generado por reverse-engineering de las entidades JPA en Product/src/main/java/com/api/product/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: Brand (brand)
CREATE TABLE IF NOT EXISTS brand (
    brand_id   uuid PRIMARY KEY,
    store_id   uuid,
    name       varchar(255),
    active     boolean
);

-- Entidad: Category (category)
CREATE TABLE IF NOT EXISTS category (
    category_id   uuid PRIMARY KEY,
    store_id      uuid,
    name          varchar(255),
    active        boolean
);

-- Entidad: Product (product) - @ManyToOne Brand (brand_id)
CREATE TABLE IF NOT EXISTS product (
    product_id    uuid PRIMARY KEY,
    store_id      uuid,
    name          varchar(255),
    description   text,
    brand_id      uuid REFERENCES brand(brand_id),
    active        boolean,
    created_at    timestamp
);

-- Entidad: ProductImage (product_image) - @ManyToOne Product (product_id)
CREATE TABLE IF NOT EXISTS product_image (
    image_id     uuid PRIMARY KEY,
    url          text NOT NULL,
    product_id   uuid REFERENCES product(product_id)
);

-- Entidad: ProductVariant (product_variant) - @ManyToOne Product (product_id)
CREATE TABLE IF NOT EXISTS product_variant (
    variant_id   uuid PRIMARY KEY,
    sku          varchar(255),
    price        numeric(19,2),
    stock        integer,
    size         varchar(255),
    color        varchar(255),
    product_id   uuid REFERENCES product(product_id),
    min_stock    integer
);

-- Tabla intermedia @ManyToMany Product <-> Category (@JoinTable "product_category")
CREATE TABLE IF NOT EXISTS product_category (
    product_id    uuid NOT NULL REFERENCES product(product_id),
    category_id   uuid NOT NULL REFERENCES category(category_id),
    PRIMARY KEY (product_id, category_id)
);

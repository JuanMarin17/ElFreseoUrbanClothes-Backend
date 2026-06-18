-- ============================================================================
-- Esquema de referencia: base de datos "order_payment" (módulo OrderPayment)
-- Generado por reverse-engineering de las entidades JPA en OrderPayment/src/main/java/com/api/OrderPayment/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Tabla: orders
-- Entidad: Order.java
-- status (enum OrderStatus, almacenado como varchar): PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
-- payment_method (enum PaymentMethod, almacenado como varchar): CREDIT_CARD, DEBIT_CARD, TRANSFER, CASH_ON_DELIVERY, DIGITAL_WALLET, PSE, EFECTY, CONTRA_ENTREGA
-- shipping_address (@Embedded ShippingAddressDTO): columnas aplanadas full_name, email, phone, address, city, department
CREATE TABLE IF NOT EXISTS orders (
    id              uuid PRIMARY KEY,
    user_id         uuid NOT NULL,
    store_id        uuid NOT NULL,
    order_number    varchar(255) NOT NULL,
    status          varchar(50) NOT NULL,
    subtotal        numeric(12,2) NOT NULL,
    tax             numeric(12,2) NOT NULL,
    discount        numeric(12,2) NOT NULL,
    total           numeric(12,2) NOT NULL,
    full_name       varchar(255),
    email           varchar(255),
    phone           varchar(255),
    address         varchar(255),
    city            varchar(255),
    department      varchar(255),
    payment_method  varchar(50),
    shipping_cost   numeric(12,2),
    notes           varchar(500),
    created_at      timestamp NOT NULL,
    updated_at      timestamp,
    CONSTRAINT uk_orders_order_number UNIQUE (order_number)
);

-- Tabla: order_items
-- Entidad: OrderItem.java
CREATE TABLE IF NOT EXISTS order_items (
    id            uuid PRIMARY KEY,
    order_id      uuid NOT NULL REFERENCES orders (id),
    product_id    uuid NOT NULL,
    product_name  varchar(255) NOT NULL,
    variant_name  varchar(255),
    quantity      integer NOT NULL,
    unit_price    numeric(12,2) NOT NULL,
    subtotal      numeric(12,2) NOT NULL
);

-- Tabla: payments
-- Entidad: Payment.java
-- status (enum PaymentStatus, almacenado como varchar): PENDING, APPROVED, REJECTED, CANCELLED, REFUNDED
-- method (enum PaymentMethod, almacenado como varchar): CREDIT_CARD, DEBIT_CARD, TRANSFER, CASH_ON_DELIVERY, DIGITAL_WALLET, PSE, EFECTY, CONTRA_ENTREGA
CREATE TABLE IF NOT EXISTS payments (
    id                     uuid PRIMARY KEY,
    order_id               uuid NOT NULL REFERENCES orders (id),
    amount                 numeric(12,2) NOT NULL,
    status                 varchar(50) NOT NULL,
    method                 varchar(50) NOT NULL,
    transaction_reference  varchar(255),
    details                varchar(500),
    paid_at                timestamp,
    created_at             timestamp NOT NULL,
    updated_at             timestamp,
    CONSTRAINT uk_payments_order_id UNIQUE (order_id)
);

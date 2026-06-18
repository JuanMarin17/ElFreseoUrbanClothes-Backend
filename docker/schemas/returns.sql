-- ============================================================================
-- Esquema de referencia: base de datos "returns" (módulo Returns)
-- Generado por reverse-engineering de las entidades JPA en Returns/src/main/java/com/api/Returns/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: ReturnRequest
-- status: enum ReturnStatus (PENDING, APPROVED, REJECTED, COMPLETED) -> varchar(50)
CREATE TABLE IF NOT EXISTS return_request (
    return_id   uuid PRIMARY KEY,
    order_id    uuid,
    user_id     uuid,
    store_id    uuid,
    reason      varchar(255),
    status      varchar(50),
    created_at  timestamp
);

-- Entidad: ReturnItem (FK lógica a return_request, sin @ManyToOne explícito en la entidad)
CREATE TABLE IF NOT EXISTS return_item (
    return_item_id  uuid PRIMARY KEY,
    return_id       uuid REFERENCES return_request(return_id),
    variant_id      uuid,
    quantity        integer,
    created_at      timestamp
);

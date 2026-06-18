-- ============================================================================
-- Esquema de referencia: base de datos "support" (módulo Support)
-- Generado por reverse-engineering de las entidades JPA en Support/src/main/java/com/api/Support/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Enum TicketStatus (com.api.Support.enums.TicketStatus): OPEN, IN_PROGRESS, CLOSED
-- Almacenado como varchar(50) via @Enumerated(EnumType.STRING)

CREATE TABLE IF NOT EXISTS support_ticket (
    ticket_id   uuid PRIMARY KEY,
    user_id     uuid,
    user_email  varchar(255),
    subject     varchar(255),
    status      varchar(50),
    created_at  timestamp
);

CREATE TABLE IF NOT EXISTS support_message (
    message_id  uuid PRIMARY KEY,
    ticket_id   uuid,
    sender_id   uuid,
    message     text,
    created_at  timestamp
);

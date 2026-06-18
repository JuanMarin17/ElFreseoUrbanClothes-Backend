-- ============================================================================
-- Esquema de referencia: base de datos "users" (módulo Users)
-- Generado por reverse-engineering de las entidades JPA en Users/src/main/java/com/api/Users/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: User (users)
-- Nota: el id no tiene @GeneratedValue; se asume que es el mismo UUID que el
-- user_id del módulo Auth (sincronizado entre servicios), generado por la app.
CREATE TABLE IF NOT EXISTS users (
    user_id        uuid PRIMARY KEY,
    user_name      varchar(255),
    phone          varchar(255),
    image_profile  varchar(255)
);

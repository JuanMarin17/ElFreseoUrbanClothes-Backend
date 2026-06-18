-- ============================================================================
-- Esquema de referencia: base de datos "cms" (módulo Cms)
-- Generado por reverse-engineering de las entidades JPA en Cms/src/main/java/com/api/Cms/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: CmsPage (cms_page)
-- created_at es inicializado por la aplicación (valor por defecto en el campo),
-- no tiene manejo especial de Hibernate (no usa @CreationTimestamp).
CREATE TABLE IF NOT EXISTS cms_page (
    page_id     uuid PRIMARY KEY,
    store_id    uuid,
    user_id     uuid,
    title       varchar(255),
    content     text,
    created_at  timestamp
);

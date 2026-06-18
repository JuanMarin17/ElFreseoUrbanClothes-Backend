-- ============================================================================
-- Esquema de referencia: base de datos "preferences" (módulo Preferences)
-- Generado por reverse-engineering de las entidades JPA en Preferences/src/main/java/com/api/Preferences/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: UserBehavior (user_behavior)
CREATE TABLE IF NOT EXISTS user_behavior (
    behavior_id   uuid PRIMARY KEY,
    user_id       uuid,
    event_type    varchar(255),
    product_id    uuid,
    created_at    timestamp
);

-- Entidad: UserPreference (user_preference)
CREATE TABLE IF NOT EXISTS user_preference (
    preference_id      uuid PRIMARY KEY,
    user_id            uuid,
    preference_type    varchar(255),
    preference_value   varchar(255)
);

-- Entidad: Wishlist (wishlist)
CREATE TABLE IF NOT EXISTS wishlist (
    wishlist_id   uuid PRIMARY KEY,
    user_id       uuid
);

-- Entidad: WishlistItem (wishlist_item)
-- Nota: wishlist_id y variant_id se modelan como columnas simples (@Column),
-- no como @ManyToOne/@JoinColumn, por lo que no se generan FK explícitas.
CREATE TABLE IF NOT EXISTS wishlist_item (
    wishlist_item_id   uuid PRIMARY KEY,
    wishlist_id        uuid,
    variant_id         uuid
);

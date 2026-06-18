-- ============================================================================
-- Esquema de referencia: base de datos "reviews" (módulo Reviews)
-- Generado por reverse-engineering de las entidades JPA en Reviews/src/main/java/com/api/Reviews/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: CommunityReview
-- UNIQUE constraint explícita: uq_community_review_user (user_id)
CREATE TABLE IF NOT EXISTS community_reviews (
    id          uuid PRIMARY KEY,
    user_id     uuid NOT NULL,
    user_name   varchar(80) NOT NULL,
    user_email  varchar(150) NOT NULL,
    rating      integer NOT NULL,
    text        text NOT NULL,
    likes       integer,
    status      varchar(20),
    created_at  timestamp,
    updated_at  timestamp,
    CONSTRAINT uq_community_review_user UNIQUE (user_id)
);

-- Entidad: ProductReview
CREATE TABLE IF NOT EXISTS product_review (
    review_id    uuid PRIMARY KEY,
    product_id   uuid,
    user_id      uuid,
    rating       integer,
    title        varchar(255),
    body         varchar(255),
    is_verified  boolean,
    created_at   timestamp
);

-- Entidad: ReviewLike
-- UNIQUE constraint explícita: uq_review_like_user (review_id, user_id)
CREATE TABLE IF NOT EXISTS review_likes (
    id         uuid PRIMARY KEY,
    review_id  uuid NOT NULL,
    user_id    uuid NOT NULL,
    CONSTRAINT uq_review_like_user UNIQUE (review_id, user_id)
);

-- Entidad: ReviewReaction
-- reaction_type: enum ReactionType (LIKE, DISLIKE) -> varchar(50)
CREATE TABLE IF NOT EXISTS review_reaction (
    reaction_id    uuid PRIMARY KEY,
    review_id      uuid,
    user_id        uuid,
    reaction_type  varchar(50)
);

-- Entidad: ReviewReply
CREATE TABLE IF NOT EXISTS review_reply (
    reply_id    uuid PRIMARY KEY,
    review_id   uuid,
    user_id     uuid,
    body        varchar(255),
    created_at  timestamp
);

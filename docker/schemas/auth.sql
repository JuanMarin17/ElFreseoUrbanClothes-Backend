-- ============================================================================
-- Esquema de referencia: base de datos "auth" (módulo Auth)
-- Generado por reverse-engineering de las entidades JPA en Auth/src/main/java/com/user/api/user/entity/
-- IMPORTANTE: este script es solo documentación. El esquema real lo gestiona
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) en cada arranque del servicio.
-- Si modificas una entidad, actualiza este archivo manualmente.
-- ============================================================================

-- Entidad: User (app_user)
CREATE TABLE IF NOT EXISTS app_user (
    user_id        uuid PRIMARY KEY,
    email          varchar(255) NOT NULL UNIQUE,
    password_hash  varchar(255),
    create_at      timestamp,
    is_active      boolean
);

-- Entidad: Role
CREATE TABLE IF NOT EXISTS role (
    role_id  uuid PRIMARY KEY,
    name     varchar(255) NOT NULL UNIQUE
);

-- Tabla intermedia @ManyToMany User <-> Role (@JoinTable "user_role")
CREATE TABLE IF NOT EXISTS user_role (
    user_id  uuid NOT NULL REFERENCES app_user(user_id),
    role_id  uuid NOT NULL REFERENCES role(role_id),
    PRIMARY KEY (user_id, role_id)
);

-- Entidad: SecretKey (verification) - @OneToOne con User (FK implícita "user_user_id")
CREATE TABLE IF NOT EXISTS verification (
    id_secret_key  bigint PRIMARY KEY,
    secret_key     varchar(255),
    code           varchar(255),
    expires_at     timestamp,
    user_user_id   uuid REFERENCES app_user(user_id)
);

-- Entidad: UserSession (user_sessions)
CREATE TABLE IF NOT EXISTS user_sessions (
    id            uuid PRIMARY KEY,
    user_id       uuid NOT NULL,
    device        varchar(255),
    browser       varchar(255),
    os            varchar(255),
    ip_address    varchar(255),
    created_at    timestamp,
    last_seen_at  timestamp,
    expires_at    timestamp,
    active        boolean NOT NULL
);

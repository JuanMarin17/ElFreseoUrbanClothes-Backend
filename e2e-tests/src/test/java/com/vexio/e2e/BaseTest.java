package com.vexio.e2e;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Clase base compartida por todos los tests E2E.
 *
 * REQUISITO: todos los contenedores Docker deben estar activos antes de ejecutar.
 *   docker-compose up -d
 *
 * Para generar y ver el reporte HTML (Allure):
 *   mvn test                 ← corre los tests y genera allure-results/
 *   mvn allure:serve         ← abre el reporte en el navegador
 *   mvn allure:report        ← genera HTML estático en target/site/allure-maven-plugin/
 */
public abstract class BaseTest {

    // ─── URLs ────────────────────────────────────────────────────────────────
    protected static final String GATEWAY     = "http://localhost:8080";
    protected static final String CART_DIRECT = "http://localhost:8086"; // no está en el gateway

    // ─── JWT ─────────────────────────────────────────────────────────────────
    private static final String JWT_SECRET = "Wm5N30DT1nFqUmdjrvM4GWqw1utDrSlpdDLyDGa4kBw=";

    // ─── Datos fijos de prueba ────────────────────────────────────────────────
    protected static final UUID   TEST_USER_ID    = UUID.fromString("11111111-1111-1111-1111-111111111111");
    protected static final UUID   TEST_ROLE_ID    = UUID.fromString("22222222-2222-2222-2222-222222222222");
    protected static final UUID   TEST_STORE_ID   = UUID.fromString("33333333-3333-3333-3333-333333333333");
    protected static final UUID   TEST_BRAND_ID   = UUID.fromString("44444444-4444-4444-4444-444444444444");
    protected static final UUID   TEST_CAT_ID     = UUID.fromString("55555555-5555-5555-5555-555555555555");
    protected static final UUID   TEST_PRODUCT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    protected static final UUID   TEST_VARIANT_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    protected static final String TEST_EMAIL      = "test-e2e@vexio.com";
    protected static final String TEST_FULL_NAME  = "E2E Tester";
    protected static final String TEST_USER_NAME  = "e2etester";
    protected static final String TEST_STORE_SLUG = "e2e-test-store-vexio";

    // ─── BD ──────────────────────────────────────────────────────────────────
    private static final String DB_URL_BASE = "jdbc:postgresql://localhost:5432/";
    private static final String DB_USER     = "postgres";
    private static final String DB_PASS     = "root";

    // Token generado una sola vez para todos los tests
    protected static final String AUTH_TOKEN;

    // ─── Timeout global ──────────────────────────────────────────────────────
    // 10 s conexión / 15 s lectura — evita que tests cuelguen 2 minutos
    protected static final RestAssuredConfig RA_CONFIG = RestAssured.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", 10_000)
                    .setParam("http.socket.timeout",     15_000));

    static {
        // AllureRestAssured captura cada request/response y lo embebe en el reporte HTML
        RestAssured.config = RA_CONFIG;
        RestAssured.filters(
            new AllureRestAssured(),
            new RequestLoggingFilter(),
            new ResponseLoggingFilter()
        );
        AUTH_TOKEN = generateToken(TEST_USER_ID, TEST_FULL_NAME, "USER", TEST_EMAIL);
    }

    /**
     * Comprueba si un servicio está disponible verificando que el HTTP status sea 2xx.
     * Solo verificar conexión TCP no es suficiente: un servicio puede aceptar la
     * conexión pero colgar indefinidamente si la BD no está lista.
     */
    protected static boolean isReachable(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(3000);
            c.setReadTimeout(5000);
            c.setRequestMethod("GET");
            int code = c.getResponseCode();
            c.disconnect();
            return code >= 200 && code < 300;
        } catch (IOException e) {
            return false;
        }
    }

    // ─── JWT ─────────────────────────────────────────────────────────────────

    protected static String generateToken(UUID userId, String fullName, String role, String email) {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .claims(Map.of("user_id", userId.toString(), "role", role, "email", email))
                .subject(fullName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7_200_000L))
                .signWith(key)
                .compact();
    }

    // ─── Specs REST Assured ───────────────────────────────────────────────────

    protected static RequestSpecification withJwt() {
        return RestAssured.given()
                .baseUri(GATEWAY)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AUTH_TOKEN);
    }

    protected static RequestSpecification withJwtDirect(String baseUri) {
        return RestAssured.given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AUTH_TOKEN);
    }

    protected static RequestSpecification noAuth() {
        return RestAssured.given()
                .baseUri(GATEWAY)
                .contentType(ContentType.JSON);
    }

    // ─── Seeders de BD ───────────────────────────────────────────────────────

    protected static Connection connectTo(String dbName) throws SQLException {
        return DriverManager.getConnection(DB_URL_BASE + dbName, DB_USER, DB_PASS);
    }

    protected static void run(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) { st.execute(sql); }
    }

    /** Inserta el usuario de prueba en auth DB. */
    protected static void seedAuthUser() {
        try (Connection c = connectTo("auth")) {
            // Detectar el nombre real de la PK de role (role_id o roleid) consultando information_schema
            String rolePkCol = detectColumn(c, "role", new String[]{"role_id", "roleid"});
            run(c, "INSERT INTO role (" + rolePkCol + ", name) " +
                "VALUES ('" + TEST_ROLE_ID + "', 'USER') ON CONFLICT DO NOTHING");

            run(c,
                "INSERT INTO app_user (user_id, email, password_hash, create_at, is_active) " +
                "VALUES ('" + TEST_USER_ID + "', '" + TEST_EMAIL + "', " +
                "'$2a$10$e2eTestHashPlaceholder12345678901234567890', NOW(), true) ON CONFLICT DO NOTHING");

            run(c,
                "INSERT INTO user_role (user_id, role_id) " +
                "VALUES ('" + TEST_USER_ID + "', '" + TEST_ROLE_ID + "') ON CONFLICT DO NOTHING");
        } catch (SQLException e) { throw new RuntimeException("seedAuthUser falló: " + e.getMessage(), e); }
    }

    /** Devuelve el primer nombre de columna de `candidates` que exista en la tabla. */
    private static String detectColumn(Connection c, String table, String[] candidates) throws SQLException {
        String sql = "SELECT column_name FROM information_schema.columns " +
                     "WHERE table_name = ? AND column_name = ANY(?) LIMIT 1";
        try (var ps = c.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setArray(2, c.createArrayOf("text", candidates));
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return candidates[0]; // fallback al primero
    }

    /** Inserta el usuario de prueba en users DB. */
    protected static void seedUsersUser() {
        try (Connection c = connectTo("users")) {
            run(c,
                "INSERT INTO users (user_id, user_name, phone) " +
                "VALUES ('" + TEST_USER_ID + "', '" + TEST_USER_NAME + "', '3001234567') ON CONFLICT DO NOTHING");
        } catch (SQLException e) { throw new RuntimeException("seedUsersUser falló: " + e.getMessage(), e); }
    }

    /** Inserta una tienda de prueba en store DB. */
    protected static void seedStore() {
        try (Connection c = connectTo("store")) {
            run(c,
                "INSERT INTO store (store_id, owner_id, name, slug, description, is_active, created_at) " +
                "VALUES ('" + TEST_STORE_ID + "', '" + TEST_USER_ID + "', " +
                "'E2E Store', '" + TEST_STORE_SLUG + "', 'Tienda de pruebas', true, NOW()) " +
                "ON CONFLICT DO NOTHING");
        } catch (SQLException e) { throw new RuntimeException("seedStore falló: " + e.getMessage(), e); }
    }

    // ─── Limpieza de BD ──────────────────────────────────────────────────────

    protected static void cleanupAuthUser() {
        try (Connection c = connectTo("auth")) {
            run(c, "DELETE FROM user_role WHERE user_id = '" + TEST_USER_ID + "'");
            run(c, "DELETE FROM verification WHERE user_user_id = '" + TEST_USER_ID + "'");
            run(c, "DELETE FROM app_user WHERE user_id = '" + TEST_USER_ID + "'");
        } catch (SQLException e) { System.err.println("cleanupAuthUser warning: " + e.getMessage()); }
    }

    protected static void cleanupUsersUser() {
        try (Connection c = connectTo("users")) {
            run(c, "DELETE FROM users WHERE user_id = '" + TEST_USER_ID + "'");
        } catch (SQLException e) { System.err.println("cleanupUsersUser warning: " + e.getMessage()); }
    }

    protected static void cleanupStore() {
        try (Connection c = connectTo("store")) {
            run(c, "DELETE FROM store_user WHERE store_id = '" + TEST_STORE_ID + "'");
            run(c, "DELETE FROM store WHERE store_id = '" + TEST_STORE_ID + "'");
        } catch (SQLException e) { System.err.println("cleanupStore warning: " + e.getMessage()); }
    }

    /** Inserta la tienda de prueba en la BD del cart-service (tiene su propia tabla store). */
    protected static void seedCartStore() {
        try (Connection c = connectTo("cart")) {
            run(c,
                "INSERT INTO store (store_id) " +
                "VALUES ('" + TEST_STORE_ID + "') ON CONFLICT DO NOTHING");
        } catch (SQLException e) { System.err.println("seedCartStore warning: " + e.getMessage()); }
    }

    protected static void cleanupCartStore() {
        try (Connection c = connectTo("cart")) {
            run(c, "DELETE FROM cart_item WHERE cart_id IN " +
                "(SELECT cart_id FROM cart WHERE store_id = '" + TEST_STORE_ID + "')");
            run(c, "DELETE FROM cart WHERE store_id = '" + TEST_STORE_ID + "'");
            run(c, "DELETE FROM store WHERE store_id = '" + TEST_STORE_ID + "'");
        } catch (SQLException e) { System.err.println("cleanupCartStore warning: " + e.getMessage()); }
    }
}

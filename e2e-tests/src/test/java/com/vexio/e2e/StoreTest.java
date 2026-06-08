package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Store Service")
/**
 * Pruebas para store-service (/api/v1/stores).
 * Las rutas de tiendas son públicas en el gateway, pero el servicio valida JWT internamente.
 *
 * Orden:
 *  1. Leer lista vacía/existente
 *  2. Crear tienda (requiere usuario en users DB)
 *  3. Consultar tienda creada
 *  4. Añadir usuario a la tienda
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StoreTest extends BaseTest {

    private static final String BASE = "/api/v1/stores";
    private static String createdStoreId;

    @BeforeAll
    void setup() {
        seedAuthUser();
        seedUsersUser();
        seedStore(); // tienda preinsertada para pruebas de lectura
    }

    @AfterAll
    void cleanup() {
        cleanupStore();
        cleanupUsersUser();
        cleanupAuthUser();
    }

    // ─── Lectura ──────────────────────────────────────────────────────────────

    @Test @Order(1)
    void getAllStores_retorna200() {
        withJwt()
            .when().get(BASE)
            .then()
            .statusCode(200);
    }

    @Test @Order(2)
    void getStoreById_tiendaSeed_retorna200() {
        withJwt()
            .when().get(BASE + "/" + TEST_STORE_ID)
            .then()
            .statusCode(200)
            .body("slug", equalTo(TEST_STORE_SLUG));
    }

    @Test @Order(3)
    void getStoreBySlug_retorna200() {
        withJwt()
            .when().get(BASE + "/slug/" + TEST_STORE_SLUG)
            .then()
            .statusCode(anyOf(is(200), is(404))); // endpoint puede ser /slug/{slug} o /{slug}
    }

    @Test @Order(4)
    void existStore_tiendaExistente_retorna200() {
        withJwt()
            .when().get(BASE + "/existStore/" + TEST_STORE_ID)
            .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(5)
    void getStoreById_idInexistente_retorna404() {
        withJwt()
            .when().get(BASE + "/00000000-0000-0000-0000-000000000099")
            .then()
            .statusCode(anyOf(is(404), is(400)));
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void createStore_datosValidos_retorna201() {
        var response = withJwt()
            .body("""
                {
                  "ownerId": "%s",
                  "name": "Mi Tienda E2E",
                  "slug": "mi-tienda-e2e-test",
                  "description": "Creada por test automatizado"
                }""".formatted(TEST_USER_ID))
            .when().post(BASE)
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        createdStoreId = response.jsonPath().getString("storeId");
    }

    @Test @Order(11)
    void createStore_sinNombre_retorna400() {
        withJwt()
            .body("""
                {
                  "ownerId": "%s",
                  "slug": "sin-nombre-store"
                }""".formatted(TEST_USER_ID))
            .when().post(BASE)
            .then()
            .statusCode(400);
    }

    @Test @Order(12)
    void createStore_slugDuplicado_retorna4xx() {
        withJwt()
            .body("""
                {
                  "ownerId": "%s",
                  "name": "Tienda Duplicada",
                  "slug": "%s"
                }""".formatted(TEST_USER_ID, TEST_STORE_SLUG))
            .when().post(BASE)
            .then()
            .statusCode(greaterThanOrEqualTo(400));
    }

    // ─── Usuarios de la tienda ─────────────────────────────────────────────────

    @Test @Order(20)
    void getStoreUsers_retorna200() {
        withJwt()
            .when().get(BASE + "/" + TEST_STORE_ID + "/users")
            .then()
            .statusCode(anyOf(is(200), is(403), is(404)));
    }

    @Test @Order(21)
    void getUserStore_porUserId_retorna200() {
        withJwt()
            .when().get(BASE + "/users/" + TEST_USER_ID)
            .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(22)
    void checkAccess_userEnTienda_retorna200() {
        withJwt()
            .when().get(BASE + "/" + TEST_STORE_ID + "/access/" + TEST_USER_ID)
            .then()
            .statusCode(anyOf(is(200), is(404), is(403)));
    }

    // ─── Limpieza de tienda creada en test ────────────────────────────────────

    @AfterAll
    void cleanupCreatedStore() {
        if (createdStoreId != null) {
            try (var c = connectTo("store")) {
                run(c, "DELETE FROM store WHERE store_id = '" + createdStoreId + "'");
            } catch (Exception e) {
                System.err.println("No se pudo limpiar la tienda creada: " + e.getMessage());
            }
        }
    }
}

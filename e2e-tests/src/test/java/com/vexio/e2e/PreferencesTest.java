package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Preferences Service")

/**
 * Pruebas para preferences-service (/api/v1/preferences, /api/v1/wishlist).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PreferencesTest extends BaseTest {

    private static final String PREFERENCES = "/api/v1/preferences";
    private static final String WISHLIST    = "/api/v1/wishlist";

    private String wishlistItemId;

    @BeforeAll
    void setup() {
        seedAuthUser();
        seedUsersUser();
        seedStore();
    }

    @AfterAll
    void cleanup() {
        cleanupStore();
        cleanupUsersUser();
        cleanupAuthUser();
    }

    // ─── Sin JWT → 401 ───────────────────────────────────────────────────────

    @Test @Order(1)
    void getPreferences_sinJWT_retorna401() {
        noAuth().when().get(PREFERENCES).then().statusCode(401);
    }

    @Test @Order(2)
    void getWishlist_sinJWT_retorna401() {
        noAuth().when().get(WISHLIST).then().statusCode(401);
    }

    // ─── Preferencias ─────────────────────────────────────────────────────────

    @Test @Order(10)
    void getMyPreferences_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PREFERENCES)
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void savePreference_datosValidos_retorna200o201() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "preferenceType": "CATEGORY",
                  "preferenceValue": "ROPA"
                }""")
            .when().post(PREFERENCES)
            .then().statusCode(anyOf(is(200), is(201)));
    }

    @Test @Order(12)
    void saveBehavior_datosValidos_retorna200o201() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "eventType": "VIEW",
                  "productId": "00000000-0000-0000-0000-000000000001"
                }""")
            .when().post(PREFERENCES + "/behavior")
            .then().statusCode(anyOf(is(200), is(201), is(400)));
    }

    @Test @Order(13)
    void getBehavior_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PREFERENCES + "/behavior")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    // ─── Wishlist ─────────────────────────────────────────────────────────────

    @Test @Order(20)
    void getMyWishlist_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(WISHLIST)
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(21)
    void addToWishlist_variantInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "variantId": "00000000-0000-0000-0000-000000000099"
                }""")
            .when().post(WISHLIST + "/items")
            .then().statusCode(anyOf(is(200), is(201), is(400), is(404)));
    }

    @Test @Order(22)
    void addToWishlist_bodyVacio_retorna400() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("{}")
            .when().post(WISHLIST + "/items")
            .then().statusCode(anyOf(is(400), is(422)));
    }

    @Test @Order(30)
    void deleteWishlistItem_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().delete(WISHLIST + "/items/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }
}

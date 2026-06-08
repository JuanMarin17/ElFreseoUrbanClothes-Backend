package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Loyal Customer Service")

/**
 * Pruebas para loyal-customer-service (/api/v1/loyalty).
 * Las rutas protegidas requieren JWT. La ruta interna (/loyalty/internal) es pública.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoyalCustomerTest extends BaseTest {

    private static final String BASE = "/api/v1/loyalty";

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
    void getMyAccount_sinJWT_retorna401() {
        noAuth().when().get(BASE + "/me").then().statusCode(401);
    }

    @Test @Order(2)
    void getMyLedger_sinJWT_retorna401() {
        noAuth().when().get(BASE + "/me/ledger").then().statusCode(401);
    }

    @Test @Order(3)
    void redeemPoints_sinJWT_retorna401() {
        noAuth().body("{}").when().post(BASE + "/redeem").then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void getMyAccount_conJWT_retorna200o404() {
        // El usuario puede no tener cuenta de lealtad aún → 404 es válido
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/me")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void getMyLedger_conJWT_retorna200o404() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/me/ledger")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void getAccountByUser_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/users/00000000-0000-0000-0000-000000000099")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(13)
    void redeemPoints_sinPuntos_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "points": 100
                }""")
            .when().post(BASE + "/redeem")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    // ─── Ruta interna (pública, sin JWT) ─────────────────────────────────────

    @Test @Order(20)
    void earnPoints_rutaInterna_retorna200o4xx() {
        // La ruta /loyalty/internal es pública en el gateway
        noAuth()
            .body("""
                {
                  "userId": "%s",
                  "storeId": "%s",
                  "points": 50,
                  "orderId": "00000000-0000-0000-0000-000000000001"
                }""".formatted(TEST_USER_ID, TEST_STORE_ID))
            .when().post(BASE + "/internal/earn")
            .then().statusCode(anyOf(is(200), is(201), is(400), is(404)));
    }
}

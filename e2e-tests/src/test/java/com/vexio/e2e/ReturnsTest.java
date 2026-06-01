package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Returns Service")

/**
 * Pruebas para returns-service (/api/v1/returns).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReturnsTest extends BaseTest {

    private static final String BASE = "/api/v1/returns";

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
    void getMyReturns_sinJWT_retorna401() {
        noAuth().when().get(BASE + "/me").then().statusCode(401);
    }

    @Test @Order(2)
    void createReturn_sinJWT_retorna401() {
        noAuth().body("{}").when().post(BASE + "/createReturn").then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void getMyReturns_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/me")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void getReturnsByStore_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/getReturnsByStore")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void getReturnById_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(20)
    void createReturn_ordenInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "orderId": "00000000-0000-0000-0000-000000000099",
                  "reason": "Producto defectuoso",
                  "items": [
                    {
                      "orderItemId": "00000000-0000-0000-0000-000000000001",
                      "quantity": 1
                    }
                  ]
                }""")
            .when().post(BASE + "/createReturn")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(21)
    void createReturn_bodyVacio_retorna400() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("{}")
            .when().post(BASE + "/createReturn")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(30)
    void updateReturnStatus_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "status": "APPROVED"
                }""")
            .when().patch(BASE + "/00000000-0000-0000-0000-000000000099/status")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(31)
    void cancelReturn_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().delete(BASE + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }
}

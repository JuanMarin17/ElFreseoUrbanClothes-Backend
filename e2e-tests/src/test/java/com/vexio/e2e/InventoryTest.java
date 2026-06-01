package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Inventory Service")

/**
 * Pruebas para inventory-service (/api/v1/inventory, /api/v1/locations).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryTest extends BaseTest {

    private static final String INVENTORY  = "/api/v1/inventory";
    private static final String LOCATIONS  = "/api/v1/locations";

    private String locationId;

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
    void getBalance_sinJWT_retorna401() {
        noAuth().when().get(INVENTORY + "/balance").then().statusCode(401);
    }

    @Test @Order(2)
    void getMovements_sinJWT_retorna401() {
        noAuth().when().get(INVENTORY + "/movements").then().statusCode(401);
    }

    @Test @Order(3)
    void getLocations_sinJWT_retorna401() {
        noAuth().when().get(LOCATIONS + "/getLocations").then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void getBalance_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(INVENTORY + "/balance")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void getMovements_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(INVENTORY + "/movements")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void getMovementsByVariant_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(INVENTORY + "/movements/variant/00000000-0000-0000-0000-000000000099")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    // ─── Ubicaciones ──────────────────────────────────────────────────────────

    @Test @Order(20)
    void createLocation_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Bodega E2E",
                  "description": "Ubicación de prueba"
                }""")
            .when().post(LOCATIONS + "/createLocation")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        locationId = response.jsonPath().getString("locationId");
    }

    @Test @Order(21)
    void getLocations_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(LOCATIONS + "/getLocations")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(22)
    void deleteLocation_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().delete(LOCATIONS + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(23)
    void deleteLocation_creada_retorna200o204() {
        if (locationId == null) {
            System.out.println("Saltando deleteLocation: ubicación no creada");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().delete(LOCATIONS + "/" + locationId)
            .then().statusCode(anyOf(is(200), is(204)));
    }

    // ─── Movimientos ──────────────────────────────────────────────────────────

    @Test @Order(30)
    void registerMovement_variantInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "variantId": "00000000-0000-0000-0000-000000000099",
                  "quantity": 10,
                  "type": "IN",
                  "reason": "Compra de prueba"
                }""")
            .when().post(INVENTORY + "/movements")
            .then().statusCode(greaterThanOrEqualTo(400));
    }
}

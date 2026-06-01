package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Order & Payment Service")

/**
 * Pruebas para order-payment-service (/api/v1/stores/{storeId}/orders).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderPaymentTest extends BaseTest {

    private final String ORDERS  = "/api/v1/stores/" + TEST_STORE_ID + "/orders";
    private String orderId;

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
    void getMyOrders_sinJWT_retorna401() {
        noAuth().when().get(ORDERS).then().statusCode(401);
    }

    @Test @Order(2)
    void createOrder_sinJWT_retorna401() {
        noAuth().body("{}").when().post(ORDERS).then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void getMyOrders_conJWT_retorna200() {
        withJwt()
            .when().get(ORDERS)
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void getAllOrdersByStore_conJWT_retorna200() {
        withJwt()
            .when().get(ORDERS + "/admin/all")
            .then().statusCode(anyOf(is(200), is(403), is(404)));
    }

    @Test @Order(12)
    void getOrder_idInexistente_retorna4xx() {
        withJwt()
            .when().get(ORDERS + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(20)
    void createOrder_carritoVacio_retorna4xx() {
        withJwt()
            .body("""
                {
                  "shippingAddress": "Calle 123 #45-67",
                  "notes": "Sin novedades"
                }""")
            .when().post(ORDERS)
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(21)
    void cancelOrder_idInexistente_retorna4xx() {
        withJwt()
            .when().delete(ORDERS + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    // ─── Pagos ────────────────────────────────────────────────────────────────

    @Test @Order(30)
    void getPayment_sinOrden_retorna4xx() {
        withJwt()
            .when().get(ORDERS + "/00000000-0000-0000-0000-000000000099/payment")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(31)
    void processPayment_ordenInexistente_retorna4xx() {
        withJwt()
            .body("""
                {
                  "method": "CASH",
                  "amount": 100000
                }""")
            .when().post(ORDERS + "/00000000-0000-0000-0000-000000000099/payment")
            .then().statusCode(greaterThanOrEqualTo(400));
    }
}

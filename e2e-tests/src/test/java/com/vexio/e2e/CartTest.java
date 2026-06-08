package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Epic("Vexio Microservicios")
@Feature("Cart Service")
/**
 * Pruebas para cart-service.
 *
 * Cart NO está en el gateway → acceso directo a localhost:8086
 * El servicio lee el usuario del header "x-user-id".
 *
 * Ruta: /api/v1/api/stores/{storeId}/cart
 * (context-path = /api/v1  +  @RequestMapping = /api/stores/{storeId}/cart)
 *
 * Si el cart-service no responde, todos los tests se saltan automáticamente (SKIPPED).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CartTest extends BaseTest {

    private static final String CART_PATH =
            "/api/v1/api/stores/" + TEST_STORE_ID + "/cart";

    @BeforeAll
    void setup() {
        // Healthcheck con context-path correcto + verificación de HTTP 2xx
        // (solo verificar TCP no es suficiente; el servicio puede aceptar conexiones
        //  pero colgar si la BD no está lista)
        assumeTrue(
            isReachable(CART_DIRECT + "/api/v1/actuator/health"),
            "cart-service no disponible o no saludable en " + CART_DIRECT + " — tests saltados"
        );
        seedAuthUser();
        seedUsersUser();
        seedStore();
        seedCartStore(); // la BD del cart tiene su propia tabla "store"
    }

    @AfterAll
    void cleanup() {
        cleanupCartStore();
        cleanupStore();
        cleanupUsersUser();
        cleanupAuthUser();
    }

    private RequestSpecification cartWithUser() {
        return RestAssured.given()
                .config(RA_CONFIG)
                .baseUri(CART_DIRECT)
                .contentType(ContentType.JSON)
                .header("x-user-id", TEST_USER_ID.toString());
    }

    private RequestSpecification cartNoUser() {
        return RestAssured.given()
                .config(RA_CONFIG)
                .baseUri(CART_DIRECT)
                .contentType(ContentType.JSON);
    }

    // ─── Sin header x-user-id ────────────────────────────────────────────────

    @Test @Order(1)
    void getCart_sinUserId_retorna4xx() {
        cartNoUser()
            .when().get(CART_PATH)
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(2)
    void addItem_sinUserId_retorna4xx() {
        cartNoUser()
            .body("""
                {
                  "productId": "00000000-0000-0000-0000-000000000001",
                  "quantity": 1
                }""")
            .when().post(CART_PATH + "/items")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    // ─── Con header x-user-id ─────────────────────────────────────────────────

    @Test @Order(10)
    void getCart_conUserId_retorna200oVacio() {
        cartWithUser()
            .when().get(CART_PATH)
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void addItem_productoInexistente_retorna4xx() {
        cartWithUser()
            .body("""
                {
                  "productId": "00000000-0000-0000-0000-000000000099",
                  "quantity": 1
                }""")
            .when().post(CART_PATH + "/items")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(12)
    void addItem_cantidadCero_retorna400() {
        cartWithUser()
            .body("""
                {
                  "productId": "00000000-0000-0000-0000-000000000001",
                  "quantity": 0
                }""")
            .when().post(CART_PATH + "/items")
            .then().statusCode(400);
    }

    @Test @Order(13)
    void addItem_bodyVacio_retorna400() {
        cartWithUser()
            .body("{}")
            .when().post(CART_PATH + "/items")
            .then().statusCode(400);
    }

    @Test @Order(20)
    void clearCart_conUserId_retorna204o200() {
        cartWithUser()
            .when().delete(CART_PATH)
            .then().statusCode(anyOf(is(200), is(204), is(404)));
    }

    @Test @Order(21)
    void removeItem_idInexistente_retorna4xx() {
        cartWithUser()
            .when().delete(CART_PATH + "/items/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(22)
    void updateItem_cantidad0_elimina() {
        cartWithUser()
            .body("""
                {
                  "quantity": 0
                }""")
            .when().put(CART_PATH + "/items/00000000-0000-0000-0000-000000000099")
            .then().statusCode(anyOf(is(200), is(400), is(404)));
    }
}

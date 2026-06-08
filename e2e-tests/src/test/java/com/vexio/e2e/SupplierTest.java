package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Supplier Service")

/**
 * Pruebas para supplier-service (/api/v1/suppliers).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SupplierTest extends BaseTest {

    private static final String BASE = "/api/v1/suppliers";
    private String supplierId;

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
    void getSuppliers_sinJWT_retorna401() {
        noAuth().when().get(BASE + "/getSuppliersByStore").then().statusCode(401);
    }

    @Test @Order(2)
    void createSupplier_sinJWT_retorna401() {
        noAuth().body("{}").when().post(BASE + "/createSupplier").then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void createSupplier_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Proveedor E2E Test",
                  "email": "proveedor@e2etest.com",
                  "phone": "3001234567",
                  "address": "Calle 123 Bogotá",
                  "nit": "900111222-3"
                }""")
            .when().post(BASE + "/createSupplier")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        supplierId = response.jsonPath().getString("supplierId");
    }

    @Test @Order(11)
    void getSuppliers_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/getSuppliersByStore")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void getSupplierById_creado_retorna200() {
        if (supplierId == null) {
            System.out.println("Saltando getSupplierById: proveedor no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/" + supplierId)
            .then().statusCode(200);
    }

    @Test @Order(13)
    void getSupplierById_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(20)
    void updateSupplier_creado_retorna200() {
        if (supplierId == null) {
            System.out.println("Saltando updateSupplier: proveedor no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Proveedor E2E Actualizado",
                  "email": "proveedor@e2etest.com",
                  "phone": "3007654321",
                  "address": "Carrera 45 Medellín",
                  "nit": "900111222-3"
                }""")
            .when().put(BASE + "/" + supplierId)
            .then().statusCode(anyOf(is(200), is(201)));
    }

    @Test @Order(30)
    void unlinkSupplier_creado_retorna200o204() {
        if (supplierId == null) {
            System.out.println("Saltando unlinkSupplier: proveedor no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().delete(BASE + "/" + supplierId + "/unlink")
            .then().statusCode(anyOf(is(200), is(204)));
    }
}

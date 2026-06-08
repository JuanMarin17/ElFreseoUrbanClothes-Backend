package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Users Service")
/**
 * Pruebas para users-service (/api/v1/users).
 * Requiere JWT en todas las rutas (gateway aplica JwtValidationFilter).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersTest extends BaseTest {

    private static final String BASE = "/api/v1/users";

    @BeforeAll
    void setup() {
        seedAuthUser();
        seedUsersUser();
    }

    @AfterAll
    void cleanup() {
        cleanupUsersUser();
        cleanupAuthUser();
    }

    // ─── Sin JWT → 401 ───────────────────────────────────────────────────────

    @Test @Order(1)
    void getProfile_sinJWT_retorna401() {
        noAuth()
            .when().get(BASE + "/me")
            .then().statusCode(401);
    }

    @Test @Order(2)
    void updateProfile_sinJWT_retorna401() {
        noAuth()
            .body("{}")
            .when().put(BASE + "/update")
            .then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void getProfile_conJWT_retorna200() {
        withJwt()
            .when().get(BASE + "/me")
            .then()
            .statusCode(200)
            .body("userName", equalTo(TEST_USER_NAME));
    }

    @Test @Order(11)
    void getUserById_conJWT_retornaUsuario() {
        withJwt()
            .when().get(BASE + "/getUserById/" + TEST_USER_ID)
            .then()
            .statusCode(200)
            .body("userName", equalTo(TEST_USER_NAME));
    }

    @Test @Order(12)
    void existUser_conJWT_retornaTrue() {
        withJwt()
            .when().get(BASE + "/existUser/" + TEST_USER_ID)
            .then()
            .statusCode(200);
    }

    @Test @Order(13)
    void getUserByName_conJWT_retornaNombre() {
        withJwt()
            .when().get(BASE + "/myUserName/" + TEST_USER_ID)
            .then()
            .statusCode(200);
    }

    @Test @Order(20)
    void updateProfile_conJWT_retorna200() {
        withJwt()
            .body("""
                {
                  "userId": "%s",
                  "userName": "%s",
                  "phone": "3009876543"
                }""".formatted(TEST_USER_ID, TEST_USER_NAME))
            .when().put(BASE + "/update")
            .then()
            .statusCode(anyOf(is(200), is(201)));
    }
}

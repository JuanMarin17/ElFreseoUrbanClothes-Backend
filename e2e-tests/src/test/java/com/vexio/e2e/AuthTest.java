package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Auth Service")
/**
 * Pruebas para auth-service (/api/v1/auth).
 *
 * Se excluyen los pasos que requieren OTP por email:
 *   - registerSecondStep
 *   - loginSecondStep
 *   - forgotPasswordSecondStep
 *
 * Detecta automáticamente si el gateway (8080) está disponible.
 * Si no responde, llama directo al auth-service (8082).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthTest extends BaseTest {

    private static final String AUTH_DIRECT = "http://localhost:8082";
    private static final String BASE        = "/api/v1/auth";

    // Detecta una sola vez qué base URL usar
    private static final String BASE_URL;

    static {
        String url = GATEWAY;
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                new java.net.URL(GATEWAY + "/api/v1/auth/getEmailById/00000000-0000-0000-0000-000000000000")
                    .openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);
            conn.connect();
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("[AuthTest] Gateway no disponible, usando auth-service directo en " + AUTH_DIRECT);
            url = AUTH_DIRECT;
        }
        BASE_URL = url;
    }

    private RequestSpecification req() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON);
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    @Test @Order(1)
    void register_bodyVacio_retorna400() {
        req().body("{}").when().post(BASE + "/register").then().statusCode(400);
    }

    @Test @Order(2)
    void register_emailInvalido_retorna400() {
        req()
            .body("""
                {
                  "userName": "testuser",
                  "email": "esto-no-es-un-email",
                  "password": "password123",
                  "phone": "3001234567",
                  "imageProfile": "https://example.com/img.jpg"
                }""")
            .when().post(BASE + "/register")
            .then().statusCode(400);
    }

    @Test @Order(3)
    void register_passwordCorta_retorna400() {
        req()
            .body("""
                {
                  "userName": "testuser",
                  "email": "valido@email.com",
                  "password": "corta",
                  "phone": "3001234567",
                  "imageProfile": "https://example.com/img.jpg"
                }""")
            .when().post(BASE + "/register")
            .then().statusCode(400);
    }

    @Test @Order(4)
    void register_telefonoInvalido_retorna400() {
        req()
            .body("""
                {
                  "userName": "testuser",
                  "email": "valido@email.com",
                  "password": "password123",
                  "phone": "abc-no-phone",
                  "imageProfile": "https://example.com/img.jpg"
                }""")
            .when().post(BASE + "/register")
            .then().statusCode(400);
    }

    @Test @Order(5)
    void register_sinUserName_retorna400() {
        req()
            .body("""
                {
                  "email": "valido@email.com",
                  "password": "password123",
                  "phone": "3001234567",
                  "imageProfile": "https://example.com/img.jpg"
                }""")
            .when().post(BASE + "/register")
            .then().statusCode(400);
    }

    @Test @Order(6)
    void register_sinImageProfile_retorna400() {
        req()
            .body("""
                {
                  "userName": "testuser",
                  "email": "valido@email.com",
                  "password": "password123",
                  "phone": "3001234567"
                }""")
            .when().post(BASE + "/register")
            .then().statusCode(400);
    }

    @Test @Order(7)
    void register_userNameDemasiadoCorto_retorna400() {
        req()
            .body("""
                {
                  "userName": "ab",
                  "email": "valido@email.com",
                  "password": "password123",
                  "phone": "3001234567",
                  "imageProfile": "https://example.com/img.jpg"
                }""")
            .when().post(BASE + "/register")
            .then().statusCode(400);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Test @Order(10)
    void login_sinEmail_retorna400() {
        req()
            .body("""
                {
                  "password": "cualquierPassword123"
                }""")
            .when().post(BASE + "/login")
            .then().statusCode(400);
    }

    @Test @Order(11)
    void login_sinPassword_retorna400() {
        req()
            .body("""
                {
                  "email": "valido@email.com"
                }""")
            .when().post(BASE + "/login")
            .then().statusCode(400);
    }

    @Test @Order(12)
    void login_emailInvalido_retorna400() {
        req()
            .body("""
                {
                  "email": "no-es-email",
                  "password": "cualquierPassword123"
                }""")
            .when().post(BASE + "/login")
            .then().statusCode(400);
    }

    @Test @Order(13)
    void login_emailInexistente_retorna4xx() {
        req()
            .body("""
                {
                  "email": "noexiste99999@nowhere.com",
                  "password": "password123"
                }""")
            .when().post(BASE + "/login")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(14)
    void login_bodyVacio_retorna400() {
        req().body("{}").when().post(BASE + "/login").then().statusCode(400);
    }

    // ─── Forgot Password ──────────────────────────────────────────────────────

    @Test @Order(20)
    void forgotPassword_emailInexistente_retorna4xx() {
        req()
            .body("""
                {
                  "email": "noexiste99999@nowhere.com"
                }""")
            .when().post(BASE + "/forgotPassword")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(21)
    void forgotPassword_emailInvalido_retorna400() {
        req()
            .body("""
                {
                  "email": "no-es-email"
                }""")
            .when().post(BASE + "/forgotPassword")
            .then().statusCode(400);
    }

    // ─── Resend Code ──────────────────────────────────────────────────────────

    @Test @Order(30)
    void resendCode_emailInexistente_retorna4xx() {
        req()
            .body("""
                {
                  "email": "noexiste99999@nowhere.com"
                }""")
            .when().post(BASE + "/resendVerificationCode")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    // ─── Get Email By Id ──────────────────────────────────────────────────────

    @Test @Order(40)
    void getEmailById_idInexistente_retorna4xx() {
        req()
            .when().get(BASE + "/getEmailById/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    // ─── Deactivate Account ───────────────────────────────────────────────────

    @Test @Order(50)
    void deactivateAccount_sinJWT_retorna401() {
        req()
            .when().delete(BASE + "/deactivateAccount")
            .then().statusCode(anyOf(is(401), is(403)));
    }
}

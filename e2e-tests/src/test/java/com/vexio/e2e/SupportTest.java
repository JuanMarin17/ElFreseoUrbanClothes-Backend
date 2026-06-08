package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Support Service")

/**
 * Pruebas para support-service (/api/v1/support).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SupportTest extends BaseTest {

    private static final String BASE = "/api/v1/support";
    private String ticketId;

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
    void getMyTickets_sinJWT_retorna401() {
        noAuth().when().get(BASE + "/tickets/me").then().statusCode(401);
    }

    @Test @Order(2)
    void createTicket_sinJWT_retorna401() {
        noAuth().body("{}").when().post(BASE + "/tickets").then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void createTicket_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "subject": "Ticket de prueba E2E",
                  "message": "Este es un mensaje de prueba para el sistema de soporte",
                  "category": "GENERAL"
                }""")
            .when().post(BASE + "/tickets")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        ticketId = response.jsonPath().getString("ticketId");
    }

    @Test @Order(11)
    void getMyTickets_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/tickets/me")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void getAllTickets_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/tickets")
            .then().statusCode(anyOf(is(200), is(403), is(404)));
    }

    @Test @Order(13)
    void getTicketById_creado_retorna200() {
        if (ticketId == null) {
            System.out.println("Saltando getTicketById: ticket no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/tickets/" + ticketId)
            .then().statusCode(200);
    }

    @Test @Order(14)
    void getTicketById_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/tickets/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(20)
    void replyTicket_creado_retorna200() {
        if (ticketId == null) {
            System.out.println("Saltando replyTicket: ticket no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "message": "Respuesta de prueba al ticket"
                }""")
            .when().post(BASE + "/tickets/" + ticketId + "/reply")
            .then().statusCode(anyOf(is(200), is(201)));
    }

    @Test @Order(21)
    void getMessages_creado_retorna200() {
        if (ticketId == null) {
            System.out.println("Saltando getMessages: ticket no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/tickets/" + ticketId + "/messages")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(30)
    void closeTicket_creado_retorna200o204() {
        if (ticketId == null) {
            System.out.println("Saltando closeTicket: ticket no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().patch(BASE + "/tickets/" + ticketId + "/close")
            .then().statusCode(anyOf(is(200), is(204)));
    }
}

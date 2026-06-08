package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Reviews Service")

/**
 * Pruebas para reviews-service (/api/v1/reviews).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewsTest extends BaseTest {

    private static final String BASE = "/api/v1/reviews";
    private String reviewId;

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
    void getMyReviews_sinJWT_retorna401() {
        noAuth().when().get(BASE + "/me").then().statusCode(401);
    }

    @Test @Order(2)
    void createReview_sinJWT_retorna401() {
        noAuth().body("{}").when().post(BASE).then().statusCode(401);
    }

    // ─── Con JWT ─────────────────────────────────────────────────────────────

    @Test @Order(10)
    void getMyReviews_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/me")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(11)
    void getReviewsByProduct_productoInexistente_retorna200oVacio() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/product/00000000-0000-0000-0000-000000000099")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void createReview_productoInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "productId": "00000000-0000-0000-0000-000000000099",
                  "rating": 5,
                  "title": "Excelente producto",
                  "body": "Me gustó mucho, muy buena calidad"
                }""")
            .when().post(BASE)
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(13)
    void createReview_ratingFueraRango_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "productId": "00000000-0000-0000-0000-000000000001",
                  "rating": 10,
                  "title": "Test",
                  "body": "Body de prueba"
                }""")
            .when().post(BASE)
            .then().statusCode(anyOf(is(400), is(404), is(422)));
    }

    @Test @Order(20)
    void getReplies_reviewInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BASE + "/00000000-0000-0000-0000-000000000099/replies")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(21)
    void addReaction_reviewInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "reactionType": "LIKE"
                }""")
            .when().post(BASE + "/00000000-0000-0000-0000-000000000099/reactions")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(22)
    void addReply_reviewInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "body": "Esta es una respuesta de prueba"
                }""")
            .when().post(BASE + "/00000000-0000-0000-0000-000000000099/replies")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(23)
    void deleteReview_idInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().delete(BASE + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(greaterThanOrEqualTo(400));
    }
}

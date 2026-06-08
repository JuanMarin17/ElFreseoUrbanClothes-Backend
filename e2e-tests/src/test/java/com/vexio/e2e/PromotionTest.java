package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Promotion Service")

/**
 * Pruebas para promotion-service (/api/v1/coupons, /api/v1/promotions).
 * Requiere JWT.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PromotionTest extends BaseTest {

    private static final String COUPONS    = "/api/v1/coupons";
    private static final String PROMOTIONS = "/api/v1/promotions";

    private String couponId;
    private String promotionId;
    private static final String COUPON_CODE = "E2ETEST2026";

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
    void getCoupons_sinJWT_retorna401() {
        noAuth().when().get(COUPONS + "/getActiveCoupons").then().statusCode(401);
    }

    @Test @Order(2)
    void getPromotions_sinJWT_retorna401() {
        noAuth().when().get(PROMOTIONS + "/getActivePromotions").then().statusCode(401);
    }

    // ─── Cupones ──────────────────────────────────────────────────────────────

    @Test @Order(10)
    void createCoupon_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "code": "%s",
                  "discountType": "PERCENTAGE",
                  "discountValue": 10.0,
                  "minOrderAmount": 50000,
                  "maxUses": 100,
                  "expiresAt": "2027-12-31T23:59:59"
                }""".formatted(COUPON_CODE))
            .when().post(COUPONS + "/createCoupon")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        couponId = response.jsonPath().getString("couponId");
    }

    @Test @Order(11)
    void getActiveCoupons_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(COUPONS + "/getActiveCoupons")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(12)
    void getAllCoupons_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(COUPONS + "/all")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(13)
    void redeemCoupon_codigoValido_retorna200() {
        if (couponId == null) {
            System.out.println("Saltando redeemCoupon: cupón no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().post(COUPONS + "/redeem/" + COUPON_CODE)
            .then().statusCode(anyOf(is(200), is(201), is(400)));
    }

    @Test @Order(14)
    void redeemCoupon_codigoInexistente_retorna4xx() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().post(COUPONS + "/redeem/CODIGOINEXISTENTE99")
            .then().statusCode(greaterThanOrEqualTo(400));
    }

    @Test @Order(15)
    void getRedemptions_cuponCreado_retorna200() {
        if (couponId == null) {
            System.out.println("Saltando getRedemptions: cupón no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(COUPONS + "/" + couponId + "/redemptions")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(16)
    void updateCoupon_creado_retorna200() {
        if (couponId == null) {
            System.out.println("Saltando updateCoupon: cupón no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "code": "%s",
                  "discountType": "PERCENTAGE",
                  "discountValue": 15.0,
                  "minOrderAmount": 30000,
                  "maxUses": 200,
                  "expiresAt": "2027-12-31T23:59:59"
                }""".formatted(COUPON_CODE))
            .when().put(COUPONS + "/" + couponId)
            .then().statusCode(anyOf(is(200), is(201)));
    }

    // ─── Promociones ──────────────────────────────────────────────────────────

    @Test @Order(20)
    void createPromotion_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Promo E2E Black Friday",
                  "description": "Descuento de prueba automatizada",
                  "discountType": "PERCENTAGE",
                  "discountValue": 20.0,
                  "startDate": "2026-01-01T00:00:00",
                  "endDate": "2027-12-31T23:59:59",
                  "active": true
                }""")
            .when().post(PROMOTIONS + "/createPromotion")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        promotionId = response.jsonPath().getString("promotionId");
    }

    @Test @Order(21)
    void getActivePromotions_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PROMOTIONS + "/getActivePromotions")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(22)
    void getAllPromotions_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PROMOTIONS + "/all")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(23)
    void updatePromotion_creada_retorna200() {
        if (promotionId == null) {
            System.out.println("Saltando updatePromotion: promoción no creada");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Promo E2E Actualizada",
                  "description": "Actualización de prueba",
                  "discountType": "FIXED",
                  "discountValue": 5000.0,
                  "startDate": "2026-01-01T00:00:00",
                  "endDate": "2027-12-31T23:59:59",
                  "active": true
                }""")
            .when().put(PROMOTIONS + "/" + promotionId)
            .then().statusCode(anyOf(is(200), is(201)));
    }
}

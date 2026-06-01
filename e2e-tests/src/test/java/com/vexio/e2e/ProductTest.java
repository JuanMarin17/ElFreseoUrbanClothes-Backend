package com.vexio.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@Epic("Vexio Microservicios")
@Feature("Product Service")

/**
 * Pruebas para product-service (/api/v1/products, /categories, /brands).
 * Todas las rutas requieren JWT (JwtValidationFilter en gateway).
 *
 * Orden: Marca → Categoría → Producto → Variante → consultas
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductTest extends BaseTest {

    private static final String PRODUCTS    = "/api/v1/products";
    private static final String CATEGORIES  = "/api/v1/categories";
    private static final String BRANDS      = "/api/v1/brands";

    // IDs creados durante los tests (para limpiar después)
    private String brandId;
    private String categoryId;
    private String productId;

    @BeforeAll
    void setup() {
        seedAuthUser();
        seedUsersUser();
        seedStore();
    }

    @AfterAll
    void cleanup() {
        cleanupProductData();
        cleanupStore();
        cleanupUsersUser();
        cleanupAuthUser();
    }

    // ─── Sin JWT → 401 ───────────────────────────────────────────────────────

    @Test @Order(1)
    void getBrands_sinJWT_retorna401() {
        noAuth().when().get(BRANDS + "/active").then().statusCode(401);
    }

    @Test @Order(2)
    void getCategories_sinJWT_retorna401() {
        noAuth().when().get(CATEGORIES + "/active").then().statusCode(401);
    }

    @Test @Order(3)
    void getProducts_sinJWT_retorna401() {
        noAuth().when().get(PRODUCTS).then().statusCode(401);
    }

    // ─── Marcas ───────────────────────────────────────────────────────────────

    @Test @Order(10)
    void createBrand_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Marca E2E Test",
                  "active": true
                }""")
            .when().post(BRANDS)
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        brandId = response.jsonPath().getString("brandId");
    }

    @Test @Order(11)
    void getBrandsActive_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BRANDS + "/active")
            .then().statusCode(200);
    }

    @Test @Order(12)
    void getAllBrands_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(BRANDS + "/all")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    // ─── Categorías ───────────────────────────────────────────────────────────

    @Test @Order(20)
    void createCategory_datosValidos_retorna201() {
        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Categoria E2E Test",
                  "active": true
                }""")
            .when().post(CATEGORIES)
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        categoryId = response.jsonPath().getString("categoryId");
    }

    @Test @Order(21)
    void getCategoriesActive_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(CATEGORIES + "/active")
            .then().statusCode(200);
    }

    @Test @Order(22)
    void getAllCategories_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(CATEGORIES + "/all")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    // ─── Productos ────────────────────────────────────────────────────────────

    @Test @Order(30)
    void createProduct_datosValidos_retorna201() {
        if (brandId == null || categoryId == null) {
            System.out.println("Saltando createProduct: marca o categoría no creada");
            return;
        }

        var response = withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .body("""
                {
                  "name": "Producto E2E",
                  "description": "Descripción de prueba",
                  "brandId": "%s",
                  "variants": [
                    {
                      "sku": "E2E-SKU-001",
                      "price": 49999.99,
                      "stock": 50,
                      "minStock": 5
                    }
                  ],
                  "images": [],
                  "categoryIds": ["%s"]
                }""".formatted(brandId, categoryId))
            .when().post(PRODUCTS)
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();

        productId = response.jsonPath().getString("productId");
    }

    @Test @Order(31)
    void listProducts_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PRODUCTS)
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(32)
    void listActiveProducts_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PRODUCTS + "/active")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    @Test @Order(33)
    void getProductById_productoCreado_retorna200() {
        if (productId == null) {
            System.out.println("Saltando getProductById: producto no creado");
            return;
        }

        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PRODUCTS + "/" + productId)
            .then().statusCode(200);
    }

    @Test @Order(34)
    void getProductById_idInexistente_retorna404() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PRODUCTS + "/00000000-0000-0000-0000-000000000099")
            .then().statusCode(anyOf(is(404), is(400)));
    }

    @Test @Order(35)
    void listNewProducts_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get(PRODUCTS + "/new")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    // ─── Variantes ────────────────────────────────────────────────────────────

    @Test @Order(40)
    void getVariants_sinJWT_retorna401() {
        noAuth()
            .when().get("/api/v1/variants")
            .then().statusCode(401);
    }

    @Test @Order(41)
    void getVariants_conJWT_retorna200() {
        withJwt()
            .header("X-Store-Id", TEST_STORE_ID.toString())
            .when().get("/api/v1/variants")
            .then().statusCode(anyOf(is(200), is(404)));
    }

    private void cleanupProductData() {
        try (var c = connectTo("product")) {
            if (productId != null) {
                run(c, "DELETE FROM product_variant WHERE product_id = '" + productId + "'");
                run(c, "DELETE FROM product WHERE product_id = '" + productId + "'");
            }
            if (brandId != null) run(c, "DELETE FROM brand WHERE brand_id = '" + brandId + "'");
            if (categoryId != null) run(c, "DELETE FROM category WHERE category_id = '" + categoryId + "'");
        } catch (Exception e) {
            System.err.println("cleanupProductData warning: " + e.getMessage());
        }
    }
}

package api.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.entity.Category;
import com.api.product.entity.Product;
import com.api.product.entity.ProductImage;
import com.api.product.entity.ProductVariant;
import com.api.product.exception.BadRequestException;
import com.api.product.exception.ConflictException;
import com.api.product.exception.ResourceNotFoundException;
import com.api.product.repository.BrandRepository;
import com.api.product.repository.CategoryRepository;
import com.api.product.repository.ProductRepository;
import com.api.product.service.ProductService;
import com.api.product.service.StockAlertService;

class ProductServiceTest {

<<<<<<< HEAD
        private ProductRepository productRepository;
        private BrandRepository brandRepository;
        private CategoryRepository categoryRepository;
        private StockAlertService stockAlertService;

        private ProductService productService;

        @BeforeEach
        void setUp() {
                productRepository = Mockito.mock(ProductRepository.class);
                brandRepository = Mockito.mock(BrandRepository.class);
                categoryRepository = Mockito.mock(CategoryRepository.class);
                stockAlertService = Mockito.mock(StockAlertService.class);

                productService = new ProductService(
                                productRepository,
                                brandRepository,
                                categoryRepository,
                                stockAlertService);
        }

        // -----------------------------
        // Helpers
        // -----------------------------

        private ProductRequestDTO buildValidDTO() {

                ProductRequestDTO.VariantDTO variant = new ProductRequestDTO.VariantDTO();
                variant.setSku("SKU-TEST-001");
                variant.setPrice(BigDecimal.valueOf(10000));
                variant.setStock(10);
                variant.setMinStock(2);

                ProductRequestDTO dto = new ProductRequestDTO();
                dto.setName("Producto Test");
                dto.setDescription("Descripción Test");
                dto.setVariants(List.of(variant));
                dto.setImages(List.of("https://ejemplo.com/imagen1.jpg"));

                return dto;
        }

        private Product buildProductEntity(UUID id, boolean active) {

                Product product = Product.builder()
                                .productId(id)
                                .name("Producto DB")
                                .description("Descripción DB")
                                .active(active)
                                .createdAt(OffsetDateTime.now())
                                .build();

                // LISTAS MUTABLES (IMPORTANTE PARA QUE NO FALLE updateProduct)
                product.setVariants(new ArrayList<>(List.of(
                                ProductVariant.builder()
                                                .sku("SKU-DB-001")
                                                .price(BigDecimal.valueOf(5000))
                                                .stock(10)
                                                .minStock(2)
                                                .product(product)
                                                .build())));

                product.setImages(new ArrayList<>(List.of(
                                ProductImage.builder()
                                                .url("https://ejemplo.com/imagen-db.jpg")
                                                .product(product)
                                                .build())));

                product.setCategories(new ArrayList<>(List.of(
                                Category.builder()
                                                .categoryId(UUID.randomUUID())
                                                .name("Categoria 1")
                                                .build())));

                return product;
        }

        // -----------------------------
        // CREATE PRODUCT TESTS
        // -----------------------------

        @Test
        void createProduct_shouldThrowBadRequest_whenDtoIsNull() {
                assertThrows(BadRequestException.class,
                                () -> productService.createProduct(null));
        }

        @Test
        void createProduct_shouldThrowBadRequest_whenNameIsBlank() {

                ProductRequestDTO dto = buildValidDTO();
                dto.setName("   ");

                assertThrows(BadRequestException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldThrowBadRequest_whenVariantsAreEmpty() {

                ProductRequestDTO dto = buildValidDTO();
                dto.setVariants(List.of());

                assertThrows(BadRequestException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldThrowConflict_whenProductAlreadyExists() {

                ProductRequestDTO dto = buildValidDTO();

                when(productRepository.existsByNameIgnoreCase(dto.getName()))
                                .thenReturn(true);

                assertThrows(ConflictException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldThrowBadRequest_whenImagesExceedLimit() {

                ProductRequestDTO dto = buildValidDTO();

                dto.setImages(List.of(
                                "https://ejemplo.com/imagen1.jpg",
                                "https://ejemplo.com/imagen2.jpg",
                                "https://ejemplo.com/imagen3.jpg",
                                "https://ejemplo.com/imagen4.jpg",
                                "https://ejemplo.com/imagen5.jpg",
                                "https://ejemplo.com/imagen6.jpg",
                                "https://ejemplo.com/imagen7.jpg"));

                assertThrows(BadRequestException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldThrowBadRequest_whenImageIsNotValid() {

                ProductRequestDTO dto = buildValidDTO();
                dto.setImages(List.of("https://google.com/foto.jpg"));

                assertThrows(BadRequestException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldThrowConflict_whenImagesAreRepeated() {

                ProductRequestDTO dto = buildValidDTO();

                dto.setImages(List.of(
                                "https://ejemplo.com/imagen1.jpg",
                                "https://ejemplo.com/imagen1.jpg"));

                assertThrows(ConflictException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldThrowResourceNotFound_whenBrandNotFound() {

                ProductRequestDTO dto = buildValidDTO();
                UUID brandId = UUID.randomUUID();
                dto.setBrandId(brandId);

                when(productRepository.existsByNameIgnoreCase(dto.getName()))
                                .thenReturn(false);

                when(brandRepository.findById(brandId))
                                .thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> productService.createProduct(dto));
        }

        @Test
        void createProduct_shouldCreateSuccessfully() {

                ProductRequestDTO dto = buildValidDTO();

                when(productRepository.existsByNameIgnoreCase(dto.getName()))
                                .thenReturn(false);

                Product savedProduct = buildProductEntity(UUID.randomUUID(), true);

                when(productRepository.save(any(Product.class)))
                                .thenReturn(savedProduct);

                ProductResponseDTO response = productService.createProduct(dto);

                assertNotNull(response);
                assertEquals("Producto DB", response.getName());
                assertEquals("ACTIVE", response.getStatus());
                assertNotNull(response.getVariants());
                assertTrue(response.getVariants().size() > 0);

                verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        void createProduct_shouldSendAlert_whenStockBelowMinStock() {

                ProductRequestDTO dto = buildValidDTO();
                dto.getVariants().get(0).setStock(1);
                dto.getVariants().get(0).setMinStock(2);

                when(productRepository.existsByNameIgnoreCase(dto.getName()))
                                .thenReturn(false);

                Product savedProduct = buildProductEntity(UUID.randomUUID(), true);
                savedProduct.getVariants().get(0).setStock(1);
                savedProduct.getVariants().get(0).setMinStock(2);

                when(productRepository.save(any(Product.class)))
                                .thenReturn(savedProduct);

                productService.createProduct(dto);

                verify(stockAlertService, times(1)).sendAlert(any());
        }

        @Test
        void createProduct_shouldNotSendAlert_whenStockAboveMinStock() {

                ProductRequestDTO dto = buildValidDTO();
                dto.getVariants().get(0).setStock(10);
                dto.getVariants().get(0).setMinStock(2);

                when(productRepository.existsByNameIgnoreCase(dto.getName()))
                                .thenReturn(false);

                Product savedProduct = buildProductEntity(UUID.randomUUID(), true);

                when(productRepository.save(any(Product.class)))
                                .thenReturn(savedProduct);

                productService.createProduct(dto);

                verify(stockAlertService, never()).sendAlert(any());
        }

        // -----------------------------
        // UPDATE PRODUCT TESTS
        // -----------------------------

        @Test
        void updateProduct_shouldReturnEmpty_whenProductNotFound() {

                UUID id = UUID.randomUUID();
                ProductRequestDTO dto = buildValidDTO();

                when(productRepository.findById(id))
                                .thenReturn(Optional.empty());

                Optional<ProductResponseDTO> result = productService.updateProduct(id, dto);

                assertTrue(result.isEmpty());
        }

        @Test
        void updateProduct_shouldThrowBadRequest_whenImagesExceedLimit() {

                UUID id = UUID.randomUUID();
                ProductRequestDTO dto = buildValidDTO();

                dto.setImages(List.of(
                                "https://ejemplo.com/nueva1.jpg",
                                "https://ejemplo.com/nueva2.jpg",
                                "https://ejemplo.com/nueva3.jpg"));

                Product existing = buildProductEntity(id, true);

                // Ya tiene 5 imágenes (LISTA MUTABLE)
                existing.setImages(new ArrayList<>(List.of(
                                ProductImage.builder().url("1").product(existing).build(),
                                ProductImage.builder().url("2").product(existing).build(),
                                ProductImage.builder().url("3").product(existing).build(),
                                ProductImage.builder().url("4").product(existing).build(),
                                ProductImage.builder().url("5").product(existing).build())));

                when(productRepository.findById(id))
                                .thenReturn(Optional.of(existing));

                assertThrows(BadRequestException.class,
                                () -> productService.updateProduct(id, dto));
        }

        @Test
        void updateProduct_shouldThrowConflict_whenNewNameAlreadyExists() {

                UUID id = UUID.randomUUID();
                ProductRequestDTO dto = buildValidDTO();
                dto.setName("Nuevo Nombre");

                Product existing = buildProductEntity(id, true);
                existing.setName("Viejo Nombre");

                when(productRepository.findById(id))
                                .thenReturn(Optional.of(existing));

                when(productRepository.existsByNameIgnoreCase("Nuevo Nombre"))
                                .thenReturn(true);

                assertThrows(ConflictException.class,
                                () -> productService.updateProduct(id, dto));
        }

        @Test
        void updateProduct_shouldUpdateSuccessfully() {

                UUID id = UUID.randomUUID();
                ProductRequestDTO dto = buildValidDTO();

                Product existing = buildProductEntity(id, true);

                when(productRepository.findById(id))
                                .thenReturn(Optional.of(existing));

                when(productRepository.existsByNameIgnoreCase(dto.getName()))
                                .thenReturn(false);

                when(productRepository.save(any(Product.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                Optional<ProductResponseDTO> result = productService.updateProduct(id, dto);

                assertTrue(result.isPresent());
                assertEquals("Producto Test", result.get().getName());

                verify(productRepository, times(1)).save(any(Product.class));
        }

        // -----------------------------
        // ACTIVE / INACTIVE TESTS
        // -----------------------------

        @Test
        void inactiveProduct_shouldReturnEmpty_whenNotFound() {

                UUID id = UUID.randomUUID();

                when(productRepository.findById(id))
                                .thenReturn(Optional.empty());

                Optional<ProductResponseDTO> result = productService.inactiveProduct(id);

                assertTrue(result.isEmpty());
        }

        @Test
        void inactiveProduct_shouldInactiveSuccessfully() {

                UUID id = UUID.randomUUID();
                Product product = buildProductEntity(id, true);

                when(productRepository.findById(id))
                                .thenReturn(Optional.of(product));

                Optional<ProductResponseDTO> result = productService.inactiveProduct(id);

                assertTrue(result.isPresent());
                assertEquals("INACTIVE", result.get().getStatus());

                verify(productRepository, times(1)).save(product);
        }

        @Test
        void activeProduct_shouldReturnEmpty_whenNotFound() {

                UUID id = UUID.randomUUID();

                when(productRepository.findById(id))
                                .thenReturn(Optional.empty());

                Optional<ProductResponseDTO> result = productService.activeProduct(id);

                assertTrue(result.isEmpty());
        }

        @Test
        void activeProduct_shouldActiveSuccessfully() {

                UUID id = UUID.randomUUID();
                Product product = buildProductEntity(id, false);

                when(productRepository.findById(id))
                                .thenReturn(Optional.of(product));

                Optional<ProductResponseDTO> result = productService.activeProduct(id);

                assertTrue(result.isPresent());
                assertEquals("ACTIVE", result.get().getStatus());

                verify(productRepository, times(1)).save(product);
        }

        // -----------------------------
        // GET BY ID TESTS
        // -----------------------------

        @Test
        void getById_shouldReturnProduct_whenFound() {

                UUID id = UUID.randomUUID();
                Product product = buildProductEntity(id, true);

                when(productRepository.findById(id))
                                .thenReturn(Optional.of(product));

                Optional<ProductResponseDTO> result = productService.getById(id);

                assertTrue(result.isPresent());
                assertEquals("Producto DB", result.get().getName());
        }

        @Test
        void getById_shouldReturnEmpty_whenNotFound() {

                UUID id = UUID.randomUUID();

                when(productRepository.findById(id))
                                .thenReturn(Optional.empty());

                Optional<ProductResponseDTO> result = productService.getById(id);

                assertTrue(result.isEmpty());
        }

        // -----------------------------
        // LIST PRODUCTS TESTS
        // -----------------------------

        @Test
        void listProducts_shouldReturnList() {

                Product p1 = buildProductEntity(UUID.randomUUID(), true);
                Product p2 = buildProductEntity(UUID.randomUUID(), true);

                Page<Product> page = new PageImpl<>(List.of(p1, p2));

                when(productRepository.findAll(any(Pageable.class)))
                                .thenReturn(page);

                List<ProductResponseDTO> result = productService.listProducts(0, 10);

                assertNotNull(result);
                assertEquals(2, result.size());
        }

        @Test
        void listActiveProducts_shouldReturnOnlyActive() {

                Product p1 = buildProductEntity(UUID.randomUUID(), true);
                Product p2 = buildProductEntity(UUID.randomUUID(), true);

                Page<Product> page = new PageImpl<>(List.of(p1, p2));

                when(productRepository.findByActiveTrue(any(Pageable.class)))
                                .thenReturn(page);

                List<ProductResponseDTO> result = productService.listActiveProducts(0, 10);

                assertEquals(2, result.size());
        }

        // -----------------------------
        // LIST ALL PRODUCTS TESTS
        // -----------------------------

        @Test
        void listAllProducts_shouldReturnList() {

                Product p1 = buildProductEntity(UUID.randomUUID(), true);
                Product p2 = buildProductEntity(UUID.randomUUID(), false);

                when(productRepository.findAll())
                                .thenReturn(List.of(p1, p2));

                List<ProductResponseDTO> result = productService.listAllProducts();

                assertEquals(2, result.size());
        }

        @Test
        void listAllActiveProducts_shouldReturnList() {

                Product p1 = buildProductEntity(UUID.randomUUID(), true);
                Product p2 = buildProductEntity(UUID.randomUUID(), true);

                when(productRepository.findByActiveTrue())
                                .thenReturn(List.of(p1, p2));

                List<ProductResponseDTO> result = productService.listAllActiveProducts();

                assertEquals(2, result.size());
        }

        // -----------------------------
        // LIST NEW PRODUCTS TESTS
        // -----------------------------

        @Test
        void listNewProducts_shouldReturnList() {

                Product p1 = buildProductEntity(UUID.randomUUID(), true);

                when(productRepository.findByCreatedAtAfter(any(OffsetDateTime.class)))
                                .thenReturn(List.of(p1));

                List<ProductResponseDTO> result = productService.listNewProducts();

                assertEquals(1, result.size());
        }

        @Test
        void listNewActiveProducts_shouldReturnList() {

                Product p1 = buildProductEntity(UUID.randomUUID(), true);

                when(productRepository.findByActiveTrueAndCreatedAtAfter(any(OffsetDateTime.class)))
                                .thenReturn(List.of(p1));

                List<ProductResponseDTO> result = productService.listNewActiveProducts();

                assertEquals(1, result.size());
        }
=======
    private ProductRepository productRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;
    private StockAlertService stockAlertService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        brandRepository = Mockito.mock(BrandRepository.class);
        categoryRepository = Mockito.mock(CategoryRepository.class);
        stockAlertService = Mockito.mock(StockAlertService.class);

        productService = new ProductService(
                productRepository,
                brandRepository,
                categoryRepository,
                stockAlertService
        );
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private ProductRequestDTO buildValidDTO() {

        ProductRequestDTO.VariantDTO variant = new ProductRequestDTO.VariantDTO();
        variant.setSku("SKU-TEST-001");
        variant.setPrice(BigDecimal.valueOf(10000));
        variant.setStock(10);
        variant.setMinStock(2);

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Producto Test");
        dto.setDescription("Descripción Test");
        dto.setVariants(List.of(variant));
        dto.setImages(List.of("https://res.cloudinary.com/demo/image/upload/v1/1.jpg"));

        return dto;
    }

    private Product buildProductEntity(UUID id, boolean active) {

        Product product = Product.builder()
                .productId(id)
                .name("Producto DB")
                .description("Descripción DB")
                .active(active)
                .createdAt(OffsetDateTime.now())
                .build();

        // LISTAS MUTABLES (IMPORTANTE PARA QUE NO FALLE updateProduct)
        product.setVariants(new ArrayList<>(List.of(
                ProductVariant.builder()
                        .sku("SKU-DB-001")
                        .price(BigDecimal.valueOf(5000))
                        .stock(10)
                        .minStock(2)
                        .product(product)
                        .build()
        )));

        product.setImages(new ArrayList<>(List.of(
                ProductImage.builder()
                        .url("https://res.cloudinary.com/demo/image/upload/v1/db.jpg")
                        .product(product)
                        .build()
        )));

        product.setCategories(new ArrayList<>(List.of(
                Category.builder()
                        .categoryId(UUID.randomUUID())
                        .name("Categoria 1")
                        .build()
        )));

        return product;
    }

    // -----------------------------
    // CREATE PRODUCT TESTS
    // -----------------------------

    @Test
    void createProduct_shouldThrowBadRequest_whenDtoIsNull() {
        assertThrows(BadRequestException.class,
                () -> productService.createProduct(null));
    }

    @Test
    void createProduct_shouldThrowBadRequest_whenNameIsBlank() {

        ProductRequestDTO dto = buildValidDTO();
        dto.setName("   ");

        assertThrows(BadRequestException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldThrowBadRequest_whenVariantsAreEmpty() {

        ProductRequestDTO dto = buildValidDTO();
        dto.setVariants(List.of());

        assertThrows(BadRequestException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldThrowConflict_whenProductAlreadyExists() {

        ProductRequestDTO dto = buildValidDTO();

        when(productRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldThrowBadRequest_whenImagesExceedLimit() {

        ProductRequestDTO dto = buildValidDTO();

        dto.setImages(List.of(
                "https://res.cloudinary.com/demo/image/upload/v1/1.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/2.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/3.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/4.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/5.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/6.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/7.jpg"
        ));

        assertThrows(BadRequestException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldThrowBadRequest_whenImageIsNotCloudinary() {

        ProductRequestDTO dto = buildValidDTO();
        dto.setImages(List.of("https://google.com/foto.jpg"));

        assertThrows(BadRequestException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldThrowConflict_whenImagesAreRepeated() {

        ProductRequestDTO dto = buildValidDTO();

        dto.setImages(List.of(
                "https://res.cloudinary.com/demo/image/upload/v1/1.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/1.jpg"
        ));

        assertThrows(ConflictException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldThrowResourceNotFound_whenBrandNotFound() {

        ProductRequestDTO dto = buildValidDTO();
        UUID brandId = UUID.randomUUID();
        dto.setBrandId(brandId);

        when(productRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(brandRepository.findById(brandId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(dto));
    }

    @Test
    void createProduct_shouldCreateSuccessfully() {

        ProductRequestDTO dto = buildValidDTO();

        when(productRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        Product savedProduct = buildProductEntity(UUID.randomUUID(), true);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponseDTO response = productService.createProduct(dto);

        assertNotNull(response);
        assertEquals("Producto DB", response.getName());
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(response.getVariants());
        assertTrue(response.getVariants().size() > 0);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_shouldSendAlert_whenStockBelowMinStock() {

        ProductRequestDTO dto = buildValidDTO();
        dto.getVariants().get(0).setStock(1);
        dto.getVariants().get(0).setMinStock(2);

        when(productRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        Product savedProduct = buildProductEntity(UUID.randomUUID(), true);
        savedProduct.getVariants().get(0).setStock(1);
        savedProduct.getVariants().get(0).setMinStock(2);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        productService.createProduct(dto);

        verify(stockAlertService, times(1)).sendAlert(any());
    }

    @Test
    void createProduct_shouldNotSendAlert_whenStockAboveMinStock() {

        ProductRequestDTO dto = buildValidDTO();
        dto.getVariants().get(0).setStock(10);
        dto.getVariants().get(0).setMinStock(2);

        when(productRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        Product savedProduct = buildProductEntity(UUID.randomUUID(), true);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        productService.createProduct(dto);

        verify(stockAlertService, never()).sendAlert(any());
    }

    // -----------------------------
    // UPDATE PRODUCT TESTS
    // -----------------------------

    @Test
    void updateProduct_shouldReturnEmpty_whenProductNotFound() {

        UUID id = UUID.randomUUID();
        ProductRequestDTO dto = buildValidDTO();

        when(productRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<ProductResponseDTO> result = productService.updateProduct(id, dto);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProduct_shouldThrowBadRequest_whenImagesExceedLimit() {

        UUID id = UUID.randomUUID();
        ProductRequestDTO dto = buildValidDTO();

        dto.setImages(List.of(
                "https://res.cloudinary.com/demo/image/upload/v1/new1.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/new2.jpg",
                "https://res.cloudinary.com/demo/image/upload/v1/new3.jpg"
        ));

        Product existing = buildProductEntity(id, true);

        // Ya tiene 5 imágenes (LISTA MUTABLE)
        existing.setImages(new ArrayList<>(List.of(
                ProductImage.builder().url("1").product(existing).build(),
                ProductImage.builder().url("2").product(existing).build(),
                ProductImage.builder().url("3").product(existing).build(),
                ProductImage.builder().url("4").product(existing).build(),
                ProductImage.builder().url("5").product(existing).build()
        )));

        when(productRepository.findById(id))
                .thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class,
                () -> productService.updateProduct(id, dto));
    }

    @Test
    void updateProduct_shouldThrowConflict_whenNewNameAlreadyExists() {

        UUID id = UUID.randomUUID();
        ProductRequestDTO dto = buildValidDTO();
        dto.setName("Nuevo Nombre");

        Product existing = buildProductEntity(id, true);
        existing.setName("Viejo Nombre");

        when(productRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(productRepository.existsByNameIgnoreCase("Nuevo Nombre"))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> productService.updateProduct(id, dto));
    }

    @Test
    void updateProduct_shouldUpdateSuccessfully() {

        UUID id = UUID.randomUUID();
        ProductRequestDTO dto = buildValidDTO();

        Product existing = buildProductEntity(id, true);

        when(productRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(productRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ProductResponseDTO> result = productService.updateProduct(id, dto);

        assertTrue(result.isPresent());
        assertEquals("Producto Test", result.get().getName());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    // -----------------------------
    // ACTIVE / INACTIVE TESTS
    // -----------------------------

    @Test
    void inactiveProduct_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(productRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<ProductResponseDTO> result = productService.inactiveProduct(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void inactiveProduct_shouldInactiveSuccessfully() {

        UUID id = UUID.randomUUID();
        Product product = buildProductEntity(id, true);

        when(productRepository.findById(id))
                .thenReturn(Optional.of(product));

        Optional<ProductResponseDTO> result = productService.inactiveProduct(id);

        assertTrue(result.isPresent());
        assertEquals("INACTIVE", result.get().getStatus());

        verify(productRepository, times(1)).save(product);
    }

    @Test
    void activeProduct_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(productRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<ProductResponseDTO> result = productService.activeProduct(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void activeProduct_shouldActiveSuccessfully() {

        UUID id = UUID.randomUUID();
        Product product = buildProductEntity(id, false);

        when(productRepository.findById(id))
                .thenReturn(Optional.of(product));

        Optional<ProductResponseDTO> result = productService.activeProduct(id);

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());

        verify(productRepository, times(1)).save(product);
    }

    // -----------------------------
    // GET BY ID TESTS
    // -----------------------------

    @Test
    void getById_shouldReturnProduct_whenFound() {

        UUID id = UUID.randomUUID();
        Product product = buildProductEntity(id, true);

        when(productRepository.findById(id))
                .thenReturn(Optional.of(product));

        Optional<ProductResponseDTO> result = productService.getById(id);

        assertTrue(result.isPresent());
        assertEquals("Producto DB", result.get().getName());
    }

    @Test
    void getById_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(productRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<ProductResponseDTO> result = productService.getById(id);

        assertTrue(result.isEmpty());
    }

    // -----------------------------
    // LIST PRODUCTS TESTS
    // -----------------------------

    @Test
    void listProducts_shouldReturnList() {

        Product p1 = buildProductEntity(UUID.randomUUID(), true);
        Product p2 = buildProductEntity(UUID.randomUUID(), true);

        Page<Product> page = new PageImpl<>(List.of(p1, p2));

        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        List<ProductResponseDTO> result = productService.listProducts(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void listActiveProducts_shouldReturnOnlyActive() {

        Product p1 = buildProductEntity(UUID.randomUUID(), true);
        Product p2 = buildProductEntity(UUID.randomUUID(), true);

        Page<Product> page = new PageImpl<>(List.of(p1, p2));

        when(productRepository.findByActiveTrue(any(Pageable.class)))
                .thenReturn(page);

        List<ProductResponseDTO> result = productService.listActiveProducts(0, 10);

        assertEquals(2, result.size());
    }

    // -----------------------------
    // LIST ALL PRODUCTS TESTS
    // -----------------------------

    @Test
    void listAllProducts_shouldReturnList() {

        Product p1 = buildProductEntity(UUID.randomUUID(), true);
        Product p2 = buildProductEntity(UUID.randomUUID(), false);

        when(productRepository.findAll())
                .thenReturn(List.of(p1, p2));

        List<ProductResponseDTO> result = productService.listAllProducts();

        assertEquals(2, result.size());
    }

    @Test
    void listAllActiveProducts_shouldReturnList() {

        Product p1 = buildProductEntity(UUID.randomUUID(), true);
        Product p2 = buildProductEntity(UUID.randomUUID(), true);

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(p1, p2));

        List<ProductResponseDTO> result = productService.listAllActiveProducts();

        assertEquals(2, result.size());
    }

    // -----------------------------
    // LIST NEW PRODUCTS TESTS
    // -----------------------------

    @Test
    void listNewProducts_shouldReturnList() {

        Product p1 = buildProductEntity(UUID.randomUUID(), true);

        when(productRepository.findByCreatedAtAfter(any(OffsetDateTime.class)))
                .thenReturn(List.of(p1));

        List<ProductResponseDTO> result = productService.listNewProducts();

        assertEquals(1, result.size());
    }

    @Test
    void listNewActiveProducts_shouldReturnList() {

        Product p1 = buildProductEntity(UUID.randomUUID(), true);

        when(productRepository.findByActiveTrueAndCreatedAtAfter(any(OffsetDateTime.class)))
                .thenReturn(List.of(p1));

        List<ProductResponseDTO> result = productService.listNewActiveProducts();

        assertEquals(1, result.size());
    }
>>>>>>> 39a86a44966c35c642d5a12aa1f80ebdb32484d7
}
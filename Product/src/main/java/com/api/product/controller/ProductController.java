package com.api.product.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.api.product.dto.ApiResponseDTO;
import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de productos.
 *
 * Todos los endpoints devuelven ApiResponseDTO con:
 * - mensaje
 * - código HTTP
 * - datos
 * - timestamp
 *
 * NOTA:
 * - GET nunca modifica el estado de los productos.
 * - PUT con {id} se usa para actualizar productos.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    /**
     * Crear un nuevo producto (con imágenes).
     *
     * Este endpoint recibe un multipart/form-data con:
     * - product: JSON con los datos del producto
     * - images: lista de imágenes (opcional)
     *
     * @param productJson JSON del producto en String
     * @param images lista de imágenes opcionales
     * @return producto creado
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            ProductRequestDTO dto = objectMapper.readValue(productJson, ProductRequestDTO.class);

            if (images != null && !images.isEmpty()) {
                dto.setImages(images);
            }

            ProductResponseDTO product = productService.createProduct(dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto creado correctamente con imágenes")
                            .status(HttpStatus.CREATED.value())
                            .data(product)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Error al crear producto: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }
    }

    /**
     * Actualizar un producto existente (con opción de agregar nuevas imágenes).
     *
     * Este endpoint recibe un multipart/form-data con:
     * - product: JSON con los datos del producto
     * - images: lista de nuevas imágenes opcionales
     *
     * @param id UUID del producto
     * @param productJson JSON del producto en String
     * @param images lista de imágenes opcionales
     * @return producto actualizado
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable UUID id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            ProductRequestDTO dto = objectMapper.readValue(productJson, ProductRequestDTO.class);

            if (images != null && !images.isEmpty()) {
                dto.setImages(images);
            }

            Optional<ProductResponseDTO> updated = productService.updateProduct(id, dto);

            if (updated.isPresent()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                        ApiResponseDTO.<ProductResponseDTO>builder()
                                .message("Producto actualizado correctamente")
                                .status(HttpStatus.ACCEPTED.value())
                                .data(updated.get())
                                .timestamp(OffsetDateTime.now())
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponseDTO.<ProductResponseDTO>builder()
                                .message("Producto con ID " + id + " no encontrado para actualizar")
                                .status(HttpStatus.NOT_FOUND.value())
                                .data(null)
                                .timestamp(OffsetDateTime.now())
                                .build()
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Error al actualizar producto: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }
    }

    /**
     * Lista todos los productos con paginación.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listProducts(
            @RequestParam int page,
            @RequestParam int size) {

        List<ProductResponseDTO> products = productService.listProducts(page, size);

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos en esta página")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Lista productos activos con paginación.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listActiveProducts(
            @RequestParam int page,
            @RequestParam int size) {

        List<ProductResponseDTO> products = productService.listActiveProducts(page, size);

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos activos en esta página")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos activos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Obtener un producto por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getById(@PathVariable UUID id) {

        Optional<ProductResponseDTO> product = productService.getById(id);

        if (product.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto encontrado correctamente")
                            .status(HttpStatus.OK.value())
                            .data(product.get())
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto con ID " + id + " no encontrado")
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }
    }

    /**
     * Inactivar un producto por ID.
     */
    @PutMapping("/inactive/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> inactiveProduct(@PathVariable UUID id) {

        Optional<ProductResponseDTO> product = productService.inactiveProduct(id);

        if (product.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto inactivado correctamente")
                            .status(HttpStatus.OK.value())
                            .data(product.get())
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto con ID " + id + " no encontrado para inactivar")
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }
    }

    /**
     * Activar un producto por ID.
     */
    @PutMapping("/active/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> activateProduct(@PathVariable UUID id) {

        Optional<ProductResponseDTO> product = productService.activeProduct(id);

        if (product.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto activado correctamente")
                            .status(HttpStatus.OK.value())
                            .data(product.get())
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto con ID " + id + " no encontrado para activar")
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }
    }

    /**
     * Mostrar todos los productos (activos e inactivos).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listAllProducts() {

        List<ProductResponseDTO> products = productService.listAllProducts();

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos registrados")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista completa de productos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Mostrar todos los productos activos sin paginación.
     */
    @GetMapping("/all/active")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listAllActiveProducts() {

        List<ProductResponseDTO> products = productService.listAllActiveProducts();

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos activos registrados")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista completa de productos activos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Lista productos creados en los últimos 7 días.
     */
    @GetMapping("/new")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listNewProducts() {

        List<ProductResponseDTO> products = productService.listNewProducts();

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos nuevos en la última semana")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos nuevos obtenida correctamente (últimos 7 días)")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Lista productos activos creados en los últimos 7 días.
     */
    @GetMapping("/new/active")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listNewActiveProducts() {

        List<ProductResponseDTO> products = productService.listNewActiveProducts();

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos activos nuevos en la última semana")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos activos nuevos obtenida correctamente (últimos 7 días)")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }
}
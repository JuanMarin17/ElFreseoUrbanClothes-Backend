package com.api.product.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.product.dto.ApiResponseDTO;
import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestBody ProductRequestDTO dto) {

        try {
            ProductResponseDTO product = productService.createProduct(dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto creado correctamente")
                            .status(HttpStatus.CREATED.value())
                            .data(product)
                            .timestamp(OffsetDateTime.now())
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Error al crear producto: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable UUID id,
            @RequestBody ProductRequestDTO dto) {

        try {
            Optional<ProductResponseDTO> updated = productService.updateProduct(id, dto);

            if (updated.isPresent()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                        ApiResponseDTO.<ProductResponseDTO>builder()
                                .message("Producto actualizado correctamente")
                                .status(HttpStatus.ACCEPTED.value())
                                .data(updated.get())
                                .timestamp(OffsetDateTime.now())
                                .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponseDTO.<ProductResponseDTO>builder()
                                .message("Producto con ID " + id + " no encontrado para actualizar")
                                .status(HttpStatus.NOT_FOUND.value())
                                .data(null)
                                .timestamp(OffsetDateTime.now())
                                .build());
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Error al actualizar producto: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ProductResponseDTO> products = productService.listProducts(page, size);

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos en esta página")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> listActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ProductResponseDTO> products = productService.listActiveProducts(page, size);

        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ApiResponseDTO.<List<ProductResponseDTO>>builder()
                            .message("No hay productos activos en esta página")
                            .status(HttpStatus.NO_CONTENT.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos activos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }

    /** Trae varios productos por ID en una sola petición (consumido por Cart, Supplier, etc. para evitar N+1). */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> getByIds(@RequestBody List<UUID> ids) {
        List<ProductResponseDTO> products = productService.getByIds(ids);
        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Productos encontrados")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }

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
                            .build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto con ID " + id + " no encontrado")
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }
    }

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
                            .build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto con ID " + id + " no encontrado para inactivar")
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }
    }

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
                            .build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseDTO.<ProductResponseDTO>builder()
                            .message("Producto con ID " + id + " no encontrado para activar")
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .timestamp(OffsetDateTime.now())
                            .build());
        }
    }

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
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista completa de productos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }

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
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista completa de productos activos obtenida correctamente")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }

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
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos nuevos obtenida correctamente (últimos 7 días)")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }

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
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponseDTO.<List<ProductResponseDTO>>builder()
                        .message("Lista de productos activos nuevos obtenida correctamente (últimos 7 días)")
                        .status(HttpStatus.OK.value())
                        .data(products)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }
}

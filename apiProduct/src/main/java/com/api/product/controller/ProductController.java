package com.api.product.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.service.ProductService;

import lombok.RequiredArgsConstructor;
/**
 * Controlador REST para la gestión de productos.
 * Proporciona endpoints para crear, listar, obtener, actualizar e inactivar productos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/products/")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    /**
     * Crear un nuevo producto.
     *
     * @param productRequestDTO DTO con los datos del producto (nombre, descripción, precios, stock, categoría y opcionalmente imagen)
     * @return ResponseEntity con ProductResponseDTO del producto creado y código HTTP:
     *         - 201 CREATED si se crea correctamente
     *         - 409 CONFLICT si el producto ya existe
     *         - 500 INTERNAL_SERVER_ERROR si ocurre algún otro error
     */
        public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        try {
            ProductResponseDTO response = productService.createProduct(productRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Revisamos el mensaje para ver si es duplicado
            if (e.getMessage() != null && e.getMessage().contains("ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping
        /**
     * Listar todos los productos (activos e inactivos).
     *
     * @return ResponseEntity con lista de ProductResponseDTO y código HTTP 302 FOUND
     */
    public ResponseEntity<List<ProductResponseDTO>> listProducts() {
        List<ProductResponseDTO> products = productService.ListProducts();
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("{id}")
    /**
     * Obtener un producto por su ID.
     *
     * @param id ID del producto a buscar
     * @return ResponseEntity con ProductResponseDTO y código HTTP:
     *         - 302 FOUND si el producto existe
     *         - 404 NOT_FOUND si no se encuentra
     */
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO response = productService.listId(id).orElse(null);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }   

    @PutMapping("{id}")
    /**
     * Actualizar un producto existente.
     *
     * @param id ID del producto a actualizar
     * @param productRequestDTO DTO con los datos actualizados del producto
     * @return ResponseEntity con ProductResponseDTO del producto actualizado y código HTTP:
     *         - 202 ACCEPTED si la actualización fue exitosa
     *         - 304 NOT_MODIFIED si no se encuentra el producto o no se aplicó actualización
     */
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO response = productService.updateProduct(id, productRequestDTO).orElse(null);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(null);
        }
    }

    @DeleteMapping("/{id}")
    /**
     * Inactivar un producto (marcarlo como inactivo) según su ID.
     *
     * @param id ID del producto a inactivar
     * @return ResponseEntity con ProductResponseDTO del producto inactivado y código HTTP:
     *         - 200 OK si la inactivación fue exitosa
     *         - 304 NOT_MODIFIED si no se encuentra el producto
     */ 
    public ResponseEntity<ProductResponseDTO> desactiveProduct(@PathVariable Long id) {
        Optional<ProductResponseDTO> response = productService.inactiveProduct(id);

        if (response.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(response.get()); 
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(response.get()); 
        }
    }

}

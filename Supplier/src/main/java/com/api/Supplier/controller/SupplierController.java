package com.api.Supplier.controller;

import java.util.List;
import java.util.UUID;

import com.api.Supplier.dto.ProductSummaryDTO;

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

import com.api.Supplier.dto.MessageResponseDTO;
import com.api.Supplier.dto.SupplierRequestDTO;
import com.api.Supplier.dto.SupplierResponseDTO;
import com.api.Supplier.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping("/createSupplier")
    public ResponseEntity<SupplierResponseDTO> createSupplier(
            @Valid @RequestBody SupplierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(dto));
    }

    @GetMapping("/getSuppliersByStore")
    public ResponseEntity<List<SupplierResponseDTO>> getSuppliersByStore() {
        return ResponseEntity.ok(supplierService.getSuppliersByStore());
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.getSupplierById(supplierId));
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody SupplierRequestDTO dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierId, dto));
    }

    // Desvincular proveedor de la tienda
    @DeleteMapping("/{supplierId}/unlink")
    public ResponseEntity<MessageResponseDTO> unlinkSupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.unlinkSupplierFromStore(supplierId));
    }

    // Desactivar proveedor (eliminado lógico)
    @DeleteMapping("/{supplierId}")
    public ResponseEntity<MessageResponseDTO> deactivateSupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.deactivateSupplier(supplierId));
    }

    // ── Productos vinculados al proveedor ────────────────────────────────────

    @PostMapping("/{supplierId}/products/{productId}")
    public ResponseEntity<MessageResponseDTO> linkProduct(
            @PathVariable UUID supplierId,
            @PathVariable UUID productId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.linkProductToSupplier(supplierId, productId));
    }

    @DeleteMapping("/{supplierId}/products/{productId}")
    public ResponseEntity<MessageResponseDTO> unlinkProduct(
            @PathVariable UUID supplierId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(supplierService.unlinkProductFromSupplier(supplierId, productId));
    }

    @GetMapping("/{supplierId}/products")
    public ResponseEntity<List<ProductSummaryDTO>> getProductsBySupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.getProductsBySupplier(supplierId));
    }
}
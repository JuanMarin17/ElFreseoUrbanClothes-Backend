package com.api.Supplier.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Supplier.dto.AssignSupplierRequest;
import com.api.Supplier.dto.CreateSupplierRequest;
import com.api.Supplier.dto.MessageResponseDTO;
import com.api.Supplier.dto.StoreSupplierResponse;
import com.api.Supplier.dto.SupplierResponse;
import com.api.Supplier.dto.UpdateSupplierRequest;
import com.api.Supplier.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // ── Suppliers ─────────────────────────────────────────────────────────────

    @GetMapping("/suppliers")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(pageable));
    }

    @GetMapping("/suppliers/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.getSupplierById(supplierId));
    }

    @PostMapping("/suppliers")
    public ResponseEntity<MessageResponseDTO> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(request));
    }

    @PatchMapping("/suppliers/{supplierId}")
    public ResponseEntity<MessageResponseDTO> updateSupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierId, request));
    }

    @DeleteMapping("/suppliers/{supplierId}")
    public ResponseEntity<MessageResponseDTO> deleteSupplier(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.deleteSupplier(supplierId));
    }

    // ── Store ↔ Supplier ─────────────────────────────────────────────────────

    @GetMapping("/stores/{storeId}/suppliers")
    public ResponseEntity<List<StoreSupplierResponse>> getSuppliersByStore(
            @PathVariable UUID storeId) {
        return ResponseEntity.ok(supplierService.getSuppliersByStore(storeId));
    }

    @PostMapping("/stores/{storeId}/suppliers")
    public ResponseEntity<MessageResponseDTO> assignSupplierToStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody AssignSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.assignSupplierToStore(storeId, request));
    }

    @DeleteMapping("/stores/{storeId}/suppliers/{supplierId}")
    public ResponseEntity<MessageResponseDTO> removeSupplierFromStore(
            @PathVariable UUID storeId,
            @PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.removeSupplierFromStore(storeId, supplierId));
    }
}
package com.api.Store.controller;

import com.api.Store.dto.StoreCreateRequestDTO;
import com.api.Store.dto.StoreResponseDTO;
import com.api.Store.dto.StoreToggleStatusRequestDTO;
import com.api.Store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

/**
 * Gestión de tiendas.
 *
 * POST /api/stores → Crear tienda
 * GET /api/stores/{storeId} → Obtener tienda por ID
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** Crear una nueva tienda */
    @PostMapping
    public ResponseEntity<StoreResponseDTO> createStore(@Valid @RequestBody StoreCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(dto));
    }

    /** Obtener una tienda por su ID */
    @GetMapping("/getByStoreId/{storeId}")
    public ResponseEntity<StoreResponseDTO> getStore(@PathVariable("storeId") UUID storeId) {
        return ResponseEntity.ok(storeService.getStoreById(storeId));
    }

    @GetMapping("/getBySlug/{slug}")
    public ResponseEntity<StoreResponseDTO> getStoreBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(storeService.getBySlug(slug));
    }


    @GetMapping("/existStore/{storeId}")
    public ResponseEntity<Boolean> existStore(@PathVariable UUID storeId) {
        return ResponseEntity.status(HttpStatus.OK).body(storeService.existStore(storeId));
    }

    /** Obtener todas las tiendas */
    @GetMapping
    public ResponseEntity<List<StoreResponseDTO>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    /** Inhabilitar o habilitar una tienda (solo SUPERADMIN) */
    @PatchMapping("/{storeId}/toggle-status")
    public ResponseEntity<StoreResponseDTO> toggleStatus(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreToggleStatusRequestDTO dto) {
        return ResponseEntity.ok(storeService.toggleStatus(storeId, dto));
    }
}


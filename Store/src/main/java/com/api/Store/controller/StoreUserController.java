package com.api.Store.controller;

import com.api.Store.dto.StoreUserRequestDTO;
import com.api.Store.dto.StoreUserResponseDTO;
import com.api.Store.service.StoreUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gestión de usuarios en tiendas.
 *
 * POST   /api/stores/{storeId}/users           → Agregar usuario a la tienda
 * GET    /api/stores/{storeId}/users           → Listar usuarios de una tienda
 * GET    /api/stores/users/{userId}            → Listar tiendas de un usuario
 * GET    /api/stores/{storeId}/access/{userId} → Validar acceso de usuario
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreUserController {

    private final StoreUserService storeUserService;

    /** Agregar un usuario a una tienda con un rol específico (ADMIN o STAFF) */
    @PostMapping("/{storeId}/users")
    public ResponseEntity<StoreUserResponseDTO> addUserToStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreUserRequestDTO dto) {

        // El storeId siempre viene del path para evitar inconsistencias
        dto.setStoreId(storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(storeUserService.addUserToStore(dto));
    }

    /** Listar todos los usuarios de una tienda */
    @GetMapping("/{storeId}/users")
    public ResponseEntity<List<StoreUserResponseDTO>> getUsersByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeUserService.getUsersByStore(storeId));
    }

    /** Listar todas las tiendas a las que pertenece un usuario */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<StoreUserResponseDTO>> getStoresByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(storeUserService.getStoresByUser(userId));
    }

    /** Verificar si un usuario tiene acceso a una tienda */
    @GetMapping("/{storeId}/access/{userId}")
    public ResponseEntity<Map<String, Boolean>> validateAccess(
            @PathVariable UUID storeId,
            @PathVariable UUID userId) {

        boolean hasAccess = storeUserService.validateAccess(userId, storeId);
        return ResponseEntity.ok(Map.of("hasAccess", hasAccess));
    }
}

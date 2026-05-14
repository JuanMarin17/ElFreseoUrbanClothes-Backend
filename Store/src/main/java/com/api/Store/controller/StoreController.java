package com.api.Store.controller;

import com.api.Store.dto.StoreCreateRequestDTO;
import com.api.Store.dto.StoreResponseDTO;
import com.api.Store.dto.StoreUserRequestDTO;
import com.api.Store.dto.StoreUserResponseDTO;
import com.api.Store.dto.settings.StoreSettingsRequestDTO;
import com.api.Store.dto.settings.StoreSettingsResponseDTO;
import com.api.Store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // POST /api/stores
    @PostMapping
    public ResponseEntity<StoreResponseDTO> createStore(@Valid @RequestBody StoreCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(dto));
    }

    // GET /api/stores/{storeId}
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponseDTO> getStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeService.getStoreById(storeId));
    }

    // POST /api/stores/users
    @PostMapping("/users")
    public ResponseEntity<StoreUserResponseDTO> addUser(@Valid @RequestBody StoreUserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.addUserToStore(dto));
    }

    // GET /api/stores/{storeId}/users
    @GetMapping("/{storeId}/users")
    public ResponseEntity<List<StoreUserResponseDTO>> getUsersByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeService.getUsersByStore(storeId));
    }

    // GET /api/stores/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<StoreUserResponseDTO>> getStoresByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(storeService.getStoresByUser(userId));
    }

    // GET /api/stores/{storeId}/settings
    @GetMapping("/{storeId}/settings")
    public ResponseEntity<StoreSettingsResponseDTO> getSettings(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeService.getSettings(storeId));
    }

    // PUT /api/stores/{storeId}/settings
    @PutMapping("/{storeId}/settings")
    public ResponseEntity<StoreSettingsResponseDTO> saveSettings(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreSettingsRequestDTO dto) {
        return ResponseEntity.ok(storeService.saveSettings(storeId, dto));
    }
}

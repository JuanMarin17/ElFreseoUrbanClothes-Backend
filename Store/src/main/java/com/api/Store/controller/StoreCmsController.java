package com.api.Store.controller;

import com.api.Store.dto.StoreCmsDTO;
import com.api.Store.service.StoreCmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Gestión de páginas CMS de la tienda (about, contact, locations, returns, faq).
 *
 * GET  /api/v1/stores/cms  → Obtener toda la info CMS de la tienda
 * PATCH /api/v1/stores/cms → Actualizar info CMS de la tienda
 */
@RestController
@RequestMapping("/stores/cms")
@RequiredArgsConstructor
public class StoreCmsController {

    private final StoreCmsService cmsService;

    /** Obtener toda la información CMS de la tienda autenticada */
    @GetMapping
    public ResponseEntity<StoreCmsDTO> getCms(
            @RequestHeader("X-Store-Id") UUID storeId
    ) {
        return ResponseEntity.ok(cmsService.getCms(storeId));
    }

    /** Actualizar (parcialmente) la información CMS de la tienda autenticada */
    @PatchMapping
    public ResponseEntity<StoreCmsDTO> updateCms(
            @RequestHeader("X-Store-Id") UUID storeId,
            @Valid @RequestBody StoreCmsDTO dto
    ) {
        cmsService.saveCms(storeId, dto);
        return ResponseEntity.ok(cmsService.getCms(storeId));
    }
}

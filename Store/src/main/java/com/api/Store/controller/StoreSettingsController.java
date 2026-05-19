package com.api.Store.controller;

import com.api.Store.dto.settings.StoreSettingsRequestDTO;
import com.api.Store.dto.settings.StoreSettingsResponseDTO;
import com.api.Store.service.StoreSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Gestión de configuración (settings) de una tienda.
 *
 * GET  /api/stores/{storeId}/settings → Obtener configuración actual
 * POST /api/stores/{storeId}/settings → Guardar / actualizar configuración
 *
 * ─────────────────────────────────────────────────────────
 * POSTMAN — Ejemplo de request body para POST:
 * ─────────────────────────────────────────────────────────
 * {
 *   "completedStep": 7,
 *   "plan": {
 *     "id": "basico",
 *     "name": "BÁSICO",
 *     "price": "$19",
 *     "features": ["1 Tienda", "Productos ilimitados", "Plantillas básicas", "Soporte por email"]
 *   },
 *   "basic": {
 *     "name": "urbana",
 *     "description": "Descripción de la tienda",
 *     "logoPreview": "https://ejemplo.com/logo.png"
 *   },
 *   "components": {
 *     "banner": {
 *       "title": "NEON VAPOR",
 *       "font": "Bebas Neue",
 *       "size": "77",
 *       "color": "#ff0f0f",
 *       "bg": "#c10b0b",
 *       "images": []
 *     },
 *     "header": {
 *       "logo": "MiTienda",
 *       "items": ["HOME", "SHOP"],
 *       "font": "Inter",
 *       "size": 16,
 *       "color": "#ffffff",
 *       "bg": "#000000"
 *     },
 *     "footer": {
 *       "text": "© Mi Tienda 2026",
 *       "font": "Montserrat",
 *       "size": 14,
 *       "color": "#888888",
 *       "bg": "#080808"
 *     }
 *   },
 *   "layout": {
 *     "id": "clasico",
 *     "title": "CLÁSICO ECOMMERCE",
 *     "description": "Diseño tradicional, enfocado en conversión y catálogo."
 *   },
 *   "legal": {
 *     "legalName": "Mi Empresa S.A.S",
 *     "idNumber": "900123456",
 *     "documentName": "rut.pdf"
 *   },
 *   "payment": {
 *     "paymentMethod": "mercadopago",
 *     "shipping": "ambos"
 *   },
 *   "preview": {},
 *   "styles": {
 *     "cardBg": "#c00c0c",
 *     "cardBorderColor1": "#ba4f6a",
 *     "cardBorderColor2": "#2bff00",
 *     "cardBorderWidth": "10",
 *     "cardRadius": "39",
 *     "colorBoton": "#ff0000",
 *     "colorParrafo": "#db6161",
 *     "colorTitulo": "#b97979"
 *   },
 *   "store": {
 *     "name": "urbana",
 *     "subdomain": "mi-tienda",
 *     "accepted": true
 *   }
 * }
 */
@RestController
@RequestMapping("/api/stores/{storeId}/settings")
@RequiredArgsConstructor
public class StoreSettingsController {

    private final StoreSettingsService storeSettingsService;

    /** Obtener la configuración actual de la tienda */
    @GetMapping
    public ResponseEntity<StoreSettingsResponseDTO> getSettings(@PathVariable UUID storeId) {
        return ResponseEntity.ok(storeSettingsService.getSettings(storeId));
    }

    /**
     * Guardar o actualizar la configuración de la tienda.
     * Semántica PATCH: solo se actualizan los campos que vengan en el body.
     */
    @PostMapping
    public ResponseEntity<StoreSettingsResponseDTO> saveSettings(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreSettingsRequestDTO dto) {

        return ResponseEntity.ok(storeSettingsService.saveSettings(storeId, dto));
    }
}

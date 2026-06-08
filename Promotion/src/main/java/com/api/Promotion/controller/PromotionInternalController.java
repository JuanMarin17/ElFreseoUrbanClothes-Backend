package com.api.Promotion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.Promotion.dto.CouponValidationDTO;
import com.api.Promotion.dto.ProductPromotionDTO;
import com.api.Promotion.service.CouponService;
import com.api.Promotion.service.PromotionService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints internos consumidos por otros microservicios (no expuestos al cliente final).
 * El Gateway debe bloquear el prefijo /promotions/internal desde el exterior.
 */
@RestController
@RequestMapping("/promotions/internal")
@RequiredArgsConstructor
public class PromotionInternalController {

    private final PromotionService promotionService;
    private final CouponService couponService;

    /**
     * Llamado por Cart para obtener promociones activas de los productos en el carrito.
     */
    @GetMapping("/by-products")
    public ResponseEntity<List<ProductPromotionDTO>> getPromotionsForProducts(
            @RequestParam List<UUID> productIds,
            @RequestHeader("X-Store-Id") String storeId) {
        return ResponseEntity.ok(
                promotionService.getPromotionsForProducts(UUID.fromString(storeId), productIds));
    }

    /**
     * Llamado por OrderPayment para validar un cupón antes de crear la orden.
     * No registra redención.
     */
    @GetMapping("/coupons/validate/{code}")
    public ResponseEntity<CouponValidationDTO> validateCoupon(
            @PathVariable String code,
            @RequestHeader("X-Store-Id") String storeId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                couponService.validateCoupon(code, UUID.fromString(storeId), UUID.fromString(userId)));
    }
}

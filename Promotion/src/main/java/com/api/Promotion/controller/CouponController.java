package com.api.Promotion.controller;

import java.util.List;
import java.util.UUID;

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

import com.api.Promotion.dto.ApiResponseDTO;
import com.api.Promotion.dto.CouponRequestDTO;
import com.api.Promotion.dto.CouponResponseDTO;
import com.api.Promotion.dto.RedemptionResponseDTO;
import com.api.Promotion.service.CouponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/createCoupon")
    public ResponseEntity<CouponResponseDTO> createCoupon(@RequestBody CouponRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(dto));
    }

    @GetMapping("/getActiveCoupons")
    public ResponseEntity<List<CouponResponseDTO>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getActiveCoupons());
    }

    @GetMapping("/all")
    public ResponseEntity<List<CouponResponseDTO>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable UUID couponId,
            @RequestBody CouponRequestDTO dto) {
        return ResponseEntity.ok(couponService.updateCoupon(couponId, dto));
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<ApiResponseDTO> deactivateCoupon(@PathVariable UUID couponId) {
        return ResponseEntity.ok(couponService.deactivateCoupon(couponId));
    }

    @PostMapping("/redeem/{code}")
    public ResponseEntity<RedemptionResponseDTO> redeemCoupon(@PathVariable String code) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.redeemCoupon(code));
    }

    @GetMapping("/{couponId}/redemptions")
    public ResponseEntity<List<RedemptionResponseDTO>> getRedemptions(@PathVariable UUID couponId) {
        return ResponseEntity.ok(couponService.getRedemptions(couponId));
    }
}
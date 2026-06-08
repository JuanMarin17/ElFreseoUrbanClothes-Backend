package com.api.Promotion.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Promotion.dto.ApiResponseDTO;
import com.api.Promotion.dto.CouponRequestDTO;
import com.api.Promotion.dto.CouponResponseDTO;
import com.api.Promotion.dto.CouponValidationDTO;
import com.api.Promotion.dto.RedemptionResponseDTO;
import com.api.Promotion.entity.Coupon;
import com.api.Promotion.entity.CouponRedemption;
import com.api.Promotion.exception.BadRequestException;
import com.api.Promotion.exception.PromotionNotFoundException;
import com.api.Promotion.exception.UnauthorizedException;
import com.api.Promotion.repository.CouponRedemptionRepository;
import com.api.Promotion.repository.CouponRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;

    // ── Crear cupón ───────────────────────────────────────────────────────────
    public CouponResponseDTO createCoupon(CouponRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        if (dto.getCode() == null || dto.getCode().isBlank())
            throw new BadRequestException("El código del cupón es obligatorio");

        if (couponRepository.existsByCode(dto.getCode()))
            throw new BadRequestException("Ya existe un cupón con ese código");

        if (dto.getDiscount() == null || dto.getDiscount().doubleValue() <= 0)
            throw new BadRequestException("El descuento debe ser mayor a 0");

        if (dto.getDiscountType() == null)
            throw new BadRequestException("El tipo de descuento es obligatorio");

        Coupon coupon = new Coupon();
        coupon.setCode(dto.getCode().toUpperCase());
        coupon.setDiscount(dto.getDiscount());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setStoreId(storeId);

        return toCouponResponse(couponRepository.save(coupon));
    }

    // ── Obtener cupones activos de la tienda ──────────────────────────────────
    public List<CouponResponseDTO> getActiveCoupons() {
        UUID storeId = getStoreIdFromHeader();
        return couponRepository.findByStoreIdAndIsActiveTrue(storeId)
                .stream().map(this::toCouponResponse).toList();
    }

    // ── Obtener todos los cupones de la tienda ────────────────────────────────
    public List<CouponResponseDTO> getAllCoupons() {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();
        return couponRepository.findByStoreId(storeId)
                .stream().map(this::toCouponResponse).toList();
    }

    // ── Actualizar cupón ──────────────────────────────────────────────────────
    public CouponResponseDTO updateCoupon(UUID couponId, CouponRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new PromotionNotFoundException("Cupón no encontrado con id: " + couponId));

        if (!coupon.getStoreId().equals(storeId))
            throw new UnauthorizedException("Este cupón no pertenece a tu tienda");

        if (dto.getCode() != null && !dto.getCode().equals(coupon.getCode())) {
            if (couponRepository.existsByCode(dto.getCode()))
                throw new BadRequestException("Ya existe un cupón con ese código");
            coupon.setCode(dto.getCode().toUpperCase());
        }

        if (dto.getDiscount() != null)
            coupon.setDiscount(dto.getDiscount());
        if (dto.getDiscountType() != null)
            coupon.setDiscountType(dto.getDiscountType());

        return toCouponResponse(couponRepository.save(coupon));
    }

    // ── Desactivar cupón (eliminado lógico) ───────────────────────────────────
    public ApiResponseDTO deactivateCoupon(UUID couponId) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new PromotionNotFoundException("Cupón no encontrado con id: " + couponId));

        if (!coupon.getStoreId().equals(storeId))
            throw new UnauthorizedException("Este cupón no pertenece a tu tienda");

        coupon.setIsActive(false);
        couponRepository.save(coupon);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Cupón desactivado correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Redimir cupón ─────────────────────────────────────────────────────────
    @Transactional
    public RedemptionResponseDTO redeemCoupon(String code) {
        UUID userId = getUserIdFromHeader();
        UUID storeId = getStoreIdFromHeader();

        Coupon coupon = couponRepository.findByCodeAndStoreId(code.toUpperCase(), storeId)
                .orElseThrow(() -> new PromotionNotFoundException("Cupón no encontrado: " + code));

        if (!coupon.getIsActive())
            throw new BadRequestException("El cupón no está activo");

        if (redemptionRepository.existsByCouponIdAndUserId(coupon.getCouponId(), userId))
            throw new BadRequestException("Ya usaste este cupón anteriormente");

        CouponRedemption redemption = new CouponRedemption();
        redemption.setCouponId(coupon.getCouponId());
        redemption.setUserId(userId);

        return toRedemptionResponse(redemptionRepository.save(redemption));
    }

    // ── Interno: validar cupón sin redimir (llamado por OrderPayment) ─────────
    public CouponValidationDTO validateCoupon(String code, UUID storeId, UUID userId) {
        Coupon coupon = couponRepository.findByCodeAndStoreId(code.toUpperCase(), storeId)
                .orElseThrow(() -> new PromotionNotFoundException("Cupón no encontrado: " + code));

        if (!coupon.getIsActive())
            throw new BadRequestException("El cupón no está activo");

        if (redemptionRepository.existsByCouponIdAndUserId(coupon.getCouponId(), userId))
            throw new BadRequestException("Ya usaste este cupón anteriormente");

        CouponValidationDTO dto = new CouponValidationDTO();
        dto.setCouponId(coupon.getCouponId());
        dto.setCode(coupon.getCode());
        dto.setDiscount(coupon.getDiscount());
        dto.setDiscountType(coupon.getDiscountType().name());
        return dto;
    }

    // ── Ver redenciones de un cupón ───────────────────────────────────────────
    public List<RedemptionResponseDTO> getRedemptions(UUID couponId) {
        validateAdminOrOwner();

        if (!couponRepository.existsById(couponId))
            throw new PromotionNotFoundException("Cupón no encontrado con id: " + couponId);

        return redemptionRepository.findByCouponId(couponId)
                .stream().map(this::toRedemptionResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private UUID getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(storeIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    private void validateAdminOrOwner() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"OWNER".equals(role))
            throw new UnauthorizedException("Solo el ADMIN u OWNER pueden realizar esta acción");
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private CouponResponseDTO toCouponResponse(Coupon c) {
        CouponResponseDTO dto = new CouponResponseDTO();
        dto.setCouponId(c.getCouponId());
        dto.setCode(c.getCode());
        dto.setDiscount(c.getDiscount());
        dto.setDiscountType(c.getDiscountType());
        dto.setStoreId(c.getStoreId());
        dto.setIsActive(c.getIsActive());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private RedemptionResponseDTO toRedemptionResponse(CouponRedemption r) {
        RedemptionResponseDTO dto = new RedemptionResponseDTO();
        dto.setRedemptionId(r.getRedemptionId());
        dto.setCouponId(r.getCouponId());
        dto.setUserId(r.getUserId());
        dto.setUsedAt(r.getUsedAt());
        return dto;
    }
}

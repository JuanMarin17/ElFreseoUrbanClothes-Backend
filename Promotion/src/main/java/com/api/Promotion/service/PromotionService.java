package com.api.Promotion.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.api.Promotion.dto.ApiResponseDTO;
import com.api.Promotion.dto.PromotionRequestDTO;
import com.api.Promotion.dto.PromotionResponseDTO;
import com.api.Promotion.entity.Promotion;
import com.api.Promotion.exception.BadRequestException;
import com.api.Promotion.exception.PromotionNotFoundException;
import com.api.Promotion.exception.UnauthorizedException;
import com.api.Promotion.repository.PromotionRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    // ── Crear promoción ───────────────────────────────────────────────────────
    public PromotionResponseDTO createPromotion(PromotionRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        if (dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("El nombre de la promoción es obligatorio");

        if (dto.getDiscount() == null || dto.getDiscount().doubleValue() <= 0)
            throw new BadRequestException("El descuento debe ser mayor a 0");

        if (dto.getDiscountType() == null)
            throw new BadRequestException("El tipo de descuento es obligatorio");

        Promotion promotion = new Promotion();
        promotion.setName(dto.getName());
        promotion.setDiscount(dto.getDiscount());
        promotion.setDiscountType(dto.getDiscountType());
        promotion.setStoreId(storeId);

        return toPromotionResponse(promotionRepository.save(promotion));
    }

    // ── Obtener promociones activas de la tienda ──────────────────────────────
    public List<PromotionResponseDTO> getActivePromotions() {
        UUID storeId = getStoreIdFromHeader();
        return promotionRepository.findByStoreIdAndIsActiveTrue(storeId)
                .stream().map(this::toPromotionResponse).toList();
    }

    // ── Obtener todas las promociones de la tienda ────────────────────────────
    public List<PromotionResponseDTO> getAllPromotions() {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();
        return promotionRepository.findByStoreId(storeId)
                .stream().map(this::toPromotionResponse).toList();
    }

    // ── Actualizar promoción ──────────────────────────────────────────────────
    public PromotionResponseDTO updatePromotion(UUID promotionId, PromotionRequestDTO dto) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException("Promoción no encontrada con id: " + promotionId));

        if (!promotion.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta promoción no pertenece a tu tienda");

        if (dto.getName() != null)
            promotion.setName(dto.getName());
        if (dto.getDiscount() != null)
            promotion.setDiscount(dto.getDiscount());
        if (dto.getDiscountType() != null)
            promotion.setDiscountType(dto.getDiscountType());

        return toPromotionResponse(promotionRepository.save(promotion));
    }

    // ── Desactivar promoción (eliminado lógico) ───────────────────────────────
    public ApiResponseDTO deactivatePromotion(UUID promotionId) {
        validateAdminOrOwner();
        UUID storeId = getStoreIdFromHeader();

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException("Promoción no encontrada con id: " + promotionId));

        if (!promotion.getStoreId().equals(storeId))
            throw new UnauthorizedException("Esta promoción no pertenece a tu tienda");

        promotion.setIsActive(false);
        promotionRepository.save(promotion);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Promoción desactivada correctamente");
        response.setStatus(200);
        return response;
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

    private void validateAdminOrOwner() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"OWNER".equals(role))
            throw new UnauthorizedException("Solo el ADMIN u OWNER pueden realizar esta acción");
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private PromotionResponseDTO toPromotionResponse(Promotion p) {
        PromotionResponseDTO dto = new PromotionResponseDTO();
        dto.setPromotionId(p.getPromotionId());
        dto.setName(p.getName());
        dto.setDiscount(p.getDiscount());
        dto.setDiscountType(p.getDiscountType());
        dto.setStoreId(p.getStoreId());
        dto.setIsActive(p.getIsActive());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
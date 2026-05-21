package com.api.Preferences.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Preferences.dto.ApiResponseDTO;
import com.api.Preferences.dto.WishlistItemRequestDTO;
import com.api.Preferences.dto.WishlistItemResponseDTO;
import com.api.Preferences.dto.WishlistResponseDTO;
import com.api.Preferences.entity.Wishlist;
import com.api.Preferences.entity.WishlistItem;
import com.api.Preferences.exception.BadRequestException;
import com.api.Preferences.exception.ReviewNotFoundException;
import com.api.Preferences.exception.UnauthorizedException;
import com.api.Preferences.repository.WishlistItemRepository;
import com.api.Preferences.repository.WishlistRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    // ── Obtener wishlist del usuario ──────────────────────────────────────────
    public WishlistResponseDTO getMyWishlist() {
        UUID userId = getUserIdFromHeader();
        Wishlist wishlist = getOrCreateWishlist(userId);
        return toWishlistResponse(wishlist);
    }

    // ── Agregar item a wishlist ───────────────────────────────────────────────
    @Transactional
    public WishlistResponseDTO addItem(WishlistItemRequestDTO dto) {
        UUID userId = getUserIdFromHeader();
        Wishlist wishlist = getOrCreateWishlist(userId);

        if (wishlistItemRepository.existsByWishlistIdAndVariantId(wishlist.getWishlistId(), dto.getVariantId()))
            throw new BadRequestException("El producto ya está en tu wishlist");

        WishlistItem item = new WishlistItem();
        item.setWishlistId(wishlist.getWishlistId());
        item.setVariantId(dto.getVariantId());
        wishlistItemRepository.save(item);

        return toWishlistResponse(wishlist);
    }

    // ── Eliminar item de wishlist ─────────────────────────────────────────────
    @Transactional
    public ApiResponseDTO removeItem(UUID wishlistItemId) {
        UUID userId = getUserIdFromHeader();
        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new ReviewNotFoundException("Item no encontrado con id: " + wishlistItemId));

        if (!item.getWishlistId().equals(wishlist.getWishlistId()))
            throw new UnauthorizedException("No tienes permisos para eliminar este item");

        wishlistItemRepository.delete(item);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Item eliminado de la wishlist correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Wishlist getOrCreateWishlist(UUID userId) {
        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            Wishlist wishlist = new Wishlist();
            wishlist.setUserId(userId);
            return wishlistRepository.save(wishlist);
        });
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

    // ── Mappers ───────────────────────────────────────────────────────────────
    private WishlistResponseDTO toWishlistResponse(Wishlist w) {
        List<WishlistItemResponseDTO> items = wishlistItemRepository
                .findByWishlistId(w.getWishlistId())
                .stream().map(i -> {
                    WishlistItemResponseDTO dto = new WishlistItemResponseDTO();
                    dto.setWishlistItemId(i.getWishlistItemId());
                    dto.setVariantId(i.getVariantId());
                    return dto;
                }).toList();

        WishlistResponseDTO dto = new WishlistResponseDTO();
        dto.setWishlistId(w.getWishlistId());
        dto.setUserId(w.getUserId());
        dto.setItems(items);
        return dto;
    }
}
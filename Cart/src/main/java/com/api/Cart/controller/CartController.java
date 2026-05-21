package com.api.Cart.controller;

import com.api.Cart.dto.cart.AddToCartRequestDTO;
import com.api.Cart.dto.cart.CartResponseDTO;
import com.api.Cart.dto.cart.UpdateCartItemRequestDTO;
import com.api.Cart.service.CartService;
import com.api.Cart.util.HeaderUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Carrito de compras de un usuario en una tienda.
 *
 * El userId se lee del header "x-user-id" (UUID).
 * En producción este header lo inyecta el gateway de autenticación tras validar el JWT.
 *
 * GET    /api/stores/{storeId}/cart                         → Ver carrito
 * POST   /api/stores/{storeId}/cart/items                   → Agregar ítem
 * PUT    /api/stores/{storeId}/cart/items/{cartItemId}      → Actualizar cantidad
 * DELETE /api/stores/{storeId}/cart/items/{cartItemId}      → Eliminar ítem
 * DELETE /api/stores/{storeId}/cart                         → Vaciar carrito
 */
@RestController
@RequestMapping("/api/stores/{storeId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final HeaderUtil headerUtil;

    /** Ver el carrito activo */
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable UUID storeId) {
        UUID userId = requireUserId();
        return ResponseEntity.ok(cartService.getCart(storeId, userId));
    }

    /** Agregar un producto al carrito (si ya existe, suma la cantidad) */
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            @PathVariable UUID storeId,
            @Valid @RequestBody AddToCartRequestDTO dto) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(cartService.addItem(storeId, userId, dto));
    }

    /** Actualizar la cantidad de un ítem (quantity = 0 lo elimina) */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateItem(
            @PathVariable UUID storeId,
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequestDTO dto) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(cartService.updateItem(storeId, userId, cartItemId, dto));
    }

    /** Eliminar un ítem del carrito */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> removeItem(
            @PathVariable UUID storeId,
            @PathVariable UUID cartItemId) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(cartService.removeItem(storeId, userId, cartItemId));
    }

    /** Vaciar y eliminar el carrito completo */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@PathVariable UUID storeId) {
        UUID userId = requireUserId();
        cartService.clearCart(storeId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID requireUserId() {
        return headerUtil.getUserIdFromHeader()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El header 'x-user-id' es obligatorio"));
    }
}

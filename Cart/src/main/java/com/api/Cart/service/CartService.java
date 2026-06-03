package com.api.Cart.service;

import com.api.Cart.client.ProductClient;
import com.api.Cart.client.StoreClient;
import com.api.Cart.client.dto.ProductResponse;
import com.api.Cart.dto.cart.AddToCartRequestDTO;
import com.api.Cart.dto.cart.CartItemResponseDTO;
import com.api.Cart.dto.cart.CartResponseDTO;
import com.api.Cart.dto.cart.UpdateCartItemRequestDTO;
import com.api.Cart.entity.Cart;
import com.api.Cart.entity.CartItem;
import com.api.Cart.exception.CartNotFoundException;
import com.api.Cart.exception.InsufficientStockException;
import com.api.Cart.exception.ProductNotFoundException;
import com.api.Cart.exception.StoreNotFoundException;
import com.api.Cart.repository.CartItemRepository;
import com.api.Cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreClient storeClient;
    private final ProductClient productClient;

    // ── 1. Ver carrito ────────────────────────────────────────────────────────
    public CartResponseDTO getCart(UUID storeId, UUID userId) {
        verifyStoreExists(storeId);
        Cart cart = findOrCreateCart(storeId, userId);
        return toResponse(cart, storeId);
    }

    // ── 2. Agregar / acumular producto ────────────────────────────────────────
    @Transactional
    public CartResponseDTO addItem(UUID storeId, UUID userId, AddToCartRequestDTO dto) {
        verifyStoreExists(storeId);

        ProductResponse product = productClient.findActiveProduct(dto.getProductId(), storeId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Producto no encontrado o no disponible: " + dto.getProductId()));

        if (product.getTotalStock() < dto.getQuantity())
            throw new InsufficientStockException(
                    "Stock insuficiente para '%s'. Disponible: %d"
                            .formatted(product.getName(), product.getTotalStock()));

        Cart cart = findOrCreateCart(storeId, userId);

        cartItemRepository.findByCart_CartIdAndProductId(cart.getCartId(), dto.getProductId())
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + dto.getQuantity();
                            if (product.getTotalStock() < newQty)
                                throw new InsufficientStockException(
                                        "Stock insuficiente. Disponible: %d, en carrito: %d"
                                                .formatted(product.getTotalStock(), existing.getQuantity()));
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .productId(dto.getProductId())
                                    .productName(product.getName())
                                    .productSku(product.getFirstSku())
                                    .productImageUrl(product.getFirstImageUrl())
                                    .quantity(dto.getQuantity())
                                    .unitPrice(product.getFirstPrice())
                                    .build();
                            cart.getItems().add(item);
                        }
                );

        cart.setUpdatedAt(OffsetDateTime.now());
        return toResponse(cartRepository.save(cart), storeId);
    }

    // ── 3. Actualizar cantidad de un ítem ─────────────────────────────────────
    @Transactional
    public CartResponseDTO updateItem(UUID storeId, UUID userId, UUID cartItemId,
                                      UpdateCartItemRequestDTO dto) {
        Cart cart = getCartOrThrow(storeId, userId);
        CartItem item = cartItemRepository
                .findByCartItemIdAndCart_CartId(cartItemId, cart.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Ítem no encontrado en el carrito"));

        if (dto.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            ProductResponse product = productClient
                    .findActiveProduct(item.getProductId(), storeId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Producto no disponible: " + item.getProductId()));

            if (product.getTotalStock() < dto.getQuantity())
                throw new InsufficientStockException(
                        "Stock insuficiente para '%s'. Disponible: %d"
                                .formatted(product.getName(), product.getTotalStock()));

            item.setQuantity(dto.getQuantity());
            cartItemRepository.save(item);
        }

        cart.setUpdatedAt(OffsetDateTime.now());
        return toResponse(cartRepository.save(cart), storeId);
    }

    // ── 4. Eliminar un ítem ───────────────────────────────────────────────────
    @Transactional
    public CartResponseDTO removeItem(UUID storeId, UUID userId, UUID cartItemId) {
        Cart cart = getCartOrThrow(storeId, userId);
        CartItem item = cartItemRepository
                .findByCartItemIdAndCart_CartId(cartItemId, cart.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Ítem no encontrado en el carrito"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cart.setUpdatedAt(OffsetDateTime.now());
        return toResponse(cartRepository.save(cart), storeId);
    }

    // ── 5. Vaciar el carrito ──────────────────────────────────────────────────
    @Transactional
    public void clearCart(UUID storeId, UUID userId) {
        Cart cart = getCartOrThrow(storeId, userId);
        cart.getItems().clear();
        cartItemRepository.deleteAllByCart_CartId(cart.getCartId());
        cartRepository.delete(cart);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cart findOrCreateCart(UUID storeId, UUID userId) {
        return cartRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().storeId(storeId).userId(userId).build()));
    }

    private Cart getCartOrThrow(UUID storeId, UUID userId) {
        return cartRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CartNotFoundException(
                        "No tienes un carrito activo en esta tienda"));
    }

    private void verifyStoreExists(UUID storeId) {
        if (!storeClient.existsStore(storeId))
            throw new StoreNotFoundException("Tienda no encontrada con id: " + storeId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private CartResponseDTO toResponse(Cart cart, UUID storeId) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(item -> toItemResponse(item, storeId))
                .toList();

        BigDecimal subtotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemDTOs.stream()
                .mapToInt(CartItemResponseDTO::getQuantity)
                .sum();

        boolean hasPriceChanges = itemDTOs.stream()
                .anyMatch(CartItemResponseDTO::getPriceChanged);

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .storeId(cart.getStoreId())
                .userId(cart.getUserId())
                .items(itemDTOs)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .hasPriceChanges(hasPriceChanges)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponseDTO toItemResponse(CartItem item, UUID storeId) {
        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        // Consulta precio actual al Product Service para detectar cambios
        ProductResponse current = productClient
                .findProductForPriceCheck(item.getProductId(), storeId);

        boolean priceChanged = false;
        BigDecimal currentPrice = null;

        if (current != null) {
            BigDecimal latestPrice = current.getFirstPrice();
            if (latestPrice != null && item.getUnitPrice().compareTo(latestPrice) != 0) {
                priceChanged = true;
                currentPrice = latestPrice;
            }
        }

        return CartItemResponseDTO.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .productImageUrl(item.getProductImageUrl())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .priceChanged(priceChanged)
                .currentPrice(currentPrice)
                .addedAt(item.getAddedAt())
                .build();
    }
}

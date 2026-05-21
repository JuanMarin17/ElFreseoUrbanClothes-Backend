package com.api.Cart.service;

import com.api.Cart.dto.cart.AddToCartRequestDTO;
import com.api.Cart.dto.cart.CartItemResponseDTO;
import com.api.Cart.dto.cart.CartResponseDTO;
import com.api.Cart.dto.cart.UpdateCartItemRequestDTO;
import com.api.Cart.entity.Cart;
import com.api.Cart.entity.CartItem;
import com.api.Cart.entity.Product;
import com.api.Cart.exception.CartNotFoundException;
import com.api.Cart.exception.InsufficientStockException;
import com.api.Cart.exception.ProductNotFoundException;
import com.api.Cart.exception.StoreNotFoundException;
import com.api.Cart.repository.CartItemRepository;
import com.api.Cart.repository.CartRepository;
import com.api.Cart.repository.ProductRepository;
import com.api.Cart.repository.StoreRepository;
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
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    // ── 1. Ver carrito ────────────────────────────────────────────────────────
    public CartResponseDTO getCart(UUID storeId, UUID userId) {
        verifyStoreExists(storeId);
        Cart cart = findOrCreateCart(storeId, userId);
        return toResponse(cart);
    }

    // ── 2. Agregar / acumular producto ────────────────────────────────────────
    @Transactional
    public CartResponseDTO addItem(UUID storeId, UUID userId, AddToCartRequestDTO dto) {
        verifyStoreExists(storeId);
        Product product = findActiveProduct(dto.getProductId(), storeId);

        if (product.getStock() < dto.getQuantity())
            throw new InsufficientStockException(
                    "Stock insuficiente para '%s'. Disponible: %d"
                            .formatted(product.getName(), product.getStock()));

        Cart cart = findOrCreateCart(storeId, userId);

        cartItemRepository.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), product.getProductId())
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + dto.getQuantity();
                            if (product.getStock() < newQty)
                                throw new InsufficientStockException(
                                        "Stock insuficiente. Disponible: %d, en carrito: %d"
                                                .formatted(product.getStock(), existing.getQuantity()));
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(dto.getQuantity())
                                    .unitPrice(product.getPrice())
                                    .build();
                            cart.getItems().add(item);
                        }
                );

        cart.setUpdatedAt(OffsetDateTime.now());
        return toResponse(cartRepository.save(cart));
    }

    // ── 3. Actualizar cantidad de un ítem ─────────────────────────────────────
    @Transactional
    public CartResponseDTO updateItem(UUID storeId, UUID userId, UUID cartItemId, UpdateCartItemRequestDTO dto) {
        Cart cart = getCartOrThrow(storeId, userId);
        CartItem item = cartItemRepository
                .findByCartItemIdAndCart_CartId(cartItemId, cart.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Ítem no encontrado en el carrito"));

        if (dto.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            if (product.getStock() < dto.getQuantity())
                throw new InsufficientStockException(
                        "Stock insuficiente para '%s'. Disponible: %d"
                                .formatted(product.getName(), product.getStock()));
            item.setQuantity(dto.getQuantity());
            cartItemRepository.save(item);
        }

        cart.setUpdatedAt(OffsetDateTime.now());
        return toResponse(cartRepository.save(cart));
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
        return toResponse(cartRepository.save(cart));
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

    private Product findActiveProduct(UUID productId, UUID storeId) {
        return productRepository.findByProductIdAndStoreId(productId, storeId)
                .filter(Product::getIsActive)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Producto no encontrado o no disponible: " + productId));
    }

    private void verifyStoreExists(UUID storeId) {
        if (!storeRepository.existsById(storeId))
            throw new StoreNotFoundException("Tienda no encontrada con id: " + storeId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private CartResponseDTO toResponse(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                .map(this::toItemResponse)
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

    private CartItemResponseDTO toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal currentPrice = product.getPrice();
        boolean priceChanged = item.getUnitPrice().compareTo(currentPrice) != 0;
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponseDTO.builder()
                .cartItemId(item.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .productSku(product.getSku())
                .productImageUrl(product.getImageUrl())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .priceChanged(priceChanged)
                .currentPrice(priceChanged ? currentPrice : null)
                .addedAt(item.getAddedAt())
                .build();
    }
}

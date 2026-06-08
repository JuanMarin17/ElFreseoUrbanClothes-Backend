package com.api.OrderPayment.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO que espeja la respuesta del módulo Cart (puerto 8086).
 * Debe mantenerse sincronizado con CartResponseDTO del módulo de carrito.
 */
@Data
public class CartResponseDTO {

    private UUID cartId;
    private UUID userId;
    private UUID storeId;
    private List<CartItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalWithDiscount;
    private int totalItems;

    @Data
    public static class CartItemDTO {
        private UUID cartItemId;
        private UUID productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}

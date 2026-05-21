package com.api.Cart.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class CartItemResponseDTO {

    private UUID cartItemId;
    private UUID productId;
    private String productName;
    private String productSku;
    private String productImageUrl;

    /** Precio guardado al agregar al carrito */
    private BigDecimal unitPrice;
    private Integer quantity;
    /** unitPrice × quantity */
    private BigDecimal subtotal;

    /** true si el precio actual del producto difiere del guardado */
    private Boolean priceChanged;
    /** Precio actual del producto en catálogo */
    private BigDecimal currentPrice;

    private OffsetDateTime addedAt;
}

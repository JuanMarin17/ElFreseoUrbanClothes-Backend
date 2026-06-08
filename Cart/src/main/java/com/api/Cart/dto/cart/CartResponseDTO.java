package com.api.Cart.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponseDTO {

    private UUID cartId;
    private UUID storeId;
    private UUID userId;

    private List<CartItemResponseDTO> items;

    /** Suma de todas las quantities */
    private Integer totalItems;
    /** Suma de todos los subtotales */
    private BigDecimal subtotal;

    /** true si algún ítem tiene priceChanged = true */
    private Boolean hasPriceChanges;

    private OffsetDateTime updatedAt;
}

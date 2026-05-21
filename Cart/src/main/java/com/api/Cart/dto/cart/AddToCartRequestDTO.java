package com.api.Cart.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Agregar un producto al carrito.
 * Si el producto ya existe, la cantidad se SUMA al existente.
 */
@Data
public class AddToCartRequestDTO {

    @NotNull(message = "El id del producto es obligatorio")
    private UUID productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer quantity;
}

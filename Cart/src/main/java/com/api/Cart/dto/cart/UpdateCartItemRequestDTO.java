package com.api.Cart.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Actualiza la cantidad de un ítem en el carrito.
 * Si quantity = 0, el ítem se elimina automáticamente.
 */
@Data
public class UpdateCartItemRequestDTO {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer quantity;
}

package com.api.OrderPayment.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo de la petición para crear una orden desde el carrito activo.
 */
@Data
public class CreateOrderRequestDTO {

    /** Dirección de envío (opcional si es retiro en tienda) */
    @Size(max = 500, message = "La dirección no puede superar 500 caracteres")
    private String shippingAddress;

    /** Notas adicionales del cliente */
    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notes;
}

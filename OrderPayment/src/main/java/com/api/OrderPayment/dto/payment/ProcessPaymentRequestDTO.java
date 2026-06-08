package com.api.OrderPayment.dto.payment;

import com.api.OrderPayment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo de la petición para procesar un pago sobre una orden existente.
 */
@Data
public class ProcessPaymentRequestDTO {

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod method;

    /**
     * Referencia de transacción externa (token de Stripe, ID de PayU, etc.)
     * En producción, el frontend envía este token tras interactuar con el gateway.
     */
    @Size(max = 255)
    private String transactionReference;

    /** Detalles adicionales (últimos 4 dígitos, banco, etc.) */
    @Size(max = 500)
    private String details;
}

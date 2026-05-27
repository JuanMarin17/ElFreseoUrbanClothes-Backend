package com.tuapp.payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutApiRequest {

    @NotBlank
    private String tenantId;

    @NotBlank
    private String externalReference;

    /** Token de tarjeta generado por MercadoPago.js en el frontend */
    @NotBlank(message = "El token de tarjeta es requerido")
    private String cardToken;

    @NotNull @Positive
    private BigDecimal amount;

    private String description;

    private Integer installments = 1;

    /** ID del método de pago (visa, master, amex, etc.) */
    @NotBlank
    private String paymentMethodId;

    @NotNull
    private Payer payer;

    @Data
    public static class Payer {
        @NotBlank
        private String email;
        private String firstName;
        private String lastName;
        private Identification identification;

        @Data
        public static class Identification {
            private String type;  // CC, NIT, etc.
            private String number;
        }
    }
}

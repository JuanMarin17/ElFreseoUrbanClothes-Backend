package com.api.payments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutProRequest {

    @NotBlank(message = "El tenantId es requerido")
    private String tenantId;

    /** ID de la orden interna de tu app */
    @NotBlank(message = "La referencia externa es requerida")
    private String externalReference;

    @NotNull
    private List<CheckoutItem> items;

    @Data
    public static class CheckoutItem {
        @NotBlank
        private String title;
        private String description;
        @NotNull @Positive
        private BigDecimal unitPrice;
        @NotNull @Positive
        private Integer quantity;
        private String pictureUrl;
        private String currencyId = "COP";
    }

    /** URL de éxito (opcional, override del default) */
    private String backUrls;

    /** Email del comprador (opcional) */
    private String payerEmail;
}

package com.api.payments.dto.request;

import com.api.payments.enums.SubscriptionPlan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequest {

    @NotBlank(message = "El tenantId es requerido")
    private String tenantId;

    @NotNull(message = "El plan es requerido")
    private SubscriptionPlan plan;

    @NotBlank(message = "El email del pagador es requerido")
    private String payerEmail;
}

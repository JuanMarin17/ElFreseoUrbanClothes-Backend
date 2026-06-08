package com.api.Transaction.dto;

import java.util.UUID;

import com.api.Transaction.enums.PlanName;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequestDTO {

    @NotNull(message = "El store_id es obligatorio")
    private UUID storeId;

    @NotNull(message = "El plan es obligatorio")
    private PlanName planName;
}
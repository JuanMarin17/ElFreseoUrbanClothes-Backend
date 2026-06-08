package com.api.Transaction.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class CheckoutResponseDTO {
    private UUID transactionId;
    private String paymentUrl; // URL de Mercado Pago donde el OWNER paga
    private String status;
}
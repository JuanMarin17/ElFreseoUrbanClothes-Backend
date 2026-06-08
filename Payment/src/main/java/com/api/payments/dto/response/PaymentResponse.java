package com.api.payments.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.api.payments.enums.PaymentStatus;
import com.api.payments.enums.PaymentType;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private String tenantId;
    private String mpPaymentId;
    private String mpPreferenceId;
    private PaymentType paymentType;
    private PaymentStatus status;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal netAmount;
    private String currency;
    private String externalReference;
    private String paymentMethodId;
    private String description;
    private String payerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    /** Solo para Checkout Pro: URL de pago de Mercado Pago */
    private String checkoutUrl;

    /** Solo para Checkout Pro: URL sandbox */
    private String sandboxCheckoutUrl;
}

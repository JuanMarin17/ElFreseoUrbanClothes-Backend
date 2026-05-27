package com.tuapp.payments.dto.response;

import com.tuapp.payments.enums.PaymentStatus;
import com.tuapp.payments.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

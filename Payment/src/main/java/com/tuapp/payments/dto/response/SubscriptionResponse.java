package com.tuapp.payments.dto.response;

import com.tuapp.payments.enums.PaymentStatus;
import com.tuapp.payments.enums.SubscriptionPlan;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SubscriptionResponse {
    private UUID id;
    private String tenantId;
    private SubscriptionPlan plan;
    private PaymentStatus status;
    private String mpSubscriptionId;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private LocalDateTime nextBillingAt;
    private boolean active;

    /** URL para que el cliente complete el pago */
    private String checkoutUrl;
    private String sandboxCheckoutUrl;
}

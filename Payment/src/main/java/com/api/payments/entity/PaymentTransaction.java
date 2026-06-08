package com.api.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.api.payments.enums.PaymentStatus;
import com.api.payments.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de cada transacción de pago.
 * Aplica tanto para suscripciones (pago → plataforma)
 * como para ventas de tienda (pago con split).
 */
@Entity
@Table(name = "payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Tenant que origina el pago (null si es suscripción del propietario de la tienda) */
    @Column(name = "tenant_id")
    private String tenantId;

    /** ID de pago generado por Mercado Pago */
    @Column(name = "mp_payment_id", unique = true)
    private String mpPaymentId;

    /** ID de la preferencia (Checkout Pro) */
    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    /** ID del merchant order (agrupa pagos) */
    @Column(name = "mp_merchant_order_id")
    private String mpMerchantOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Monto total de la transacción */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Comisión retenida por la plataforma (1%) — solo en ventas de tienda */
    @Column(name = "platform_fee", precision = 15, scale = 2)
    private BigDecimal platformFee;

    /** Monto neto que recibe la tienda */
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "COP";

    /** ID de pedido interno de tu aplicación */
    @Column(name = "external_reference")
    private String externalReference;

    /** Método de pago devuelto por MP (credit_card, debit_card, etc.) */
    @Column(name = "payment_method_id")
    private String paymentMethodId;

    /** Tipo de método (credit_card, ticket, bank_transfer...) */
    @Column(name = "payment_type_id")
    private String paymentTypeId;

    @Column(name = "description")
    private String description;

    /** Email del pagador según MP */
    @Column(name = "payer_email")
    private String payerEmail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}

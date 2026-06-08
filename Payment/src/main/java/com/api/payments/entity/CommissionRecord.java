package com.api.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro contable de cada comisión cobrada por la plataforma.
 * Relacionada 1:1 con un PaymentTransaction de tipo STORE_SALE.
 */
@Entity
@Table(name = "commission_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "payment_transaction_id", nullable = false)
    private UUID paymentTransactionId;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    /** Monto original de la venta */
    @Column(name = "sale_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal saleAmount;

    /** Porcentaje cobrado (ej. 0.01 = 1%) */
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    /** Monto de comisión cobrado */
    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "COP";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

package com.api.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.api.payments.enums.PaymentStatus;
import com.api.payments.enums.SubscriptionPlan;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Plan de suscripción activo de un tenant.
 * Se crea/actualiza cuando MP confirma el pago del plan.
 */
@Entity
@Table(name = "tenant_subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** ID de la suscripción en Mercado Pago (para suscripciones recurrentes) */
    @Column(name = "mp_subscription_id")
    private String mpSubscriptionId;

    /** ID del preapproval plan en MP */
    @Column(name = "mp_preapproval_plan_id")
    private String mpPreapprovalPlanId;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "next_billing_at")
    private LocalDateTime nextBillingAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == PaymentStatus.APPROVED
            && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
}

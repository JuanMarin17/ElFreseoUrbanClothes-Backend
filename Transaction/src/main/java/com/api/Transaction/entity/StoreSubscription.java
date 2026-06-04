package com.api.Transaction.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.Transaction.enums.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "store_subscription")
public class StoreSubscription {

    @Id
    @GeneratedValue
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "renewal_at")
    private LocalDateTime renewalAt;
}
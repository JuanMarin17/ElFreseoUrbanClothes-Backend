package com.api.Promotion.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "coupon_redemption")
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "redemption_id")
    private UUID redemptionId;

    @Column(name = "coupon_id")
    private UUID couponId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @PrePersist
    void onCreate() {
        this.usedAt = OffsetDateTime.now();
    }
}
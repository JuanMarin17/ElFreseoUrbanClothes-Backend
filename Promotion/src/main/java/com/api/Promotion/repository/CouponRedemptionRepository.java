package com.api.Promotion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Promotion.entity.CouponRedemption;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {
    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);

    List<CouponRedemption> findByCouponId(UUID couponId);
}
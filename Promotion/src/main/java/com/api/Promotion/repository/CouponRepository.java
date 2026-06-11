package com.api.Promotion.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Promotion.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    List<Coupon> findByStoreIdAndActiveTrue(UUID storeId);

    List<Coupon> findByStoreId(UUID storeId);

    Optional<Coupon> findByCodeAndStoreId(String code, UUID storeId);

    boolean existsByCode(String code);
}
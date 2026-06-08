package com.api.Promotion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Promotion.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByStoreIdAndIsActiveTrue(UUID storeId);

    List<Promotion> findByStoreId(UUID storeId);

    List<Promotion> findByStoreIdAndProductIdInAndIsActiveTrue(UUID storeId, List<UUID> productIds);
}
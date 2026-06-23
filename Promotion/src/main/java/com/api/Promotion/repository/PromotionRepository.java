package com.api.Promotion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Promotion.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByStoreIdAndActiveTrue(UUID storeId);

    Page<Promotion> findByStoreId(UUID storeId, Pageable pageable);

    List<Promotion> findByStoreIdAndProductIdInAndActiveTrue(UUID storeId, List<UUID> productIds);
}
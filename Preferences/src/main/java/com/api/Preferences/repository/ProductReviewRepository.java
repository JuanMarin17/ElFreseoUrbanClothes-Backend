package com.api.Preferences.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Preferences.entity.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    List<ProductReview> findByProductId(UUID productId);

    List<ProductReview> findByUserId(UUID userId);

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);
}
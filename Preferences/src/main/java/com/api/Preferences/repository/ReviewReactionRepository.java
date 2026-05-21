package com.api.Preferences.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Preferences.entity.ReviewReaction;

public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, UUID> {
    List<ReviewReaction> findByReviewId(UUID reviewId);

    Optional<ReviewReaction> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);
}
package com.api.Reviews.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Reviews.entity.ReviewLike;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    void deleteAllByReviewId(UUID reviewId);
}

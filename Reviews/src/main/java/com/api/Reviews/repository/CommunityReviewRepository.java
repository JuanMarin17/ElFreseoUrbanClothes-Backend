package com.api.Reviews.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Reviews.entity.CommunityReview;

public interface CommunityReviewRepository extends JpaRepository<CommunityReview, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<CommunityReview> findByUserId(UUID userId);

    Page<CommunityReview> findByStatus(String status, Pageable pageable);
}

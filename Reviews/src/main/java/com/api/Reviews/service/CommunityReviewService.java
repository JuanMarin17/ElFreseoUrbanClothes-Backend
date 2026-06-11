package com.api.Reviews.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Reviews.dto.CommunityReviewPageDTO;
import com.api.Reviews.dto.CommunityReviewRequestDTO;
import com.api.Reviews.dto.CommunityReviewResponseDTO;
import com.api.Reviews.dto.LikeResponseDTO;
import com.api.Reviews.entity.CommunityReview;
import com.api.Reviews.entity.ReviewLike;
import com.api.Reviews.exception.BadRequestException;
import com.api.Reviews.exception.ConflictException;
import com.api.Reviews.exception.ReviewNotFoundException;
import com.api.Reviews.exception.UnauthorizedException;
import com.api.Reviews.repository.CommunityReviewRepository;
import com.api.Reviews.repository.ReviewLikeRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityReviewService {

    private final CommunityReviewRepository reviewRepository;
    private final ReviewLikeRepository likeRepository;

    @Transactional(readOnly = true)
    public CommunityReviewPageDTO getApprovedReviews(int page, int limit, String sort) {
        Sort sortOrder = "top".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "likes")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), sortOrder);
        Page<CommunityReview> reviewPage = reviewRepository.findByStatus("approved", pageable);

        List<CommunityReviewResponseDTO> data = reviewPage.getContent()
                .stream()
                .map(this::toResponseDTO)
                .toList();

        return CommunityReviewPageDTO.builder()
                .data(data)
                .total(reviewPage.getTotalElements())
                .page(page)
                .totalPages(reviewPage.getTotalPages())
                .build();
    }

    @Transactional
    public CommunityReviewResponseDTO createReview(CommunityReviewRequestDTO dto) {
        UUID userId = getUserIdFromContext();

        if (reviewRepository.existsByUserId(userId))
            throw new ConflictException("Ya existe una reseña para este usuario");

        String userName = RequestContext.getHeader("X-User-Name");
        String userEmail = RequestContext.getHeader("X-User-Email");

        CommunityReview review = CommunityReview.builder()
                .userId(userId)
                .userName(userName != null ? userName : "")
                .userEmail(userEmail != null ? userEmail : "")
                .rating(dto.getRating())
                .text(dto.getText())
                .likes(0)
                .status("approved")
                .build();

        return toResponseDTO(reviewRepository.save(review));
    }

    @Transactional
    public LikeResponseDTO toggleLike(UUID reviewId) {
        UUID userId = getUserIdFromContext();

        CommunityReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId));

        Optional<ReviewLike> existing = likeRepository.findByReviewIdAndUserId(reviewId, userId);

        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            review.setLikes(Math.max(0, review.getLikes() - 1));
            liked = false;
        } else {
            likeRepository.save(ReviewLike.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .build());
            review.setLikes(review.getLikes() + 1);
            liked = true;
        }

        reviewRepository.save(review);

        return LikeResponseDTO.builder()
                .likes(review.getLikes())
                .liked(liked)
                .build();
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        UUID userId = getUserIdFromContext();
        String role = RequestContext.getHeader("X-User-Role");

        CommunityReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId));

        boolean isOwner = review.getUserId().equals(userId);
        boolean isPrivileged = "ADMIN".equals(role) || "SUPERADMIN".equals(role);

        if (!isOwner && !isPrivileged)
            throw new UnauthorizedException("No tienes permisos para eliminar esta reseña");

        likeRepository.deleteAllByReviewId(reviewId);
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public CommunityReviewResponseDTO getMyReview() {
        UUID userId = getUserIdFromContext();
        return reviewRepository.findByUserId(userId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ReviewNotFoundException("No tienes una reseña activa"));
    }

    private UUID getUserIdFromContext() {
        String header = RequestContext.getHeader("X-User-Id");
        if (header == null || header.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    private CommunityReviewResponseDTO toResponseDTO(CommunityReview r) {
        return CommunityReviewResponseDTO.builder()
                .id(r.getId())
                .userName(r.getUserName())
                .userEmail(r.getUserEmail())
                .rating(r.getRating())
                .text(r.getText())
                .likes(r.getLikes())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

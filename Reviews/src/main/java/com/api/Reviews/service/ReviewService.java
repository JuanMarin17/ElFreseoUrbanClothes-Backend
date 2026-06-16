package com.api.Reviews.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Reviews.dto.ApiResponseDTO;
import com.api.Reviews.dto.ReactionRequestDTO;
import com.api.Reviews.dto.ReactionResponseDTO;
import com.api.Reviews.dto.ReplyRequestDTO;
import com.api.Reviews.dto.ReplyResponseDTO;
import com.api.Reviews.dto.ReviewRequestDTO;
import com.api.Reviews.dto.ReviewResponseDTO;
import com.api.Reviews.entity.ProductReview;
import com.api.Reviews.entity.ReviewReaction;
import com.api.Reviews.entity.ReviewReply;
import com.api.Reviews.exception.BadRequestException;
import com.api.Reviews.exception.ReviewNotFoundException;
import com.api.Reviews.exception.UnauthorizedException;
import com.api.Reviews.repository.ProductReviewRepository;
import com.api.Reviews.repository.ReviewReactionRepository;
import com.api.Reviews.repository.ReviewReplyRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ReviewReactionRepository reactionRepository;
    private final ReviewReplyRepository replyRepository;
    private final NotificationService notificationService;

    public ReviewResponseDTO createReview(ReviewRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        if (dto.getProductId() == null)
            throw new BadRequestException("El productId es obligatorio");

        if (dto.getRating() == null || dto.getRating() < 0 || dto.getRating() > 5)
            throw new BadRequestException("El rating debe estar entre 0 y 5");

        if (reviewRepository.existsByProductIdAndUserId(dto.getProductId(), userId))
            throw new BadRequestException("Ya existe una reseña para este producto");

        ProductReview review = new ProductReview();
        review.setProductId(dto.getProductId());
        review.setStoreId(dto.getStoreId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setBody(dto.getBody());

        ProductReview saved = reviewRepository.save(review);

        if (saved.getStoreId() != null) {
            try {
                notificationService.notifyStore(saved.getStoreId(), "new-review", Map.of(
                        "reviewId", saved.getReviewId(),
                        "productId", saved.getProductId(),
                        "rating", saved.getRating(),
                        "userId", userId));
            } catch (Exception e) {
                // notificación no bloquea la respuesta
            }
        }

        return toReviewResponse(saved);
    }

    public List<ReviewResponseDTO> getReviewsByProduct(UUID productId) {
        return reviewRepository.findByProductId(productId)
                .stream().map(this::toReviewResponse).toList();
    }

    public List<ReviewResponseDTO> getMyReviews() {
        UUID userId = getUserIdFromHeader();
        return reviewRepository.findByUserId(userId)
                .stream().map(this::toReviewResponse).toList();
    }

    public ApiResponseDTO deleteReview(UUID reviewId) {
        UUID userId = getUserIdFromHeader();
        String role = RequestContext.getHeader("X-User-Role");

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Reseña no encontrada con id: " + reviewId));

        if (!review.getUserId().equals(userId) &&
                !"OWNER".equals(role))
            throw new UnauthorizedException("No tienes permisos para eliminar esta reseña");

        reviewRepository.delete(review);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Reseña eliminada correctamente");
        response.setStatus(200);
        return response;
    }

    @Transactional
    public ReactionResponseDTO reactToReview(UUID reviewId, ReactionRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        if (!reviewRepository.existsById(reviewId))
            throw new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId);

        Optional<ReviewReaction> existing = reactionRepository
                .findByReviewIdAndUserId(reviewId, userId);

        if (existing.isPresent()) {
            ReviewReaction reaction = existing.get();
            reaction.setReactionType(dto.getReactionType());
            return toReactionResponse(reactionRepository.save(reaction));
        }

        ReviewReaction reaction = new ReviewReaction();
        reaction.setReviewId(reviewId);
        reaction.setUserId(userId);
        reaction.setReactionType(dto.getReactionType());

        return toReactionResponse(reactionRepository.save(reaction));
    }

    public ReplyResponseDTO replyToReview(UUID reviewId, ReplyRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        if (!reviewRepository.existsById(reviewId))
            throw new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId);

        if (dto.getBody() == null || dto.getBody().isBlank())
            throw new BadRequestException("El cuerpo de la respuesta es obligatorio");

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setUserId(userId);
        reply.setBody(dto.getBody());

        return toReplyResponse(replyRepository.save(reply));
    }

    public List<ReplyResponseDTO> getRepliesByReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId))
            throw new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId);

        return replyRepository.findByReviewId(reviewId)
                .stream().map(this::toReplyResponse).toList();
    }

    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    private ReviewResponseDTO toReviewResponse(ProductReview r) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewId(r.getReviewId());
        dto.setProductId(r.getProductId());
        dto.setUserId(r.getUserId());
        dto.setRating(r.getRating());
        dto.setTitle(r.getTitle());
        dto.setBody(r.getBody());
        dto.setIsVerified(r.getIsVerified());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private ReactionResponseDTO toReactionResponse(ReviewReaction r) {
        ReactionResponseDTO dto = new ReactionResponseDTO();
        dto.setReactionId(r.getReactionId());
        dto.setReviewId(r.getReviewId());
        dto.setUserId(r.getUserId());
        dto.setReactionType(r.getReactionType());
        return dto;
    }

    private ReplyResponseDTO toReplyResponse(ReviewReply r) {
        ReplyResponseDTO dto = new ReplyResponseDTO();
        dto.setReplyId(r.getReplyId());
        dto.setReviewId(r.getReviewId());
        dto.setUserId(r.getUserId());
        dto.setBody(r.getBody());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
package com.api.Preferences.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Preferences.dto.ApiResponseDTO;
import com.api.Preferences.dto.ReactionRequestDTO;
import com.api.Preferences.dto.ReactionResponseDTO;
import com.api.Preferences.dto.ReplyRequestDTO;
import com.api.Preferences.dto.ReplyResponseDTO;
import com.api.Preferences.dto.ReviewRequestDTO;
import com.api.Preferences.dto.ReviewResponseDTO;
import com.api.Preferences.entity.ProductReview;
import com.api.Preferences.entity.ReviewReaction;
import com.api.Preferences.entity.ReviewReply;
import com.api.Preferences.exception.BadRequestException;
import com.api.Preferences.exception.ReviewNotFoundException;
import com.api.Preferences.exception.UnauthorizedException;
import com.api.Preferences.repository.ProductReviewRepository;
import com.api.Preferences.repository.ReviewReactionRepository;
import com.api.Preferences.repository.ReviewReplyRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ReviewReactionRepository reactionRepository;
    private final ReviewReplyRepository replyRepository;

    // ── Crear reseña ──────────────────────────────────────────────────────────
    public ReviewResponseDTO createReview(ReviewRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        if (reviewRepository.existsByProductIdAndUserId(dto.getProductId(), userId))
            throw new BadRequestException("Ya existe una reseña para este producto");

        if (dto.getRating() < 0 || dto.getRating() > 5)
            throw new BadRequestException("El rating debe estar entre 1 y 5");

        ProductReview review = new ProductReview();
        review.setProductId(dto.getProductId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setBody(dto.getBody());

        return toReviewResponse(reviewRepository.save(review));
    }

    // ── Obtener reseñas por producto ──────────────────────────────────────────
    public List<ReviewResponseDTO> getReviewsByProduct(UUID productId) {
        return reviewRepository.findByProductId(productId)
                .stream().map(this::toReviewResponse).toList();
    }

    // ── Obtener mis reseñas ───────────────────────────────────────────────────
    public List<ReviewResponseDTO> getMyReviews() {
        UUID userId = getUserIdFromHeader();
        return reviewRepository.findByUserId(userId)
                .stream().map(this::toReviewResponse).toList();
    }

    // ── Eliminar reseña ───────────────────────────────────────────────────────
    public ApiResponseDTO deleteReview(UUID reviewId) {
        UUID userId = getUserIdFromHeader();
        String role = RequestContext.getHeader("X-User-Role");

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId));

        // Solo el dueño de la reseña o el OWNER pueden eliminarla
        if (!review.getUserId().equals(userId) && !"OWNER".equals(role))
            throw new UnauthorizedException("No tienes permisos para eliminar esta reseña");

        reviewRepository.delete(review);

        ApiResponseDTO response = new ApiResponseDTO();
        response.setMessage("Reseña eliminada correctamente");
        response.setStatus(200);
        return response;
    }

    // ── Reaccionar a reseña ───────────────────────────────────────────────────
    @Transactional
    public ReactionResponseDTO reactToReview(UUID reviewId, ReactionRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        if (!reviewRepository.existsById(reviewId))
            throw new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId);

        Optional<ReviewReaction> existing = reactionRepository.findByReviewIdAndUserId(reviewId, userId);

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

    // ── Responder reseña ──────────────────────────────────────────────────────
    public ReplyResponseDTO replyToReview(UUID reviewId, ReplyRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        if (!reviewRepository.existsById(reviewId))
            throw new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId);

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setUserId(userId);
        reply.setBody(dto.getBody());

        return toReplyResponse(replyRepository.save(reply));
    }

    // ── Obtener respuestas de una reseña ──────────────────────────────────────
    public List<ReplyResponseDTO> getRepliesByReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId))
            throw new ReviewNotFoundException("Reseña no encontrada con id: " + reviewId);

        return replyRepository.findByReviewId(reviewId)
                .stream().map(this::toReplyResponse).toList();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
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

    // ── Mappers ───────────────────────────────────────────────────────────────
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
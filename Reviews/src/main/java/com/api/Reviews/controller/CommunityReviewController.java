package com.api.Reviews.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.Reviews.dto.CommunityReviewPageDTO;
import com.api.Reviews.dto.CommunityReviewRequestDTO;
import com.api.Reviews.dto.CommunityReviewResponseDTO;
import com.api.Reviews.dto.LikeResponseDTO;
import com.api.Reviews.service.CommunityReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class CommunityReviewController {

    private final CommunityReviewService communityReviewService;

    /** Público — solo reseñas aprobadas, paginadas */
    @GetMapping
    public ResponseEntity<CommunityReviewPageDTO> getReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(communityReviewService.getApprovedReviews(page, limit, sort));
    }

    /** Requiere JWT — crea la reseña del usuario autenticado */
    @PostMapping
    public ResponseEntity<CommunityReviewResponseDTO> createReview(
            @Valid @RequestBody CommunityReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communityReviewService.createReview(dto));
    }

    /** Requiere JWT — toggle like/unlike */
    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponseDTO> toggleLike(@PathVariable UUID id) {
        return ResponseEntity.ok(communityReviewService.toggleLike(id));
    }

    /** Requiere JWT — solo dueño o ADMIN/SUPERADMIN */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        communityReviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    /** Requiere JWT — reseña del usuario autenticado, 404 si no tiene */
    @GetMapping("/me")
    public ResponseEntity<CommunityReviewResponseDTO> getMyReview() {
        return ResponseEntity.ok(communityReviewService.getMyReview());
    }
}

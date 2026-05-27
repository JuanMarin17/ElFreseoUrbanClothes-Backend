package com.api.Reviews.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Reviews.dto.ApiResponseDTO;
import com.api.Reviews.dto.ReactionRequestDTO;
import com.api.Reviews.dto.ReactionResponseDTO;
import com.api.Reviews.dto.ReplyRequestDTO;
import com.api.Reviews.dto.ReplyResponseDTO;
import com.api.Reviews.dto.ReviewRequestDTO;
import com.api.Reviews.dto.ReviewResponseDTO;
import com.api.Reviews.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(dto));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getMyReviews());
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponseDTO> deleteReview(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(reviewService.deleteReview(reviewId));
    }

    @PostMapping("/{reviewId}/reactions")
    public ResponseEntity<ReactionResponseDTO> react(
            @PathVariable UUID reviewId,
            @RequestBody ReactionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.reactToReview(reviewId, dto));
    }

    @PostMapping("/{reviewId}/replies")
    public ResponseEntity<ReplyResponseDTO> reply(
            @PathVariable UUID reviewId,
            @RequestBody ReplyRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.replyToReview(reviewId, dto));
    }

    @GetMapping("/{reviewId}/replies")
    public ResponseEntity<List<ReplyResponseDTO>> getReplies(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(reviewService.getRepliesByReview(reviewId));
    }
}
package com.api.Promotion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Promotion.dto.ApiResponseDTO;
import com.api.Promotion.dto.PromotionRequestDTO;
import com.api.Promotion.dto.PromotionResponseDTO;
import com.api.Promotion.service.PromotionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/createPromotion")
    public ResponseEntity<PromotionResponseDTO> createPromotion(@RequestBody PromotionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(dto));
    }

    @GetMapping("/getActivePromotions")
    public ResponseEntity<List<PromotionResponseDTO>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }

    @GetMapping("/getAllPromotions")
    public ResponseEntity<List<PromotionResponseDTO>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<PromotionResponseDTO> updatePromotion(
            @PathVariable UUID promotionId,
            @RequestBody PromotionRequestDTO dto) {
        return ResponseEntity.ok(promotionService.updatePromotion(promotionId, dto));
    }

    @DeleteMapping("/{promotionId}")
    public ResponseEntity<ApiResponseDTO> deactivatePromotion(@PathVariable UUID promotionId) {
        return ResponseEntity.ok(promotionService.deactivatePromotion(promotionId));
    }
}